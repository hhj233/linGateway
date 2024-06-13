package com.lin.core.filter.monitor;

import com.alibaba.nacos.common.utils.RandomUtils;
import com.lin.common.constant.FilterConst;
import com.lin.core.ConfigLoader;
import com.lin.core.context.GatewayContext;
import com.lin.core.filter.Filter;
import com.lin.core.filter.FilterAspect;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author linzj
 */
@FilterAspect(id = FilterConst.MONITOR_END_FILTER_ID,
        name = FilterConst.MONITOR_END_FILTER_NAME,
        order = FilterConst.MONITOR_END_FILTER_ORDER)
@Slf4j
public class MonitorEndFilter implements Filter {
    /**
     * Prometheus监控的注册表实例，用于存储和管理监控指标。
     */
    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public MonitorEndFilter() {
        this.prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        try {
            // 开启
            HttpServer httpServer = HttpServer.create(
                    new InetSocketAddress(9090), 0);
            httpServer.createContext("/prometheus", exchange -> {
                // 获取prometheus格式的监控数据
                String scrape = prometheusMeterRegistry.scrape();

                // 发送响应头 状态码200 内容长度为指标数据的字节长度
                exchange.sendResponseHeaders(HttpStatus.SC_OK, scrape.getBytes().length);
                // 发送响应体 即指标数据
                try (OutputStream os = exchange.getResponseBody()){
                    os.write(scrape.getBytes());
                }
            });

            // 启动http服务器
            new Thread(httpServer::start).start();
        }catch (IOException e) {
            log.error("prometheus http server start error", e);
            throw new RuntimeException(e);
        }
        // prometheus启动成功
        log.info("prometheus http server start successfully,port:{}", 9090);
    }

    public static void main(String[] args) {
        MonitorEndFilter monitorEndFilter = new MonitorEndFilter();
        // 使用mock数据定期更新Prometheus监控指标，以模拟应用程序负载
        Executors.newScheduledThreadPool(1000).scheduleAtFixedRate(() -> {
            // 创建Timer.Sample实例，开始计时
            Timer.Sample sample = Timer.start();
            try {
                // 模拟执行一个持续100到200毫秒的任务
                Thread.sleep(RandomUtils.nextInt(100, 200));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // TODO: 此处代码被注释，后续需根据实际情况实现具体的监控逻辑
            // 创建一个定制的Timer，记录请求的相关信息
            Timer timer = monitorEndFilter.prometheusMeterRegistry.timer("linGatewayRequest",
                    "uniqueId", "backend-http-server:1.0.0",
                    "protocol", "http",
                    "path", "/http-server/ping" + RandomUtils.nextInt(10, 200));
            // 停止计时器，并将数据记录到Prometheus注册表中
            sample.stop(timer);
        }, 200, 100, TimeUnit.MILLISECONDS); // 定时任务的初始延迟、周期和时间单位
    }

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        Timer timer = prometheusMeterRegistry.timer("linGatewayRequest", "uniqueId", ctx.getUniqueId(),
                "protocol", ctx.getProtocol(),
                "path", ctx.getRequest().getPath());

        // 停止时间，记录操作的执行时间
        ctx.getTimerSample().stop(timer);
    }
}
