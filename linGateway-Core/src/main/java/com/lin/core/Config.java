package com.lin.core;

import com.lmax.disruptor.*;
import lombok.Data;

/**
 * config配置类
 */
@Data
public class Config {
    private int port = 8088;
    private int prometheusPort = 18000;
    private String applicationName = "lin-gateway";
    private String registryAddress = "127.0.0.1:8848";
    private String env = "dev";
    // netty
    private int eventLoopGroupBossNum = 1;
    private int eventLoopGroupWorkerNum = 1;
    private int maxContextLength = 64*1024*1024;
    // 默认单异步模式
    private boolean whenComplete = true;
    // http Async默认参数
    // 连接超时时间
    private int httpConnectTimeOut = 30 * 1000;
    // 请求超时时间
    private int httpRequestTimeOut = 30 * 1000;
    // 客户端请求重试次数
    private int httpMaxRequestRetry = 2;
    // 客户端最大请求数
    private int httpMaxConnections = 10000;
    // 客户端每个地址支持最大连接数
    private int httpConnectionsPerHost = 8000;
    // 客户端空闲超时时间
    private int httpPooledConnectionIdleTimeOut = 60 * 1000;
    private String defaultBufferType = "default";
    private String parallelBufferType = "default";
    private int bufferSize = 1024 * 16;
    private int processThread = Runtime.getRuntime().availableProcessors();
    private String waitStrategy = "blocking";


    public WaitStrategy getWaitStrategy() {
        switch (waitStrategy) {
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "yielding":
                return new YieldingWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }
}
