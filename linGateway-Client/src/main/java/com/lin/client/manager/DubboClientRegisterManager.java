package com.lin.client.manager;

import com.lin.client.api.ApiAnnotationScanner;
import com.lin.client.api.ApiProperties;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;
import com.lin.common.constant.BasicConst;
import com.lin.common.constant.GatewayConst;
import com.lin.common.utils.NetUtil;
import com.lin.common.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author linzj
 */
@Slf4j
public class DubboClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent> {
    private Set<Object> set = new HashSet<>();
    public DubboClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // 这里和mvc有区别 mvc可以直接监听start容器启动事件
        // 而dubbo的导出事件会在spring容器初始化之后执行 所以也可以使用applicationListener去监听
        if (applicationEvent instanceof ServiceBeanExportedEvent) {
            try {
                ServiceBean serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();
                doRegisterDubbo(serviceBean);
            }catch (Exception e){
                log.error("doRegisterDubbo error", e);
                throw new RuntimeException(e);
            }
        } else if (applicationEvent instanceof ApplicationEvent) {
            log.info("dubbo api started");
        }
    }

    /**
     * 实际注册方法
     * @param serviceBean 包含服务的接口、实现类、版本、分组、协议、注册中心等配置
     */
    private void doRegisterDubbo(ServiceBean serviceBean) {
        // 拿到真正的bean对象
        Object bean = serviceBean.getRef();
        if (set.contains(bean)) {
            return;
        }

        ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean, serviceBean);

        if (Objects.isNull(serviceDefinition)) {
            return;
        }

        serviceDefinition.setEnvType(getApiProperties().getEnv());

        // 创建服务实例
        ServiceInstance serviceInstance = new ServiceInstance();
        String localIp = NetUtil.getLocalIp();
        int port = serviceBean.getProtocol().getPort();
        String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        serviceInstance.setVersion(serviceDefinition.getVersion());
        serviceInstance.setUniqueId(serviceDefinition.getUniqueId());
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setGray(getApiProperties().isGray());
        serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);
        serviceInstance.setRegisterTime(TimeUtil.getCurrentTimeMills());

        register(serviceDefinition, serviceInstance);
    }
}
