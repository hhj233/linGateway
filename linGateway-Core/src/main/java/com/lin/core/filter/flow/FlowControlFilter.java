package com.lin.core.filter.flow;

import com.google.protobuf.DescriptorProtos;
import com.lin.common.config.Rule;
import com.lin.core.context.GatewayContext;
import com.lin.core.filter.Filter;
import com.lin.core.filter.FilterAspect;

import java.util.Iterator;
import java.util.Set;

import static com.lin.common.constant.FilterConst.*;

/**
 * @author linzj
 */
@FilterAspect(id = FLOW_CTL_FILTER_ID,
        name = FLOW_CTL_FILTER_NAME,
        order = FLOW_CTL_FILTER_ORDER)
public class FlowControlFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        Rule rule = ctx.getRule();
        if (rule == null) {
            return;
        }
        // 获取流控规则
        Set<Rule.FlowControllerConfig> flowControllerConfigs = rule.getFlowControllerConfigs();
        Iterator<Rule.FlowControllerConfig> iterator = flowControllerConfigs.iterator();
        Rule.FlowControllerConfig flowControllerConfig;
        while (iterator.hasNext()) {
            GatewayFlowControlRule gatewayFlowControlRule = null;
            flowControllerConfig = iterator.next();
            if (flowControllerConfig == null) {
                continue;
            }
            String path = ctx.getRequest().getPath();
            if (flowControllerConfig.getType().equalsIgnoreCase(FLOW_CTL_TYPE_PATH)
                    && path.equals(flowControllerConfig.getValue())) {

            } else if (flowControllerConfig.getType().equalsIgnoreCase(FLOW_CTL_TYPE_SERVICE)) {
                // TODO 实现服务自己流控
            }
            if (gatewayFlowControlRule != null) {
                //执行流量控制
                gatewayFlowControlRule.doFlowControlFilter(flowControllerConfig, rule.getServiceId());
            }
        }
    }
}
