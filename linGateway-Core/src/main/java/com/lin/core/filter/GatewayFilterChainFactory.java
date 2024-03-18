package com.lin.core.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lin.common.config.Rule;
import com.lin.common.constant.FilterConst;
import com.lin.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author linzj
 */
@Slf4j
public class GatewayFilterChainFactory implements FilterChainFactory{
    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }

    /**
     * 使用caffeine缓存 并且设置过期时间10分钟
     */
    private Cache<String,GatewayFilterChain> chainCache = Caffeine.newBuilder().recordStats()
            .expireAfterWrite(10, TimeUnit.MINUTES).build();

    /**
     * 过滤器存储映射 过滤器id-过滤器
     */
    private Map<String,Filter> processFilterMap = new ConcurrentHashMap<>();

    public GatewayFilterChainFactory() {
        ServiceLoader<Filter> serviceLoader = ServiceLoader.load(Filter.class);
        serviceLoader.stream().forEach(filterProvider -> {
            Filter filter = filterProvider.get();
            FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            log.info("load filter success:{},{},{},{}",filter.getClass(),annotation.id(),annotation.name(),
                    annotation.order());
            if (annotation != null) {
                //添加到过滤器集合
                String filterId = annotation.id();
                if (StringUtils.isEmpty(filterId)) {
                    filterId = filter.getClass().getName();
                }
                processFilterMap.put(filterId, filter);
            }
        });
    }

    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception {
        return chainCache.get(ctx.getRule().getId(),k->doBuildFilterChain(ctx.getRule()));
    }

    public GatewayFilterChain doBuildFilterChain(Rule rule) {
        GatewayFilterChain chain = new GatewayFilterChain();
        List<Filter> filters = new ArrayList<>();
        // 手动将某些过滤器链加入过滤器链中
        /*filters.add(getFilterInfo(FilterConst.GRAY_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.MOCK_FILTER_ID));*/
        if (rule != null ){
            Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigs();
            Iterator<Rule.FilterConfig> iterator = filterConfigs.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()) {
                filterConfig = iterator.next();
                if (filters == null){
                    continue;
                }
                String filterConfigId = filterConfig.getId();
                if (StringUtils.isNotEmpty(filterConfigId) && getFilterInfo(filterConfigId)!=null) {
                    Filter filter = getFilterInfo(filterConfigId);
                    filters.add(filter);
                }
            }
        }
        // 添加路由过滤器-这是最后一步
        filters.add(getFilterInfo(FilterConst.ROUTER_FILTER_ID));
        // 排序
        filters.sort(Comparator.comparing(Filter::getOrder));
        chain.addFilterList(filters);
        return chain;
    }

    @Override
    public Filter getFilterInfo(String filterId) {
        return processFilterMap.get(filterId);
    }
}
