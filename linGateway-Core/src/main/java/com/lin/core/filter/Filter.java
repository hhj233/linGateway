package com.lin.core.filter;

import com.lin.core.context.GatewayContext;

/**
 * @author linzj
 * 过滤器顶层接口
 */
public interface Filter {
    void doFilter(GatewayContext ctx) throws Exception;

    default int getOrder() {
        FilterAspect filterAspect = this.getClass().getAnnotation(FilterAspect.class);
        if (filterAspect != null) {
            return filterAspect.order();
        }
        return Integer.MAX_VALUE;
    }
}
