package com.lin.common.config;

import lombok.Data;

/**
 * @author linzj
 */
@Data
public class DubboServiceInvoker extends AbstractServiceInvoker{
    /**
     * 注册中心地址
     */
    private String registerAddress;

    /**
     * 接口全类名
     */
    private String interfaceClass;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数名称的集合
     */
    private String[] parameterType;

    /**
     * dubbo服务版本号
     */
    private String version;
}
