package com.lin.client.api;

import java.lang.annotation.*;

/**
 * @author linzj
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiService {
    /**
     * 服务id
     * @return
     */
    String  serviceId();

    /**
     * 版本号
     * @return
     */
    String version() default "1.0.0";

    /**
     * 协议
     * @return
     */
    ApiProtocol protocol();

    /**
     * 过滤路径
     * @return
     */
    String patternPath();

    /**
     * 接口名
     * @return
     */
    String interfaceName() default "";
}
