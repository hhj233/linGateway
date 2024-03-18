package com.lin.httpServer.controller;

import com.lin.client.api.ApiInvoker;
import com.lin.client.api.ApiProperties;
import com.lin.client.api.ApiProtocol;
import com.lin.client.api.ApiService;
import com.lin.common.config.ServiceDefinition;
import com.lin.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linzj
 * httpController类
 */
@RestController
@Slf4j
@ApiService(serviceId = "backend-http-server", protocol = ApiProtocol.HTTP, patternPath = "/http-server/**")
public class HttpController {
    @Autowired
    private ApiProperties apiProperties;

    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() {
        log.info("ping:{}", JsonUtil.toJsonString(apiProperties));
        return "pong gary = false";
    }

    public static void main(String[] args) {
        String a = "{\"uniqueId\":\"backend-http-server\",\"serviceId\":\"backend-http-server\",\"version\":\"1.0.0\",\"protocol\":\"http\",\"patternPath\":\"/http-server/**\",\"envType\":\"dev\",\"enable\":true,\"invokerMap\":{\"/http-server/ping\":{\"invokerPath\":\"/http-server/ping\",\"timeout\":5000}}}";
        ServiceDefinition parse = JsonUtil.parse(a, ServiceDefinition.class);
        System.out.println(parse.toString());
    }
}
