package com.lin.client.manager;

import com.lin.client.api.ApiProperties;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;
import com.lin.registerCenter.api.RegisterCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * @author linzj
 * 抽象服务注册管理类
 */
@Slf4j
public class AbstractClientRegisterManager {
    @Getter
    private ApiProperties apiProperties;
    private RegisterCenter registerCenter;

    protected AbstractClientRegisterManager(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;

        // 初始化注册中心对象
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found registerCenter impl");
            throw new RuntimeException("not found registerCenter impl");
        });
        // 注册中心初始化代码
        registerCenter.init(apiProperties.getRegisterAddress(), apiProperties.getEnv());
    }

    /**
     * 提供给子类进行服务注册
     * @param serviceDefinition 服务定义
     * @param serviceInstance   服务实例
     */
    protected void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        registerCenter.register(serviceDefinition, serviceInstance);
    }

}
