package com.lin.registerCenter.nacosImpl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.junit.Test;

public class NacosRegisterCenterTest {
    @Test
    public void register() {
        String serviceAddress = "127.0.0.1:8848";
        try {
            NamingService namingService = NacosFactory.createNamingService(serviceAddress);
            Instance instance = new Instance();
            instance.setIp("127.0.0.1");
            instance.setPort(8080);
            instance.setServiceName("testServiceName");
            instance.addMetadata("version","1.0.0");
            instance.addMetadata("env","test");

            // 注册服务
            namingService.registerInstance("testServiceName",instance);
            System.out.println("服务注册成功");
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}