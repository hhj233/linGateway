package com.lin.registerCenter.nacosImpl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;
import com.lin.common.constant.GatewayConst;
import com.lin.common.utils.JsonUtil;
import com.lin.registerCenter.api.RegisterCenter;
import com.lin.registerCenter.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 实现我们注册中心的具体方法
 */
@Slf4j
public class NacosRegisterCenter implements RegisterCenter {
    /**
     * 注册中心地址
     */
    private String registerAddress;

    /**
     * 环境选择
     */
    private String env;

    /**
     * nacos用于维护服务实例信息
     */
    private NamingService namingService;

    /**
     * nacos用于维护服务定义信息
     */
    private NamingMaintainService namingMaintainService;

    /**
     * 监听器列表
     * 监听器列表会变化，出现线程安全问题
     */
    private List<RegisterCenterListener> registerCenterListenerList = new CopyOnWriteArrayList<>();

    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress = registerAddress;
        this.env = env;
        try {
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService = NamingFactory.createNamingService(registerAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            // 构造nacos实例
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
            nacosInstance.setPort(serviceInstance.getPort());
            nacosInstance.setIp(serviceInstance.getIp());
            // 实例信息可以放到mateData中
            nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY, JsonUtil.toJsonString(serviceInstance)));

            // 注册
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);

            // 更新服务定义
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0,
                    Map.of(GatewayConst.META_DATA_KEY, JsonUtil.toJsonString(serviceDefinition)));
            log.info("register {} {}", serviceDefinition, serviceInstance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            namingService.deregisterInstance(serviceDefinition.getServiceId(), env, serviceInstance.getIp(), serviceInstance.getPort());
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllServices(RegisterCenterListener registerCenterListener) {
        // 服务订阅首先需要将我们的服务监听器加入我们的服务列表中;
        registerCenterListenerList.add(registerCenterListener);
        // 进行服务订阅
        doSubscribeAllServices();

        // 可能有新服务加入，需要一个定时任务来检查
        ScheduledExecutorService scheduleThreadPool = Executors.newScheduledThreadPool(1,new NameThreadFactory("doSubscribeAllServices"));
        // 循环执行服务发现与订阅
        scheduleThreadPool.scheduleWithFixedDelay(() -> doSubscribeAllServices(), 10, 10, TimeUnit.SECONDS);
    }


    private void doSubscribeAllServices() {
        try {
            // 得到当前服务已经订阅的服务
            // 这里其实已经在init的时候初始化namingService了，所以这里可以直接拿到当前服务已经订阅的服务
            Set<String> subscribeAllServicesSet = namingService.getSubscribeServices().stream().map(ServiceInfo::getName).collect(Collectors.toSet());

            int pageNo = 1;
            int pageSize = 100;

            // 分页从nacos拿到所有的服务列表
            List<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();
            while (CollectionUtils.isNotEmpty(serviceList)) {
                log.info("serviceList size:{}", serviceList.size());
                for (String service : serviceList) {
                    // 判断当前是否已经订阅了服务
                    if (subscribeAllServicesSet.contains(service)) {
                        continue;
                    }

                    // nacos 事件监听器 订阅当前服务
                    // 我们需要自己实现一个nacos的事件订阅类，来具体执行订阅执行时的操作
                    NacosRegisterListener nacosRegisterListener = new NacosRegisterListener();
                    // 当前服务之前不存在，调用监听器方法进行添加处理
                    nacosRegisterListener.onEvent(new NamingEvent(service, null));
                    // 为指定服务和环境注册一个事件监听器
                    namingService.subscribe(service, env, nacosRegisterListener);
                    log.info("subscribe a service, ServiceName {} Env {}", service, env);
                }

                serviceList = namingService.getServicesOfServer(++pageNo, pageSize, env).getData();
            }
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 实现对nacos事件的监听 这个事件监听器会在nacos发生事件变化的时候进行回调
     * NamingEvent是一个事件对象，用于表示与服务命名空间（naming）相关的事件
     * NamingEvent的作用是用于监听和处理命名空间中的服务实例（Service Instance）的变化，
     * 以便应用程序可以根据这些变化来动态的更新服务实例列表，以保持与注册中心的同步
     */
    public class NacosRegisterListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            // 先判断是否是注册中心事件
            if (event instanceof NamingEvent) {
                log.info("the triggered event info is :{}", JsonUtil.toJsonString(event));
                NamingEvent namingEvent = (NamingEvent) event;
                // 获取当前变更的服务名
                String serviceName = namingEvent.getServiceName();

                try {
                    // 获取服务定义信息
                    Service service = namingMaintainService.queryService(serviceName, env);
                    ServiceDefinition serviceDefinition = JsonUtil.parse(service.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceDefinition.class);
                    // 获得服务实例信息
                    List<Instance> allInstances = namingService.getAllInstances(service.getName(), env);
                    HashSet<ServiceInstance> set = new HashSet<>();

                    /**
                     * meta-data数据如下
                     * {
                     *   "version": "1.0.0",
                     *   "environment": "production",
                     *   "weight": 80,
                     *   "region": "us-west",
                     *   "labels": "web, primary",
                     *   "description": "Main production service"
                     * }
                     */
                    for (Instance instance : allInstances) {
                        ServiceInstance serviceInstance = JsonUtil.parse(instance.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceInstance.class);
                        set.add(serviceInstance);
                    }

                    // 调用我们自己的监听器
                    // TODO 为啥要这么写 什么作用?
                    registerCenterListenerList.stream().forEach(registerCenterListener -> {
                        registerCenterListener.onChange(serviceDefinition, set);
                    });
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
