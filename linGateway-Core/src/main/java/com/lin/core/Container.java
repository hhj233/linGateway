package com.lin.core;

public class Container implements LifeCycle{
    private Config config;

    public Container(Config config) {
        this.config = config;
        this.init();
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdowm() {

    }
}
