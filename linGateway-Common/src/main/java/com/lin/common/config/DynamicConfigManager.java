package com.lin.common.config;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author linzj
 * 动态服务缓存配置管理类 用于缓存从配置中心拉取下来的配置
 */

@Data
public class DynamicConfigManager {
    /**
     * 服务定义集合
     */
    private ConcurrentHashMap<String, ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 服务实例集合
     */
    private ConcurrentHashMap<String, Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();

    /**
     * 规则集合
     */
    private ConcurrentHashMap<String, Rule> ruleMap = new ConcurrentHashMap<>();

    /**
     * 路径及规则集合
     */
    private ConcurrentHashMap<String, Rule> pathRuleMap = new ConcurrentHashMap<>();

    /**
     * 服务规则集合
     */
    private ConcurrentHashMap<String, List<Rule>> serivceRuleMap = new ConcurrentHashMap<>();

    public static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }

    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void putServiceDefinition(String uniqueId, ServiceDefinition serviceDefinition) {
        serviceDefinitionMap.put(uniqueId, serviceDefinition);
    }

    public ServiceDefinition getServiceDefinition(String uniqueId) {
        return serviceDefinitionMap.get(uniqueId);
    }

    public void removeServiceDefinition(String uniqueId) {
        serviceDefinitionMap.remove(uniqueId);
    }

    /**对服务实例进行缓存的方法**/

    public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> serviceInstanceSet = serviceInstanceMap.get(uniqueId);
        serviceInstanceSet.add(serviceInstance);
    }

    public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
        serviceInstanceMap.put(uniqueId, serviceInstanceSet);
    }

    public Set<ServiceInstance> getServiceInstanceByUniqueId(String uniqueId, boolean gray) {
        Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        if (CollectionUtils.isEmpty(serviceInstances)) {
            return Collections.emptySet();
        }
        // 不为空为灰度流量
        if (gray) {
            return serviceInstances.stream()
                    .filter(ServiceInstance::isGray)
                    .collect(Collectors.toSet());
        }

        return serviceInstances;
    }

    public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        removeServiceInstance(uniqueId, serviceInstance);
        Set<ServiceInstance> serviceInstanceSet = serviceInstanceMap.get(uniqueId);
        serviceInstanceSet.add(serviceInstance);
    }

    public void removeServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> serviceInstanceSet = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> iterator = serviceInstanceSet.iterator();
        while (iterator.hasNext()) {
            ServiceInstance instance = iterator.next();
            if (instance.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeServiceInstance(String uniqueId) {
        serviceInstanceMap.remove(uniqueId);
    }

    /**对规则进行缓存的方法**/

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    public void putAllRule(List<Rule> ruleList) {
        ConcurrentHashMap<String, Rule> newRuleMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Rule> newPathMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, List<Rule>> newServiceMap = new ConcurrentHashMap<>();
        for(Rule rule : ruleList) {
            newRuleMap.put(rule.getId(), rule);

            List<Rule> rules = newServiceMap.get(rule.getServiceId());
            if (CollectionUtils.isEmpty(rules)) {
                rules = new ArrayList<>();
            }
            rules.add(rule);
            newServiceMap.put(rule.getServiceId(), rules);

            List<String> paths = rule.getPaths();
            for (String path : paths) {
                String key = String.format("%s.%s", rule.getServiceId(), path);
                newPathMap.put(key, rule);
            }
        }

        ruleMap = newRuleMap;
        pathRuleMap = newPathMap;
        serivceRuleMap = newServiceMap;
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    public Rule  getRuleByPath(String path){
        return pathRuleMap.get(path);
    }

    public List<Rule>  getRuleByServiceId(String serviceId){
        return serivceRuleMap.get(serviceId);
    }


}
