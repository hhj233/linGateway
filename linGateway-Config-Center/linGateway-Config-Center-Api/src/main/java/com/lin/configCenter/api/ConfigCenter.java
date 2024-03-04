package com.lin.configCenter.api;

/**
 * @author linzj
 * 配置中心接口方法
 */
public interface ConfigCenter {

    /**
     * 配置中心初始化接口
     * @param serviceAddress    配置中心地址
     * @param env   环境
     */
    void init(String serviceAddress, String env);

    /**
     * 监听配置中心配置变更
     * @param ruleChangeListener   规则监听器
     */
    void subscribeRuleChange(RuleChangeListener ruleChangeListener);
}
