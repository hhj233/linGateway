package com.lin.core;

import com.lin.core.netty.NettyHttpClient;
import com.lin.core.netty.NettyHttpServer;
import com.lin.core.netty.processor.DisruptorNettyCoreProcessor;
import com.lin.core.netty.processor.NettyCoreProcessor;
import com.lin.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

import static com.lin.common.constant.GatewayConst.BUFFER_TYPE_PARALLEL;

/**
 * @author linzj
 */
@Slf4j
public class Container implements LifeCycle{
    private final Config config;
    private NettyHttpServer nettyHttpServer;
    private NettyHttpClient nettyHttpClient;
    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        this.init();
    }

    @Override
    public void init() {
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        // 如果启动要使用多生产者多消费者 读取配置
        if (BUFFER_TYPE_PARALLEL.equals(config.getParallelBufferType())) {
            // 开启的情况下使用DisruptorNettyCoreProcessor
            nettyProcessor = new DisruptorNettyCoreProcessor(config, nettyCoreProcessor);
        } else {
            nettyProcessor = nettyCoreProcessor;
        }
        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);
        this.nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getEventLoopGroupWorker());
    }

    @Override
    public void start() {
        this.nettyProcessor.start();
        this.nettyHttpServer.start();
        this.nettyHttpClient.start();
        log.info("lin gateway start!");
    }

    @Override

    public void shutdown() {
        this.nettyProcessor.shutdown();
        this.nettyHttpServer.shutdown();
        this.nettyHttpClient.shutdown();
    }
}
