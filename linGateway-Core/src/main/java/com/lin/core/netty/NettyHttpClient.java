package com.lin.core.netty;

import com.lin.core.Config;
import com.lin.core.LifeCycle;
import com.lin.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;
import java.util.Objects;

/**
 * @author linzj
 * NettyHttpClient 类负责创建和管理基于Netty的异步HTTP客户端。
 * 它实现了LifeCycle接口，以提供初始化、启动和关闭客户端的方法。
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {
    /**
     * 配置信息对象， 包含HTTP客户端的配置参数
     */
    private final Config config;

    /**
     * Netty 事件循环组 用于处理客户端的网络事件
     */
    private final EventLoopGroup eventLoopGroupWorker;

    /**
     * 异步HTTP客户端实例
     *
     */
    private AsyncHttpClient asyncHttpClient;

    public NettyHttpClient(Config config, EventLoopGroup eventLoopGroupWorker) {
        this.config = config;
        this.eventLoopGroupWorker = eventLoopGroupWorker;
        init();
    }

    /**
     * 初始化异步HTTP客户端 设置其配置参数
     */
    @Override
    public void init() {
        // 创建异步HTTP客户端配置
        DefaultAsyncHttpClientConfig defaultAsyncHttpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
                // 使用netty事件循环组
                .setEventLoopGroup(eventLoopGroupWorker)
                // 连接超时设置
                .setConnectTimeout(config.getHttpConnectTimeOut())
                // 请求超时设置
                .setRequestTimeout(config.getHttpRequestTimeOut())
                // 最大重定向次数
                .setMaxRedirects(config.getHttpMaxRequestRetry())
                // 使用池化的ByteBuf分配器以提升性能
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                // 强制压缩
                .setCompressionEnforced(true)
                // 设置最大连接数
                .setMaxConnections(config.getHttpMaxConnections())
                // 设置每台主机最大连接数
                .setMaxConnectionsPerHost(config.getHttpConnectionsPerHost())
                // 连接池空闲连接的超时时间
                .setPooledConnectionIdleTimeout(config.getHttpPooledConnectionIdleTimeOut())
                .build();
        // 根据配置创建异步HTTP客户端
        this.asyncHttpClient = new DefaultAsyncHttpClient(defaultAsyncHttpClientConfig);
    }

    @Override
    public void start() {
        // 使用AsyncHttpHelper单例模式初始化异步Http客户端
        AsyncHttpHelper.getInstance().initialized(this.asyncHttpClient);
    }

    @Override
    public void shutdown() {
        // 如果客户端不为空 则尝试关闭它
        if (Objects.nonNull(this.asyncHttpClient)) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                log.error("nettyHttpClient shutdown error", e);
            }
        }

    }
}
