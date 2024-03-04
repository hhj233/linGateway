package com.lin.core.filter.flow;

import com.lin.common.config.Rule;

/**
 * @author Administrator
 * 网络流控规则接口
 */
public interface GatewayFlowControlRule {
    /**
     * 执行流控规则过滤器
     * @param flowControllerConfig
     * @param serviceId
     */
    void doFlowControlFilter(Rule.FlowControllerConfig flowControllerConfig, String serviceId);
}
