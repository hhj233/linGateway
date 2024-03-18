package com.lin.core.netty;

import com.lin.common.utils.RemotingUtil;
import com.lin.core.Config;
import com.lin.core.LifeCycle;
import com.lin.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author linzj
 * netty server端实现
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {
    /**
     * 服务器配置对象，用于获取如端口号等配置信息
     */
    private final Config config;

    /**
     * 自定义的netty处理器接口，用于定义如何处理接收到的请求
     */
    private final NettyProcessor nettyProcessor;

    /**
     * 服务器引导类 用于配置和启动netty服务
     */
    private ServerBootstrap serverBootstrap;

    /**
     * boss线程组，用于处理新的客户端链接
     */
    private EventLoopGroup eventLoopGroupBoss;

    /**
     * 工作线程组 用于处理已经建立链接的后续操作
     */
    @Getter
    private EventLoopGroup eventLoopGroupWorker;

    public NettyHttpServer(Config config, NettyProcessor nettyProcessor) {
        this.config = config;
        this.nettyProcessor = nettyProcessor;
        this.init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        // 判断是否使用epoll模型 linux系统下的高性能网络通信模型
        if (useEpollCheck()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("epoll-netty-boss-nio"));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("epoll-netty-worker-nio"));
        } else {
            // 使用默认的NIO模型
            this.eventLoopGroupBoss = new NioEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("default-netty-boss-nio"));
            this.eventLoopGroupWorker = new NioEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("default-netty-worker-nio"));
        }
    }

    public boolean useEpollCheck() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        // 配置服务器参数 如端口、tcp参数等
        this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWorker)
                .channel(useEpollCheck() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // TCP 允许的最大队列长度
                .option(ChannelOption.SO_BACKLOG, 1024)
                // TCP 允许端口重用
                .option(ChannelOption.SO_REUSEADDR, true)
                // 保持长连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 禁用nagle算法 适用于小数据即时传输
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 设置发送缓存区大小
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                // 设置接收缓存区大小
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                // 设置接口监听
                .localAddress(new InetSocketAddress(config.getPort()))
                // 定义处理新连接的管道初始化逻辑
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        // 配置管道的编解码器和自定义处理器
                        channel.pipeline().addLast(
                                // http编解码器
                                new HttpServerCodec(),
                                // 聚合http请求
                                new HttpObjectAggregator(config.getMaxContextLength()),
                                // 处理http 100 continue请求
                                new HttpServerExpectContinueHandler(),
                                // 自定义处理器
                                new NettyHttpServerHandler(nettyProcessor),
                                // 连接管理处理器
                                new NettyServerConnectManagerHandler()
                        );
                    }
                });

        // 绑定接口并启动服务
        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}", config.getPort());
        } catch (InterruptedException e) {
            throw new RuntimeException("启动服务器异常",e);
        }
    }

    /**
     * 关闭netty服务器 释放资源
     */
    @Override
    public void shutdown() {
        if (this.eventLoopGroupBoss != null) {
            this.eventLoopGroupBoss.shutdownGracefully();
        }
        if (this.eventLoopGroupWorker != null) {
            this.eventLoopGroupWorker.shutdownGracefully();
        }
    }
}
