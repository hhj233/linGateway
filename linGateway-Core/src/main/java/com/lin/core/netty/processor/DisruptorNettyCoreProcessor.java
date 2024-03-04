package com.lin.core.netty.processor;

import com.lin.core.Config;
import com.lin.core.context.HttpRequestWrapper;

/**
 * @author linzj
 */
public class DisruptorNettyCoreProcessor implements NettyProcessor{
    private final Config config;
    private final NettyCoreProcessor nettyCoreProcessor;

    public DisruptorNettyCoreProcessor(Config config, NettyCoreProcessor nettyCoreProcessor) {
        this.config = config;
        this.nettyCoreProcessor = nettyCoreProcessor;
    }

    @Override
    public void process(HttpRequestWrapper requestWrapper) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
