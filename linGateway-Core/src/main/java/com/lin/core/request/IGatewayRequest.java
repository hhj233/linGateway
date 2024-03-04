package com.lin.core.request;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * @author linzj
 */
public interface IGatewayRequest {
    /**
     * 修改域名
     * @param host
     */
    void setModifyHost(String host);

    /**
     * 获取域名
     * @return
     */
    String getModifyHost();

    /**
     * 设置路径
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取路径
     * @return
     */
    String getModifyPath();

    /**
     * 添加请求头信息
     * @param name
     * @param value
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求头信息
     * @param name
     * @param value
     */
    void setHeader(CharSequence name, String value);

    /**
     * GET　增加请求参数
     * @param name
     * @param value
     */
    void addQueryParam(String name, String value);

    /**
     * Post 增加请求参数
     * @param name
     * @param value
     */
    void addFormParam(String name, String value);

    /**
     * 增加或替换cookie
     * @param cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 设置请求超时时间
     * @param requestTimeout
     */
    void setRequestTimeout(int requestTimeout);

    /**
     * 获取最终的请求路径
     * @return
     */
    String getFinalUrl();

    /**
     * 构建最后的请求对象
     * @return
     */
    Request build();

}
