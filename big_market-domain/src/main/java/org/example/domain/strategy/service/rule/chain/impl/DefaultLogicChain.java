package org.example.domain.strategy.service.rule.chain.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.example.domain.strategy.service.rule.chain.AbstractLogicChain;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 兜底责任链节点
 */
@Slf4j
@Component("default")
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    protected IStrategyDispatch dispatch;

    @Override
    public Integer logic(String userId, Long strategyId) {
        Integer awardId = dispatch.getRandomAwardId(strategyId);
        log.info("抽奖责任链 - 默认处理 userId: {} strategyId: {}, awardId: {}", userId, strategyId, awardId);
        return awardId;
    }

    @Override
    protected String ruleModel() {
        return "default";
    }
}
