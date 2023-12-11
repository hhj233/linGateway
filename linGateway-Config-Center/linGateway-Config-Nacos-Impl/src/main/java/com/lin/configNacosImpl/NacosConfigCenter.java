package com.lin.configNacosImpl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.lin.common.config.Rule;
import com.lin.common.config.Rules;
import com.lin.common.utils.JsonUtil;
import com.lin.configCenterApi.ConfigCenter;
import com.lin.configCenterApi.RuleChangeListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;


/**
 * @author linzj
 */
@Slf4j
public class NacosConfigCenter implements ConfigCenter {
    /**
     * 需要拉取服务配置的DATA_ID 要求自定义
     */
    private static final String DATA_ID = "api-gateway";

    /**
     * 服务端地址
     */
    private String serviceAddress;

    /**
     * 环境
     */
    private String env;

    private ConfigService configService;

    @Override
    public void init(String serviceAddress, String env) {
        this.serviceAddress = serviceAddress;
        this.env = env;
        try {
            this.configService = NacosFactory.createConfigService(serviceAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeRuleChange(RuleChangeListener ruleChangeListener) {
        try  {
            // 初始化通知 DATA_ID是自己定义的 返回值是一个json
            String configJson = this.configService.getConfig(DATA_ID, env, 5000);
            log.info("config from nacos :{}", configJson);
            Rules rules = JsonUtil.parse(configJson, Rules.class);
            List<Rule> ruleList = rules.getRules();
            // 调用监听器 参数为ruleList
            ruleChangeListener.onRuleChange(ruleList);

            // 监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                // 是否需要额外线程执行
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String s) {
                    log.info("config from nacos :{}", s);
                    Rules rules = JsonUtil.parse(s, Rules.class);
                    List<Rule> ruleList = rules.getRules();
                    ruleChangeListener.onRuleChange(ruleList);
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}
