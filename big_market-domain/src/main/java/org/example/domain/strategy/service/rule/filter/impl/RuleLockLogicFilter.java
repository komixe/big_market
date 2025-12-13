package org.example.domain.strategy.service.rule.filter.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.strategy.model.entity.RuleActionEntity;
import org.example.domain.strategy.model.entity.RuleMatterEntity;
import org.example.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.annotation.LogicStrategy;
import org.example.domain.strategy.service.rule.filter.ILogicFilter;
import org.example.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户抽奖n次后，对应奖品可解锁抽奖
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_LOCK)
public class RuleLockLogicFilter implements ILogicFilter<RuleActionEntity.RaffleCenterEntity> {

    /** 策略仓储，用于查询策略规则配置  */
    @Resource
    private IStrategyRepository repository;

    private Long userRaffleCount = 0L;

    /**
     * 执行奖品解锁规则校验，抽奖n次后，对应奖品可解锁抽奖
     * @param ruleMatterEntity 规则校验上下文
     * @return 规则执行结果
     */
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleCenterEntity> filter(RuleMatterEntity ruleMatterEntity) {

        // 1.查询规则配置值
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(),
                ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        long raffleRuleValue = Long.parseLong(ruleValue);

        // 2. 判断用户抽奖次数是否达到解锁阈值
        if (userRaffleCount >= raffleRuleValue) {
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }

        // 3.未解锁，交由后续规则或流程处理
        return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                .build();


    }
}
