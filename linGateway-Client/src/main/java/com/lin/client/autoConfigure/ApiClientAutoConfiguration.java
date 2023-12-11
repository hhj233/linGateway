package com.lin.client.autoConfigure;

import com.lin.client.api.ApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author linzj
 * 服务自动配置类
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "lin", name = {"registerAddress"})
public class ApiClientAutoConfiguration {

    @Autowired
    private ApiProperties apiProperties;
}
