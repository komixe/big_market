package org.example.domain.strategy.service.rule.chain.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.rule.chain.AbstractLogicChain;
import org.example.types.common.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 黑名单方法
 */
@Slf4j
@Component("rule_blacklist")
public class BlackListLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository repository;

    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("抽奖责任链 - 黑名单开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel());

        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.valueOf(splitRuleValue[0]);

        // 1.用户ID是否匹配黑名单
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                log.info("抽奖责任链 - 黑名单接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
                return awardId;
            }
        }

        // 2.进行下一个责任链节点
        log.info("抽奖责任链 - 黑名单放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return "rule_blacklist";
    }
}
