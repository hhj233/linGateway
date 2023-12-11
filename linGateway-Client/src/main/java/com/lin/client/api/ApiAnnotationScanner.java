package com.lin.client.api;

import com.lin.common.config.DubboServiceInvoker;
import com.lin.common.config.HttpServiceInvoker;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.config.ServiceInvoker;
import com.lin.common.constant.DubboConst;
import lombok.NoArgsConstructor;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author linzj
 * 注解扫描类
 */
@NoArgsConstructor
public class ApiAnnotationScanner {
    private static class SingletonHolder {
        static final ApiAnnotationScanner INSTANCE = new ApiAnnotationScanner();
    }
    public static ApiAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 扫描传入的对象，最终返回一个服务定义
     * @param bean
     * @param args
     * @return
     */
    public ServiceDefinition scanner(Object bean, Object... args) {
        // 判断是否存在我们暴露服务注解
        Class<?> clazz = bean.getClass();
        if (!clazz.isAnnotationPresent(ApiService.class)) {
            return null;
        }

        ApiService apiService = clazz.getAnnotation(ApiService.class);
        String serviceId = apiService.serviceId();
        String version = apiService.version();
        ApiProtocol protocol = apiService.protocol();
        String patternPath = apiService.patternPath();

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        // 创建一个请求路径与执行器容器
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();
        // 获取当前类的所有方法
        Method[] methods = clazz.getMethods();
        if (methods == null && methods.length <= 0) {
            return null;
        }
        for (Method method : methods) {
            if (!method.isAnnotationPresent(ApiInvoker.class)) {
                continue;
            }
            ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
            String path = apiInvoker.path();
            switch (protocol) {
                case HTTP :
                    HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path);
                    invokerMap.put(path, httpServiceInvoker);
                    break;
                case DUBBO :
                    break;
                default :
                    break;
            }
        }
        serviceDefinition.setServiceId(serviceId);
        serviceDefinition.setEnable(true);
        serviceDefinition.setProtocol(protocol.getProtocol());
        serviceDefinition.setUniqueId(serviceId);
        serviceDefinition.setVersion(version);
        serviceDefinition.setPatternPath(patternPath);
        serviceDefinition.setInvokerMap(invokerMap);
        return serviceDefinition;
    }


    /**
     * 创建httpServiceInvoker对象
     * @param path
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 构建dubboSerivceInvoker对象
     * @param path  请求路径
     * @param serviceBean   将java类转化为一个可发布的DUBBO服务，从而允许远程服务消费者调用该服务
     *                      DUBBO服务的配置，包括注册地址、接口类等信息
     * @param method    方法
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);

        String methodName = method.getName();
        // 获取注册中心定制
        String registerAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        // 将配置信息设置到dubbo服务执行器中
        dubboServiceInvoker.setRegisterAddress(registerAddress);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);
        dubboServiceInvoker.setMethodName(methodName);
        
        // 参数类型数组
        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] clazz = method.getParameterTypes();
        for (int i = 0; i < clazz.length; i++) {
            parameterTypes[i] = clazz[i].getName();
        }
        dubboServiceInvoker.setParameterType(parameterTypes);

        Integer timeout = serviceBean.getTimeout();
        if (timeout == null || timeout == 0) {
            ProviderConfig provider = serviceBean.getProvider();
            if (Objects.nonNull(provider)) {
                Integer providerTimeout = provider.getTimeout();
                if (providerTimeout == null || providerTimeout == 0) {
                    timeout = DubboConst.DUBBO_TIMEOUT;
                } else {
                    timeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(timeout);

        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);

        return dubboServiceInvoker;
    }
}
