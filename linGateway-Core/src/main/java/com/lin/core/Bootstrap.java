package com.lin.core;

public class Bootstrap {

    public static void main(String[] args) {
        // 加载核心网关配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config.getPort());
    }
}
