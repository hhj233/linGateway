package com.lin.core.netty;

import com.lin.core.context.HttpRequestWrapper;
import com.lin.core.netty.processor.NettyCoreProcessor;
import com.lin.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author linzj
 * nettyHttpServerHandler用于处理通过netty传入的Http请求
 * 继承channelInboundHandlerAdapter 这样可以覆盖回调方法来处理入站事件
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 成员变量，用来处理具体的业务逻辑
     */
    private final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }


    /**
     * 当从客户端接收到数据 该方法会被调用
     * 这里将入站的数据(http请求)包装后 传给业务逻辑处理器
     * @param ctx
     * @param msg 预期接受一个FullHttpRequest
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 将接受的消息转为 FullHttpRequest 对象
        FullHttpRequest request = (FullHttpRequest) msg;
        // 构建网关请求对象
        HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
        httpRequestWrapper.setFullHttpRequest(request);
        httpRequestWrapper.setNettyCtx(ctx);
        nettyProcessor.process(httpRequestWrapper);
    }

    /**
     * 处理入站的异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        // TODO 记录异常动作
        System.out.println("exceptionCaught");
    }
}
