package com.lin.client.autoConfigure;

import com.lin.client.api.ApiProperties;
import com.lin.client.manager.DubboClientRegisterManager;
import com.lin.client.manager.SpringMvcClientRegisterManager;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * @author linzj
 * 服务自动配置类
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "linGateway", name = {"registerAddress"})
public class ApiClientAutoConfiguration {

    @Autowired
    private ApiProperties apiProperties;


    /**
     * springMvc环境下才会配置
     * @return
     */
    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMvcClientRegisterManager.class)
    public SpringMvcClientRegisterManager springMvcClientRegisterManager() {
        return new SpringMvcClientRegisterManager(apiProperties);
    }

    /**
     * dubbo环境才会进行配置
     * @return
     */
    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(DubboClientRegisterManager.class)
    public DubboClientRegisterManager dubboClientRegisterManager() {
        return new DubboClientRegisterManager(apiProperties);
    }
}
