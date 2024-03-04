package com.lin.dubboService.Impl;

import com.lin.client.api.ApiProtocol;
import com.lin.client.api.ApiService;
import com.lin.dubboInterface.DubboRpcService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author linzj
 */
@ApiService(serviceId = "backend-dubbo-server", protocol = ApiProtocol.DUBBO, patternPath = "/**")
@DubboService
public class DubboRpcServiceImpl implements DubboRpcService {

    @Override
    public String testRpc(String msg) {
        return "hello world!!!"+ msg;
    }
}
