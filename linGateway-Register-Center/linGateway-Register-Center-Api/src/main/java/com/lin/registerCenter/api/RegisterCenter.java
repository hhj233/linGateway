package com.lin.registerCenter.api;

import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;

/**
 * 注册中心接口方法
 * 用于提供抽象的注册中心接口
 * @author linzj8
 */
public interface RegisterCenter {
    /**
     * 初始化
     * @param registerAddress 注册中心地址
     * @param env 要注册到的环境
     */
    void init(String registerAddress, String env);

    /**
     * 注册服务
     * @param serviceDefinition 服务定义信息
     * @param serviceInstance 服务实例信息
     */
    void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 注销下线
     * @param serviceDefinition
     * @param serviceInstance
     */
    void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 订阅所有服务变更
     * @param registerCenterListener
     */
    void subscribeAllServices(RegisterCenterListener registerCenterListener);
}
