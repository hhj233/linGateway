package com.lin.configCenterApi;

import com.lin.common.config.Rule;

import java.util.List;

/**
 * @author Administrator
 * 规则变更监听器
 */
public interface RuleChangeListener {
    /**
     * 规则变更调用此方法 对规则进行更新
     * @param rules 新规则
     */
    void onRuleChange(List<Rule> rules);
}
