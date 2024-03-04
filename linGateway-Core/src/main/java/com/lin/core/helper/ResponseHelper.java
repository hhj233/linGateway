package com.lin.core.helper;

import com.lin.common.constant.BasicConst;
import com.lin.common.enums.ResponseCode;
import com.lin.core.context.IContext;
import com.lin.core.response.GatewayResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

import java.util.Objects;

/**
 * @author linzj
 * 响应的辅助类
 */
public class ResponseHelper {

    /**
     * 获取响应对象
     * @param responseCode
     * @return
     */
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponse(responseCode);
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes()));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+ ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        return httpResponse;
    }


    /**
     * 根据上下文和Response对象构建FullHttpResponse
     * @param ctx
     * @param gatewayResponse
     * @return
     */
    private static FullHttpResponse getHttpResponse(IContext ctx, GatewayResponse gatewayResponse) {
        ByteBuf content;
        if (Objects.nonNull(gatewayResponse.getFutureResponse())) {
            content = Unpooled.wrappedBuffer(gatewayResponse.getFutureResponse().getResponseBodyAsByteBuffer());
        } else if (Objects.nonNull(gatewayResponse.getContent())) {
            content = Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes());
        } else {
            content = Unpooled.wrappedBuffer(BasicConst.BLANK_SEPARATOR_1.getBytes());
        }

        if (Objects.isNull(gatewayResponse.getFutureResponse())) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    gatewayResponse.getHttpResponseStatus(), content);
            httpResponse.headers().add(gatewayResponse.getResponseHeaders());
            httpResponse.headers().add(gatewayResponse.getExtraResponseHeaders());
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            return httpResponse;
        } else {
            gatewayResponse.getFutureResponse().getHeaders().add(gatewayResponse.getExtraResponseHeaders());

            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(gatewayResponse.getFutureResponse().getStatusCode()),
                    content);
            httpResponse.headers().add(gatewayResponse.getFutureResponse().getHeaders());
            return httpResponse;
        }
    }

    /**
     * 写回响应信息的方法
     * @param ctx
     */
    public static void writeResponse(IContext ctx) {
        // 释放资源
        ctx.releaseRequest();

        if (ctx.isWritten()){
            // 1. 第一步构建响应对象 并写回数据
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ctx, (GatewayResponse) ctx.getResponse());
            if (!ctx.isKeepAlive()) {
                ctx.getNettyCtx()
                        .writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            }
            // 长连接
            else {
                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.getNettyCtx().writeAndFlush(httpResponse);
            }
            // 设置回写结束状态：COMPLETED
            ctx.completed();
        } else if (ctx.isCompleted()) {
            ctx.invokerCompletedCallback();
        }
    }
}
