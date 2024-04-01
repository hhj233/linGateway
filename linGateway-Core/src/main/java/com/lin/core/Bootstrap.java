package com.lin.core;

import com.lin.common.config.DynamicConfigManager;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;
import com.lin.common.constant.BasicConst;
import com.lin.common.utils.BannerUtil;
import com.lin.common.utils.JsonUtil;
import com.lin.common.utils.NetUtil;
import com.lin.common.utils.TimeUtil;
import com.lin.configCenter.api.ConfigCenter;
import com.lin.registerCenter.api.RegisterCenter;
import com.lin.registerCenter.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        BannerUtil.printBanner(Bootstrap.class);
        // 加载核心网关配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config.getPort());

        // 插件初始化
        // 配置中心管理器初始化 连接配置中心 监听配置的新增、修改、删除
        ServiceLoader<ConfigCenter> serviceLoader = ServiceLoader.load(ConfigCenter.class);
        ConfigCenter configCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found configCenterImpl");
            return new RuntimeException("not found configCenterImpl");
        });

        // 从配置中心获取配置
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRuleChange(rules -> {
            DynamicConfigManager.getInstance().putAllRule(rules);
        });

        // 启动容器
        Container container = new Container(config);
        container.start();

        // 连接注册中心 将注册中心实例加载到本地
        final RegisterCenter registerCenter = registerAndSubscribe(config);

        // 服务优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                registerCenter.deregister(buildGatewayServiceDefinition(config), buildGatewayServiceInstance(config));
                container.shutdown();
            }
        });
    }

    private static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        RegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("registerCenterImpl not found");
            throw new RuntimeException("registerCenterImpl not found");
        });
        registerCenter.init(config.getRegistryAddress(), config.getEnv());

        // 构造网关服务定义和实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);

        // 注册
        registerCenter.register(serviceDefinition, serviceInstance);

        // 订阅
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                log.info("refresh service and instance:{} {}", serviceDefinition.getUniqueId(),
                        JsonUtil.toJsonString(serviceInstanceSet));
                DynamicConfigManager manager = DynamicConfigManager.getInstance();
                // 将这次变更事件影响后的服务实例再次添加到对应的服务实例集合
                manager.addServiceInstance(serviceDefinition.getUniqueId(), serviceInstanceSet);
                // 修改发生对应的服务定义
                manager.putServiceDefinition(serviceDefinition.getUniqueId(), serviceDefinition);
            }
        });
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtil.getLocalIp();
        int port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + BasicConst.COLON_SEPARATOR + port);
        serviceInstance.setPort(port);
        serviceInstance.setIp(localIp);
        serviceInstance.setRegisterTime(TimeUtil.getCurrentTimeMills());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setInvokerMap(Map.of());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }
}
