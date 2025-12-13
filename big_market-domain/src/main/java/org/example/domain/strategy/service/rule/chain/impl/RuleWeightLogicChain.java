package org.example.domain.strategy.service.rule.chain.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.example.domain.strategy.service.rule.chain.AbstractLogicChain;
import org.example.types.common.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 权重
 */
@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository repository;

    @Resource
    private IStrategyDispatch dispatch;

    public Long userScore = 4500L;

    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("抽奖责任链 - 权重开始 userId: {}, strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());

        // 1.查询数据库中定义的规则权重值
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel());

        // 2.根据用户ID查询用户抽奖消耗的积分值
        Map<Long, String> analyticalValue = getAnalyticalValue(ruleValue);
        if (null == analyticalValue || analyticalValue.isEmpty()) {
            return null;
        }

        // 3.转换Keys值，并默认排序
        ArrayList<Long> analyticalSortedKeys = new ArrayList<>(analyticalValue.keySet());
        Collections.sort(analyticalSortedKeys);

        // 4.找出最小符合的值
        Long nextValue = analyticalSortedKeys.stream()
                .filter(key -> userScore >= key)
                .findFirst()
                .orElse(null);

        if (null != nextValue) {
            Integer awardId = dispatch.getRandomAwardId(strategyId, analyticalValue.get(nextValue));
            log.info("抽奖责任链 - 权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
            return awardId;
        }

        return next().logic(userId, strategyId);
    }

    /**
     * 将规则字符串解析为Map<Long, String>
     * 规则字符串例如：4000:102,103,104,105 5000:102,103,104,105,106,107
     *
     * @param ruleValue 规则值字符串
     * @return Map
     */
    private Map<Long, String> getAnalyticalValue(String ruleValue){
        // 1.按照空格拆分为若干规则条目
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();

        // 2.逐个处理每个规则条目
        for (String ruleValueKey : ruleValueGroups) {
            if (ruleValueKey == null || ruleValueKey.isEmpty()){
                return  null;
            }
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }
            // 3.转为Map
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }

    @Override
    protected String ruleModel() {
        return "rule_weight";
    }
}
