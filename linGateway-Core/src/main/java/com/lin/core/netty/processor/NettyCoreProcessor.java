package com.lin.core.netty.processor;

import com.lin.common.enums.ResponseCode;
import com.lin.common.exception.BasicException;
import com.lin.core.context.GatewayContext;
import com.lin.core.context.HttpRequestWrapper;
import com.lin.core.filter.FilterChainFactory;
import com.lin.core.filter.GatewayFilterChainFactory;
import com.lin.core.helper.RequestHelper;
import com.lin.core.helper.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linzj
 * NettyCoreProcessor 是负责基于 Netty 服务器中处理 HTTP 请求的组件
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor{
    /**
     * FilterChainFactory 负责创建和管理过滤器的执行
     */
    private FilterChainFactory filterChainFactory = GatewayFilterChainFactory.getInstance();

    /**
     * 处理传入的 HTTP 请求
     * @param requestWrapper 包含FullHttpRequest 和 ChannelHandlerContext 的 httpRequestWrapper。
     */
    @Override
    public void process(HttpRequestWrapper requestWrapper) {
        FullHttpRequest request = requestWrapper.getFullHttpRequest();
        ChannelHandlerContext ctx = requestWrapper.getNettyCtx();

        try {
            // 创建并填充gatewayContext 以保存有关传入请求的信息
            GatewayContext gatewayContext = RequestHelper.doContext(request, ctx);

            // 在gatewayContext链上执行过滤器链逻辑
            filterChainFactory.buildFilterChain(gatewayContext).doFilter(gatewayContext);
        } catch (BasicException e) {
            // 通过记录日志并发送适当的 HTTP 响应处理已知异常
            log.error("处理错误{} {}", e.getCode().getCode(), e.getCode().getMessage());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(ctx, request, httpResponse);
        } catch (Throwable e) {
            log.error("处理未知错误",e);
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(ctx, request, httpResponse);
        }
    }

    public void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        ctx.writeAndFlush(response)
                // 发送响应后关闭资源
                .addListener(ChannelFutureListener.CLOSE);
        // 释放与请求相关的资源
        ReferenceCountUtil.release(request);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
