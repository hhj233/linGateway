package com.lin.core.filter;

import com.lin.core.context.GatewayContext;

/**
 * @author linzj
 * 过滤器链工厂 用于生成过滤器链
 */
public interface FilterChainFactory {
    /**
     * 构建过滤器链
     * @param ctx
     * @return
     * @throws Exception
     */
    GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception;

    /**
     * 根据过滤器id获取过滤器
     * @param filterId
     * @return
     * @param <T>
     * @throws Exception
     */
    <T> T getFilterInfo(String filterId) throws Exception;
}
