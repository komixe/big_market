package org.example.domain.strategy.service.raffle;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.strategy.model.entity.RaffleAwardEntity;
import org.example.domain.strategy.model.entity.RaffleFactorEntity;
import org.example.domain.strategy.model.entity.RuleActionEntity;
import org.example.domain.strategy.model.entity.StrategyEntity;
import org.example.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import org.example.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.IRaffleStrategy;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.example.domain.strategy.service.rule.factory.DefaultLogicFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * 抽奖策略抽象类，定义抽奖的标准流程
 */
@Slf4j
@Service
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    /** 策略仓储服务 */
    protected IStrategyRepository repository;

    /** 策略调度服务 */
    protected IStrategyDispatch dispatch;

    public  AbstractRaffleStrategy(IStrategyDispatch strategyDispatch, IStrategyRepository strategyRepository, ResourceLoader resourceLoader) {
        this.dispatch = strategyDispatch;
        this.repository = strategyRepository;
    }


    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1.参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode());
        }

        // 2.策略查询
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);

        // 3.抽奖前 - 规则过滤
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity =
                this.doCheckRaffleBeforeLogic(
                        RaffleFactorEntity.builder().userId(userId).strategyId(strategyId).build(),
                        strategy.ruleModels());

        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionEntity.getCode())) {
            if (DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionEntity.getRuleModel())){
                return RaffleAwardEntity.builder()
                        .awardId(ruleActionEntity.getData().getAwardId())
                        .build();
            } else if (DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(ruleActionEntity.getRuleModel())){
                String ruleWeightValueKey = ruleActionEntity.getData().getRuleWeightValueKey();
                Integer awardId = dispatch.getRandomAwardId(strategyId, ruleWeightValueKey);
                return  RaffleAwardEntity.builder()
                        .awardId(awardId)
                        .build();
            }
        }

        // 4.默认抽奖流程
        Integer awardId = dispatch.getRandomAwardId(strategyId);

        // 5.查询奖品规则
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModel(strategyId, awardId);

        // 6.抽奖中 - 规则过滤
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleCenterEntity = this.doCheckRaffleCenterLogic(
                RaffleFactorEntity.builder().strategyId(strategyId).award(awardId).userId(userId).build(),
                strategyAwardRuleModelVO.raffleCenterRuleModelList());

        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleCenterEntity.getCode())) {
            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
            return RaffleAwardEntity.builder()
                    .awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
                    .build();

        }
        return  RaffleAwardEntity.builder()
                .awardId(awardId)
                .build();
    }


    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(
            RaffleFactorEntity raffleFactorEntity, String... logics);

    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(
            RaffleFactorEntity raffleFactorEntity, String... logics);
}
