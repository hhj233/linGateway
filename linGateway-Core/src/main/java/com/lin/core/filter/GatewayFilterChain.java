package com.lin.core.filter;

import com.lin.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linzj
 */
@Slf4j
public class GatewayFilterChain {
    List<Filter> filters = new ArrayList<>();

    public GatewayFilterChain addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }
    public GatewayFilterChain addFilterList(List<Filter> filters) {
        this.filters.addAll(filters);
        return this;
    }

    /**
     * 执行过滤器链处理过程
     * @param ctx
     * @return
     * @throws Exception
     */
    public GatewayContext doFilter(GatewayContext ctx) throws Exception{
        if (filters.isEmpty()) {
            return ctx;
        }
        try {
            for (Filter filter : filters) {
                filter.doFilter(ctx);
                if (ctx.isTerminated()) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("执行过滤器发生异常，异常信息：{}",e.getMessage());
            throw e;
        }
        return ctx;
    }
}
