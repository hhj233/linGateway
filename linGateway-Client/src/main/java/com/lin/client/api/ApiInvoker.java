package com.lin.client.api;

import java.lang.annotation.*;

/**
 * @author linzj
 * 该注解必须要服务方法上使用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    String path();
}
