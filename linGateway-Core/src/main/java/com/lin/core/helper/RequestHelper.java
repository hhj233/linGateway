package com.lin.core.helper;

import com.lin.common.config.*;
import com.lin.common.constant.BasicConst;
import com.lin.common.constant.GatewayConst;
import com.lin.common.enums.ResponseCode;
import com.lin.common.exception.ResponseException;
import com.lin.core.context.GatewayContext;
import com.lin.core.request.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.lin.common.enums.ResponseCode.PATH_NO_MATCHED;

/**
 * @author linzj
 */
public class RequestHelper {
    public static GatewayContext doContext(FullHttpRequest httpRequest, ChannelHandlerContext ctx) {
        // 构建请求对象 GatewayRequest
        GatewayRequest gatewayRequest = doRequest(httpRequest, ctx);

        // 根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)
        ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(gatewayRequest.getUniqueId());

        // 根据请求对象获取服务定义的方法调用，然后获取对应的规则
        ServiceInvoker serviceInvoker = new HttpServiceInvoker();
        serviceInvoker.setInvokerPath(gatewayRequest.getPath());
        serviceInvoker.setTimeout(500);

        // 根据请求对象获取规则
        Rule rule = getRule(gatewayRequest, serviceDefinition.getServiceId());

        //构建网关上下文
        GatewayContext gatewayContext = new GatewayContext(serviceDefinition.getProtocol(), ctx, HttpUtil.isKeepAlive(httpRequest),
                gatewayRequest, rule, 0);

        gatewayContext.getRequest().setModifyHost("127.0.0.1:80805");

        return gatewayContext;
    }

    /**
     * 构建Request请求对象
     * @param fullHttpRequest
     * @param ctx
     * @return
     */
    private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullHttpRequest.headers();
        // 从head头中必须传入的关键属性 uniqueId
        String uniqueId = headers.get(GatewayConst.UNIQUE_ID);

        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientId = getClientIp(ctx, fullHttpRequest);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

        GatewayRequest gatewayRequest = new GatewayRequest(uniqueId, charset, clientId, host, uri, method, contentType, headers, fullHttpRequest);
        return gatewayRequest;
    }

    /**
     * 获取客户端ip
     * @param ctx
     * @param request
     * @return
     */
    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;
        if (StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(","));
            if (values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if (clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }

    /**
     * 根据请求对象获取Rule对象
     * @param request
     * @param serviceId
     * @return
     */
    private static Rule getRule(GatewayRequest request, String serviceId) {
        String key = serviceId + "." + request.getPath();
        Rule rule = DynamicConfigManager.getInstance().getRuleByPath(key);
        if (rule != null) {
            return rule;
        }

        return DynamicConfigManager.getInstance().getRuleByServiceId(serviceId).stream()
                .filter(r -> request.getPath().startsWith(r.getPrefix()))
                .findAny()
                .orElseThrow(() -> new ResponseException(PATH_NO_MATCHED));
    }
}
