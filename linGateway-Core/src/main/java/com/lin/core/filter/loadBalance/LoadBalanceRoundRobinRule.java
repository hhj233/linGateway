package com.lin.core.filter.loadBalance;

import com.lin.common.config.DynamicConfigManager;
import com.lin.common.config.ServiceInstance;
import com.lin.common.enums.ResponseCode;
import com.lin.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linzj
 */
@Slf4j
public class LoadBalanceRoundRobinRule implements LoadBalanceRule{
    private final AtomicInteger position = new AtomicInteger(1);
    private final String serviceId;
    private static Map<String, LoadBalanceRoundRobinRule> serviceMap = new ConcurrentHashMap<>();

    private LoadBalanceRoundRobinRule(String serviceId) {
        this.serviceId = serviceId;
    }

    public static LoadBalanceRoundRobinRule getInstance(String serviceId) {
        LoadBalanceRoundRobinRule roundRobinRule = serviceMap.get(serviceId);
        if(Objects.isNull(roundRobinRule)) {
            roundRobinRule = new LoadBalanceRoundRobinRule(serviceId);
        }
        return roundRobinRule;
    }

    @Override
    public ServiceInstance choose(String serviceId, boolean gray) {
        Set<ServiceInstance> instanceSet = DynamicConfigManager.getInstance()
                .getServiceInstanceByUniqueId(serviceId, gray);
        if (CollectionUtils.isEmpty(instanceSet)) {
            log.error("no service instance available for:{}",serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
        ArrayList<ServiceInstance> serviceInstances = new ArrayList<>(instanceSet);
        int pos = Math.abs(this.position.incrementAndGet());
        return serviceInstances.get(pos % serviceInstances.size());
    }
}
