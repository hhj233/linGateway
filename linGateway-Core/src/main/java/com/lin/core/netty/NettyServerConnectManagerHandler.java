package com.lin.core.netty;

import com.lin.common.utils.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linzj
 */
@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    /**
     * 当通道注册到他的EventLoop时调用，即它可以开始处理IO事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String address = RemotingUtil.parseChannelRemoteAddress(ctx.channel());
        log.debug("NETTY SERVER PIPELINE:channelRegistered:{}", address);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String address = RemotingUtil.parseChannelRemoteAddress(ctx.channel());
        log.debug("NETTY SERVER PIPELINE:channelUnregistered:{}", address);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String address = RemotingUtil.parseChannelRemoteAddress(ctx.channel());
        log.debug("NETTY SERVER PIPELINE:channelActive:{}", address);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String address = RemotingUtil.parseChannelRemoteAddress(ctx.channel());
        log.debug("NETTY SERVER PIPELINE:channelActive:{}", address);
        super.channelInactive(ctx);
    }


    /**
     * 用户自定义事件被触发时调用，可以用来处理空闲状态检测事件
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state().equals(IdleState.ALL_IDLE)) {
                final String address = RemotingUtil.parseChannelRemoteAddress(ctx.channel());
                log.debug("NETTY SERVER PIPELINE: remoteAddr: {}", address);
                ctx.channel().close();
            }
        }
        // 传递事件给下一个channelHandler
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String address = RemotingUtil.parseChannelRemoteAddress(ctx.channel());
        log.warn("NETTY SERVER PIPELINE: remoteAddr： {}, exceptionCaught {}", address, cause);
        super.exceptionCaught(ctx, cause);
    }
}
