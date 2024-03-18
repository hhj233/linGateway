package com.lin.core.filter.router;

import com.lin.common.config.Rule;
import com.lin.common.enums.ResponseCode;
import com.lin.common.exception.ConnectException;
import com.lin.common.exception.ResponseException;
import com.lin.core.ConfigLoader;
import com.lin.core.context.GatewayContext;
import com.lin.core.filter.Filter;
import com.lin.core.filter.FilterAspect;
import com.lin.core.helper.AsyncHttpHelper;
import com.lin.core.helper.ResponseHelper;
import com.lin.core.response.GatewayResponse;
import com.netflix.hystrix.*;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.lin.common.constant.FilterConst.*;

/**
 * @author linzj
 */
@Slf4j
@FilterAspect(id = ROUTER_FILTER_ID,
        name = ROUTER_FILTER_NAME,
        order = ROUTER_FILTER_ORDER)
public class RouterFilter implements Filter {
    private final Logger accessLog = LoggerFactory.getLogger("accessLog");
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        // 获取熔断降级的配置
        Optional<Rule.HystrixConfig> hystrixConfig = getHystrixConfig(ctx);
        if (hystrixConfig.isPresent()) {
            routeWithHystrix(ctx, hystrixConfig);
        } else {
            route(ctx, hystrixConfig);
        }
    }


    /**
     * 获取hystrixConfig
     * @param gatewayContext
     * @return
     */
    private static Optional<Rule.HystrixConfig> getHystrixConfig(GatewayContext gatewayContext) {
        Rule rule = gatewayContext.getRule();
        Optional<Rule.HystrixConfig> hystrixConfig = rule.getHystrixConfigs().stream()
                .filter(s -> StringUtils.equals(s.getPath(),
                gatewayContext.getRequest().getPath())).findFirst();
        return hystrixConfig;
    }

    /**
     * 正常异步逻辑
     * whenComplete方法：
     * whenComplete是一个非异步的完成方法。
     * 当CompletableFuture的执行完成或发生异常时，提供一个回调。
     * 这个回调将在CompletableFuture执行的相同进程中执行。这意味着，如果completeFuture的操作时阻塞的，那么回调也会在同一个阻塞的线程中执行
     * 这段代码如果whenComplete为true，则在future完成时使用whenComplete方法，这意味着complete方法将在future所在的线程中被调用。
     *
     * whenCompleteAsync是一个异步的完成方法。
     * 它也提供一个在CompletableFuture执行完成或者发生异常时执行的回调。
     * 与whenComplete执行方法不同，这个回调在不同的线程中执行。通常情况下，默认采用ForkJoinPool中的某个线程上执行，除非提供自定义的Executor
     * 在代码中，如果whenComplete为false，则调用whenCompleteAsync方法。
     * @param ctx
     * @param hystrixConfig
     * @return
     */
    private CompletableFuture<Response> route(GatewayContext ctx, Optional<Rule.HystrixConfig> hystrixConfig) {
        Request request = ctx.getRequest().build();
        // 执行具体请求，并得到一个COmpletableFuture对象用于帮助我们执行后续的处理
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if (whenComplete) {
            future.whenComplete(((response, throwable) -> {
                complete(request, response, throwable, ctx, hystrixConfig);
            }));
        } else {
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request, response, throwable, ctx, hystrixConfig);
            }));
        }
        return future;
    }

    /**
     * 根据提供的GatewayContext和Hystrix配置，执行路由配置 并在熔断时执行降级逻辑
     * 熔断会发生在：
     *  当 Hystrix 命令的执行时间超过配置的超时时间
     *  当 Hystrix 命令的执行出现异常或错误
     *  当连续请求失败率达到配置的阈值
     * @param ctx
     * @param hystrixConfig
     */
    private void routeWithHystrix(GatewayContext ctx, Optional<Rule.HystrixConfig> hystrixConfig) {
        HystrixCommand.Setter setter =
                // 进行分组
                HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(ctx.getUniqueId()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(ctx.getRequest().getPath()))
                        // 线程池大小设置
                        .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                                .withCoreSize(hystrixConfig.get().getThreadCoreSize()))
                        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                                // 线程池
                                .withExecutionIsolationStrategy(HystrixCommandProperties
                                        .ExecutionIsolationStrategy.THREAD)
                                .withExecutionTimeoutInMilliseconds(hystrixConfig.get().getTimeoutInMilliseconds())
                                .withExecutionIsolationThreadInterruptOnTimeout(true)
                                .withExecutionTimeoutEnabled(true));

        // 创建一个新的HystrixCommand对象 用于执行实际的路由操作
        new HystrixCommand<Object>(setter) {
            @Override
            protected Object run() throws Exception {
                route(ctx, hystrixConfig).get();
                return null;
            }

            @Override
            protected Object getFallback() {
                // 当熔断发生时，执行降级逻辑
                // 设置网关上下文的响应信息 通常包括一个降级响应
                ctx.setResponse(hystrixConfig.get().getFallbackResponse());
                ctx.written();
                return null;
            }
        }
        // 执行hystrix指令
        .execute();
    }

    private void complete(Request request, Response response, Throwable throwable, GatewayContext ctx,
                          Optional<Rule.HystrixConfig> hystrixConfig) {
        // 请求处理完毕 释放资源
        ctx.releaseRequest();
        // 获取网关上下文规则
        Rule rule = ctx.getRule();
        // 获取重新请求次数
        int currentRetryTimes = ctx.getCurrentRetryTimes();
        int confRetryTimes = rule.getRetryConfig().getTimes();
        // 判断是否出现异常 如果是进行重试
        if ((throwable instanceof TimeoutException || throwable instanceof IOException) &&
                currentRetryTimes <= confRetryTimes && !hystrixConfig.isPresent()) {
            //请求重试
            doRetry(ctx, currentRetryTimes);
            return;
        }

        try {
            // 之前出现异常 执行期异常返回逻辑
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException){
                    log.warn("complete time out {}", url);
                    ctx.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                    ctx.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    ctx.setThrowable(new ConnectException(throwable, ctx.getUniqueId(), url,
                            ResponseCode.HTTP_RESPONSE_ERROR));
                    ctx.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                //没有出现异常直接正常返回
                ctx.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Throwable t) {
            ctx.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            ctx.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", t);
        } finally {
            ctx.written();
            ResponseHelper.writeResponse(ctx);

            // 增加日志打印
            accessLog.info("{} {} {} {} {} {} {}",
                    System.currentTimeMillis() - ctx.getRequest().getBeginTime(),
                     ctx.getRequest().getClientIp(),
                     ctx.getRequest().getUniqueId(),
                     ctx.getRequest().getMethod(),
                     ctx.getRequest().getPath(),
                     ctx.getResponse().getHttpResponseStatus().code(),
                     ctx.getResponse().getFutureResponse().getResponseBodyAsBytes().length);
        }
    }

    private void doRetry(GatewayContext gatewayContext, int retryTimes) {
        System.out.println("当前重试次数为" + retryTimes);
        gatewayContext.setCurrentRetryTimes(retryTimes + 1);
        try {
            //调用路由过滤器方法再次进行请求重试
            doFilter(gatewayContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
