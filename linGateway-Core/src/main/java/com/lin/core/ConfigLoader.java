package com.lin.core;

import com.lin.common.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * 配置加载类
 */
@Slf4j
public class ConfigLoader {
    private static final String CONFIG_FILE = "gateway.properties";
    private static final String ENV_PREFIX = "gateway_";
    private static final String JVM_PREFIX = "gateway.";
    private static final ConfigLoader INSTANCE = new ConfigLoader();

    public ConfigLoader() {
    }
    public static ConfigLoader getInstance() {
        return INSTANCE;
    }
    private Config config;
    public static Config getConfig() {
        return INSTANCE.config;
    }

    public Config load(String args[]) {
        config = new Config();

        //配置文件
        loadFromConfigFile();
        // 环境变量
        loadFromEnv();
        // jvm配置
        loadFromJvm();
        // 运行参数
        loadFromArg(args);
        return config;
    }

    /**
     * 读取配置文件数据
     */
    private void loadFromConfigFile(){
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (inputStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
                PropertiesUtil.properties2Object(properties, config);
            } catch (Exception e) {
                log.warn("load config file {} error, {}", CONFIG_FILE, e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {

                    }
                }
            }
        }

    }

    /**
     * 读取环境变量配置
     */
    private void loadFromEnv() {
        Map<String, String> envConfig = System.getenv();
        Properties properties = new Properties();
        properties.putAll(envConfig);
        PropertiesUtil.properties2Object(properties, config, ENV_PREFIX);
    }

    /**
     * 读取jvm配置
     */
    private void loadFromJvm() {
        Properties properties = System.getProperties();
        PropertiesUtil.properties2Object(properties, config, JVM_PREFIX);
    }

    /**
     * 读取运行时参数
     */
    private void loadFromArg(String[] args) {
        if (args != null && args.length > 0 ){
            Properties properties = new Properties();
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                }
            }
            PropertiesUtil.properties2Object(properties, config);
        }
    }
}
