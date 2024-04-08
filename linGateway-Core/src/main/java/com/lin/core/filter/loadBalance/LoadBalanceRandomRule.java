package com.lin.core.filter.loadBalance;

import com.lin.common.config.DynamicConfigManager;
import com.lin.common.config.ServiceInstance;
import com.lin.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.lin.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * @author linzj
 */
@Slf4j
public class LoadBalanceRandomRule implements LoadBalanceRule{

    private final String serviceId;

    private static Map<String, LoadBalanceRandomRule> serviceMap = new ConcurrentHashMap<>();

    public LoadBalanceRandomRule(String serviceId) {
        this.serviceId = serviceId;
    }

    public static LoadBalanceRandomRule getInstance(String serviceId) {
        LoadBalanceRandomRule loadBalanceRandomRule = serviceMap.get(serviceId);
        if (Objects.isNull(loadBalanceRandomRule)) {
            loadBalanceRandomRule = new LoadBalanceRandomRule(serviceId);
            serviceMap.put(serviceId, loadBalanceRandomRule);
        }
        return loadBalanceRandomRule;
    }

    @Override
    public ServiceInstance choose(String serviceId, boolean gray) {
        Set<ServiceInstance> instanceSet = DynamicConfigManager.getInstance()
                .getServiceInstanceByUniqueId(serviceId, gray);
        if (CollectionUtils.isEmpty(instanceSet)) {
            log.error("no service instance available for :{}", serviceId);
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
        ArrayList<ServiceInstance> instances = new ArrayList<>(instanceSet);
        int idx = ThreadLocalRandom.current().nextInt(instances.size());
        return instances.get(idx);
    }
}
