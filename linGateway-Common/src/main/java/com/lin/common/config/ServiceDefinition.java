package com.lin.common.config;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 资源服务定义类，无论下游是什么服务都需要进行注册
 */
public class ServiceDefinition implements Serializable {
    /**
     * 唯一服务号id serviceId:version
     */
    private String uniqueId;
    /**
     * 服务id
     */
    private String serviceId;
    /**
     * 版本
     */
    private String version;
    /**
     * 服务的具体协议:http\dubbo ..
     */
    private String protocol;
    /**
     * 路由匹配规则
     */
    private String patternPath;
    /**
     * 环境名称
     */
    private String envType;
    /**
     * 服务启用禁用
     */
    private boolean enable = true;
    /**
     * 服务列表信息
     */
    private Map<String,ServiceInvoker> invokerMap;

    public ServiceDefinition() {
        super();
    }

    public ServiceDefinition(String uniqueId, String serviceId, String version, String protocol, String patternPath, String envType, boolean enable, Map<String, ServiceInvoker> invokerMap) {
        super();
        this.uniqueId = uniqueId;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.patternPath = patternPath;
        this.envType = envType;
        this.enable = enable;
        this.invokerMap = invokerMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this == null || getClass() != obj.getClass()) {
            return false;
        }
        ServiceDefinition serviceDefinition = (ServiceDefinition) obj;
        return Objects.equals(this.uniqueId, serviceDefinition.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPatternPath() {
        return patternPath;
    }

    public void setPatternPath(String patternPath) {
        this.patternPath = patternPath;
    }

    public String getEnvType() {
        return envType;
    }

    public void setEnvType(String envType) {
        this.envType = envType;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Map<String, ServiceInvoker> getInvokerMap() {
        return invokerMap;
    }

    public void setInvokerMap(Map<String, ServiceInvoker> invokerMap) {
        this.invokerMap = invokerMap;
    }
}
