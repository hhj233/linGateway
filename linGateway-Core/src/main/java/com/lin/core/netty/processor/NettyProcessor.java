package com.lin.core.netty.processor;

import com.lin.core.context.HttpRequestWrapper;

/**
 * @author linzj
 */
public interface NettyProcessor {
    void process(HttpRequestWrapper requestWrapper);

    void start();

    void shutdown();
}
