package com.lin.core.filter.loadBalance;

import com.lin.common.config.ServiceInstance;

/**
 * @author linzj
 */
public interface LoadBalanceRule {
    /**
     * 获取服务实例
     * @param serviceId
     * @param gray
     * @return
     */
    ServiceInstance choose(String serviceId, boolean gray);
}
