package com.lin.core.filter;

import java.lang.annotation.*;

/**
 * @author linzj
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FilterAspect {
    /**
     * 过滤器id
     * @return
     */
    String id();

    /**
     * 过滤器名称
     * @return
     */
    String name() default "";

    /**
     * 排序
     * @return
     */
    int order() default 0;
}
