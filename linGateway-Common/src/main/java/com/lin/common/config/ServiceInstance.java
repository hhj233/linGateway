package com.lin.common.config;

import java.io.Serializable;
import java.util.Objects;

/**
 * 一个服务定义会对应多个服务实例
 */
public class ServiceInstance implements Serializable {

    /**
     * 服务实例id ip:port
     */
    protected String serviceInstanceId;
    /**
     * 服务定义唯一id
     */
    protected String uniqueId;
    /**
     * 服务实例地址
     */
    protected String ip;

    /**
     * 端口号
     */
    protected int port;
    /**
     * 标签信息
     */
    protected String tags;

    /**
     * 权重信息
     */
    protected Integer weight;

    /**
     * 服务注册时间戳
     */
    protected long registerTime;

    /**
     * 服务实例启用禁用
     */
    protected boolean enable = true;

    /**
     * 服务实例是否是灰度
     */
    protected boolean gray;

    public ServiceInstance() {
        super();
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isGray() {
        return gray;
    }

    public void setGray(boolean gray) {
        this.gray = gray;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this == null || getClass() != obj.getClass()) {
            return false;
        }
        ServiceInstance serviceInstance = (ServiceInstance) obj;
        return Objects.equals(serviceInstanceId, serviceInstance.serviceInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId);
    }
}
