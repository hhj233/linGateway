package com.lin.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author linzj
 * 规则类
 */
@Data
@AllArgsConstructor
@Builder
public class Rule implements Comparable<Rule>,Serializable {
    /**
     * 规则id，全局唯一
     */
    private String id;

    /**
     * 规则名
     */
    private String name;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 服务id
     */
    private String serviceId;

    /**
     * 请求前缀
     */
    private String prefix;

    /**
     * 路径集合
     */
    private List<String> paths;

    /**
     * 规则排序，对应场景，一条路径对应多条规则，然后只执行一条规则的情况
     */
    private Integer order;

    /**
     * 过滤器配置
     */
    private Set<FilterConfig> filterConfigs = new HashSet();

    /**
     * 限流规则
     */
    private Set<FlowControllerConfig> flowControllerConfigs = new HashSet<>();

    /**
     * 重试规则
     */
    private RetryConfig retryConfig;

    /**
     * 熔断规则
     */
    private Set<HystrixConfig> hystrixConfigs = new HashSet<>();

    public Rule() {
        super();
    }

    @Override
    public int compareTo(Rule rule) {
        int orderCompare = Integer.compare(this.order, rule.getOrder());
        if (orderCompare == 0) {
            return getId().compareTo(rule.getId());
        }
        return orderCompare;
    }



    @Data
    public static class FilterConfig {
        /**
         * 过滤器唯一id
         */
        private String id;

        /**
         * 过滤器规则描述{"timeOut":500,"balance": random}
         */
        private String config;


        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || getClass() != obj.getClass()) {
                return false;
            }
            FilterConfig that = (FilterConfig) obj;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    /**
     * 限流配置类
     */
    @Data
    public static class FlowControllerConfig {
        /**
         * 限流类型-可能是path,也可能是ip或者服务
         */
        private String type;
        /**
         * 限流对象
         */
        private String value;
        /**
         * 限流模式，单机，集群
         */
        private String model;

        /**
         * 限流规则，json存储
         */
        private String config;
    }

    @Data
    public static class RetryConfig {
        private int times;
    }

    @Data
    public static class HystrixConfig {
        /**
         * 熔断降级路径
         */
        private String path;

        /**
         * 超时时间
         */
        private int timeoutInMilliseconds;

        /**
         * 核心线程
         */
        private int threadCoreSize;

        /**
         * 降级响应
         */
        private String fallbackResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || getClass() != o.getClass()) {
            return false;
        }

        FilterConfig that = (FilterConfig) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
