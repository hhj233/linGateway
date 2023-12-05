package com.lin.common.config;

/**
 * 服务调用的接口模型秒描述
 */
public interface ServiceInvoker {
    /**
     * 获取真正的服务调用全路径
     * @return
     */
    String getInvokerPath();

    /**
     * 设置服务调用全路径
     * @param invokerPath
     */
    void setInvokerPath(String invokerPath);
    /**
     * 获取该服务调用的超时时间
     */
    int getTimeout();
    /**
     * 设置服务调用的超时时间
     */
    void setTimeout(int timeout);
}
