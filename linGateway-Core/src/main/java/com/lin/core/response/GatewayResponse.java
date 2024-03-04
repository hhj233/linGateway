package com.lin.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lin.common.enums.ResponseCode;
import com.lin.common.utils.JsonUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.asynchttpclient.Response;

/**
 * @author linzj
 */
@Data
@NoArgsConstructor
public class GatewayResponse {
    /**
     * 响应头
     */
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    /**
     * 额外的响应结果
     */
    private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    /**
     * 响应内容
     */
    private String content;

    /**
     * 异步返回对象
     */
    private Response futureResponse;

    /**
     * 响应状态码
     */
    private HttpResponseStatus httpResponseStatus;


    /**
     * 设置响应头信息
     * @param key
     * @param val
     */
    public void putHeader(CharSequence key, CharSequence val) {
        responseHeaders.add(key,val);
    }

    /**
     * 异步构建响应对象
     * @param futureResponse
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Response futureResponse) {
        GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return response;
    }

    public static GatewayResponse buildGatewayResponse(ResponseCode code, Object... args) {
        ObjectNode objectNode = JsonUtil.createObjectNode();
        objectNode.put(JsonUtil.STATUS, code.getStatus().code());
        objectNode.put(JsonUtil.CODE, code.getCode());
        objectNode.put(JsonUtil.MESSAGE, code.getMessage());

        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(code.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        response.setContent(JsonUtil.toJsonString(objectNode));
        return response;
    }
}
