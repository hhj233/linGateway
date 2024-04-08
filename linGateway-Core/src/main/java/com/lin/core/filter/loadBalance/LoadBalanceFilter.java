package com.lin.core.filter.loadBalance;

import com.lin.common.config.Rule;
import com.lin.common.config.ServiceInstance;
import com.lin.common.enums.ResponseCode;
import com.lin.common.exception.NotFoundException;
import com.lin.common.utils.JsonUtil;
import com.lin.core.context.GatewayContext;
import com.lin.core.filter.Filter;
import com.lin.core.filter.FilterAspect;
import com.lin.core.request.GatewayRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.lin.common.constant.FilterConst.*;

/**
 * @author linzj
 */
@FilterAspect(id = LOAD_BALANCE_FILTER_ID,
        name = LOAD_BALANCE_FILTER_NAME,
        order = LOAD_BALANCE_FILTER_ORDER)
@Slf4j
public class LoadBalanceFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        String serviceId = ctx.getUniqueId();
        LoadBalanceRule loadBalanceRule = getLoadBalanceRule(ctx);
        ServiceInstance serviceInstance = loadBalanceRule.choose(serviceId, ctx.isGray());
        System.out.println("IP为" + serviceInstance.getIp() + ",端口号：" + serviceInstance.getPort());
        GatewayRequest request = ctx.getRequest();
        if (Objects.nonNull(serviceInstance) && Objects.nonNull(request)) {
            StringBuilder host = new StringBuilder(serviceInstance.getIp()).append(":").append(serviceInstance.getPort());
            request.setModifyHost(host.toString());
        } else {
            log.error("no instance available for :{}", serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    private LoadBalanceRule getLoadBalanceRule(GatewayContext ctx) {
        LoadBalanceRule loadBalanceRule = null;
        Rule rule = ctx.getRule();
        if (Objects.isNull(rule)) {
            return LoadBalanceRandomRule.getInstance(ctx.getUniqueId());
        }
        Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigs();
        Iterator<Rule.FilterConfig> iterator = filterConfigs.iterator();
        Rule.FilterConfig loadBalanceConfig = null;
        while (iterator.hasNext()) {
            Rule.FilterConfig filterConfig = iterator.next();
            if (Objects.isNull(filterConfig) || !LOAD_BALANCE_FILTER_ID.equals(filterConfig.getId())) {
                continue;
            }
            loadBalanceConfig = filterConfig;
            break;
        }
        String strategy = LOAD_BALANCE_STRATEGY_RANDOM;
        String config = loadBalanceConfig.getConfig();
        if (StringUtils.isNotBlank(config)) {
            Map<String, String> loadBalanceConfigMap = JsonUtil.parse(config, Map.class);
            strategy = loadBalanceConfigMap.getOrDefault(LOAD_BALANCE_KEY, strategy);
        }
        switch (strategy) {
            case LOAD_BALANCE_STRATEGY_RANDOM -> {
                loadBalanceRule = LoadBalanceRandomRule.getInstance(rule.getServiceId());
                break;
            }
            case LOAD_BALANCE_STRATEGY_ROUND_ROBIN -> {
                loadBalanceRule = LoadBalanceRoundRobinRule.getInstance(rule.getServiceId());
                break;
            }
            default -> {
                log.warn("not found loadBalanceConfig for {}", rule.getServiceId());
                loadBalanceRule = LoadBalanceRandomRule.getInstance(rule.getServiceId());
            }
        }
        return loadBalanceRule;
    }
}
