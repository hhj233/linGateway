package com.lin.client.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author linzj
 */
@Data
@ConfigurationProperties(prefix = "lin")
public class ApiProperties {
    /**
     * 注册中心地址
     */
    private String registerAddress;
    /**
     * 环境
     */
    private String env;
    /**
     * 是否灰度发布
     */
    private boolean gray;
}
