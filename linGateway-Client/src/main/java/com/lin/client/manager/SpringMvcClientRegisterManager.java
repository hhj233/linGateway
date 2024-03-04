package com.lin.client.manager;

import com.lin.client.api.ApiAnnotationScanner;
import com.lin.client.api.ApiProperties;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInstance;
import com.lin.common.constant.BasicConst;
import com.lin.common.constant.GatewayConst;
import com.lin.common.utils.NetUtil;
import com.lin.common.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author linzj
 * mvc服务注册管理类
 */
@Slf4j
public class SpringMvcClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent>,ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;
    private Set<Object> set = new HashSet<>();
    public SpringMvcClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // 监听spring启动事件
        if (applicationEvent instanceof ApplicationStartedEvent) {
            try {
                // 执行具体的springmvc项目注册
                doRegisterSpringMvc();
            }catch (Exception e) {
                log.error("doRegisterSpringMvc error", e);
                throw new RuntimeException(e);
            }

            log.info("springMvc api started");
        }
    }

    /**
     * 实际服务注册方法
     */
    private void doRegisterSpringMvc() {
        // 获取RequestMappingHandlerMapping类型的bean实例 得到我们controller信息
        // TODO RequestMappingHandlerMapping 文章输出
        // TODO BeanFactoryUtils.beanOfTypeIncludingAncestors
        Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                RequestMappingHandlerMapping.class, true, false);
        for (RequestMappingHandlerMapping handlerMapping : allRequestMappings.values()) {
            // 遍历所有的请求处理映射器
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> me : handlerMethods.entrySet()) {
                // 获取方法
                HandlerMethod handlerMethod = me.getValue();

                // TODO debug一眼
                Class<?> clazz = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(clazz);
                if (set.contains(bean)) {
                    continue;
                }

                // 扫描我们的服务 得到服务定义信息
                ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean);

                if (Objects.isNull(serviceDefinition)) {
                    continue;
                }

                // 设定环境信息
                serviceDefinition.setEnvType(getApiProperties().getEnv());
                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtil.getLocalIp();
                int port = serverProperties.getPort();
                String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
                serviceInstance.setServiceInstanceId(serviceInstanceId);
                serviceInstance.setIp(localIp);
                serviceInstance.setPort(port);
                serviceInstance.setUniqueId(serviceDefinition.getUniqueId());
                serviceInstance.setRegisterTime(TimeUtil.getCurrentTimeMills());
                serviceInstance.setVersion(serviceDefinition.getVersion());
                serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);
                serviceInstance.setGray(getApiProperties().isGray());

                // 注册
                register(serviceDefinition, serviceInstance);

            }
        }

    }
}
