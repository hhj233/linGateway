package com.lin.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @author linzj
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest fullHttpRequest;
    private ChannelHandlerContext nettyCtx;
}
