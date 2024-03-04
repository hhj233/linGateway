package com.lin.core.context;

import com.lin.common.config.Rule;
import com.lin.common.utils.AssertUtil;
import com.lin.core.request.GatewayRequest;
import com.lin.core.response.GatewayResponse;
import io.micrometer.core.instrument.Timer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linzj
 */
@Slf4j
public class GatewayContext extends BasicContext{
    private GatewayRequest request;
    private GatewayResponse response;
    private Rule rule;
    private int currentRetryTimes;
    @Getter
    @Setter
    private boolean gray;

    /**
     * 记录应用程序中的方法调用和服务请求所花费的时间
     */
    @Getter
    @Setter
    private Timer.Sample timerSample;


    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                          GatewayRequest request, Rule rule, int currentRetryTimes) {
        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
        this.currentRetryTimes = currentRetryTimes;
    }

    public static class Builder {
        private String protocol;
        private ChannelHandlerContext nettyCtx;
        private boolean keepAlive;
        private GatewayRequest request;
        private Rule rule;

        public Builder() {

        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }
        public Builder setRequest(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(protocol, "protocol 不能为空");
            AssertUtil.notNull(nettyCtx, "nettyCtx 不能为空");
            AssertUtil.notNull(request, "request 不能为空");
            AssertUtil.notNull(rule, "rule不能为空");
            return new GatewayContext(protocol, nettyCtx,keepAlive,request, rule,0);
        }
    }

    @Override
    public GatewayRequest getRequest() {
        return request;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    @Override
    public GatewayResponse getResponse() {
        return response;
    }

    public void setResponse(GatewayResponse response) {
        this.response = response;
    }

    @Override
    public Rule getRule() {
        return this.rule;
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public int getCurrentRetryTimes() {
        return currentRetryTimes;
    }

    public void setCurrentRetryTimes(int currentRetryTimes) {
        this.currentRetryTimes = currentRetryTimes;
    }

    /**
     * 根据过滤器ID获取对应的过滤器配置信息
     * @param filterId
     * @return
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    public String getUniqueId() {
        return request.getUniqueId();
    }

    /**
     * 重写父类释放资源方法，用于正在释放资源
     * release()方法通过减少对象的引用计数。当计数达到零时，资源被释放
     */
    public void releaseRequest() {
        if (requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
    }

    /**
     * 获取原始的请求对象
     * @return
     */
    public GatewayRequest getOriginRequest() {
        return request;
    }
}
