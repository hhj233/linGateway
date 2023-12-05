package com.lin.registerCenterApi;

import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;

import java.util.Set;

/**
 * 注册中心的监听器，用来监听注册中心的一些变化
 */
public interface RegisterCenterListener {
    void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet);
}
