package com.lin.core.context;

import com.lin.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author linzj
 */
public interface IContext {
    /**
     * 一个请求正在执行的状态
     */
    int RUNNING = 0;
    /**
     * 标志请求结束 写回response
     */
    int WRITTEN = 1;
    /**
     * 写回成功后，设置该标识 如果是Netty ctx.writeAndFlush(response)
     */
    int COMPLETED = 2;
    /**
     * 整个网关请求完毕 彻底结束
     */
    int TERMINATED = -1;

    /**
     * 设置上下文状态为正常运行状态
     */
    void running();

    /**
     * 设置上下文状态为标记写回状态
     */
    void written();

    /**
     * 设置上下文状态为标记写回成功状态
     */
    void completed();

    /**
     * 设置上下文状态为请求完毕状态
     */
    void terminated();

    /**
     * 是否在运行
     * @return
     */
    boolean isRunning();

    /**
     * 是否在写回
     * @return
     */
    boolean isWritten();

    /**
     * 是否是写回完成
     * @return
     */
    boolean isCompleted();

    /**
     * 是否是请求完毕
     * @return
     */
    boolean isTerminated();

    /**
     * 获取协议
     * @return
     */
    String getProtocol();

    /**
     * 获取规则
     * @return
     */
    Rule getRule();

    /**
     * 设置规则
     * @param rule
     */
    void setRule(Rule rule);

    /**
     * 获取请求
     * @return
     */
    Object getRequest();

    /**
     * 设置请求
     * @param request
     */
    void setRequest(Object request);

    /**
     * 获取返回体
     * @return
     */
    Object getResponse();

    /**
     * 设置返回体
     * @param response
     */
    void setResponse(Object response);

    /**
     * 获取异常
     * @return
     */
    Throwable getThrowable();

    /**
     * 设置异常
     * @param throwable
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取参数
     * @param key
     * @return
     */
    Object getAttribute(Map<String,Object> key);

    /**
     * 设置参数
     * @param key
     * @param obj
     */
    void setAttribute(String key, Object obj);

    /**
     * 获取netty上下文
     * @return
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 是否保持连接
     * @return
     */
    boolean isKeepAlive();

    /**
     * 是否是释放资源
     */
    void releaseRequest();

    /**
     * 设置回调函数
     * @param consumer
     */
    void setCompletedCallback(Consumer<IContext> consumer);

    /**
     * 设置回调函数
     */
    void invokerCompletedCallback();

}
