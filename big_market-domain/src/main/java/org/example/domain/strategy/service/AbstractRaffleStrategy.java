package org.example.domain.strategy.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.strategy.model.entity.RaffleAwardEntity;
import org.example.domain.strategy.model.entity.RaffleFactorEntity;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.example.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import org.example.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
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

    /** 抽奖的责任链 -> 从抽奖的规则中，解耦出前置规则为责任链处理 */
    protected DefaultChainFactory defaultChainFactory;

    /** 抽奖的决策树 -> 负责抽奖中到抽奖后的规则过滤 */
    protected DefaultTreeFactory treeFactory;

    public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch dispatch,  DefaultChainFactory defaultChainFactory, DefaultTreeFactory treeFactory) {
        this.repository = repository;
        this.dispatch = dispatch;
        this.defaultChainFactory = defaultChainFactory;
        this.treeFactory = treeFactory;
    }


    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1.参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode());
        }

        // 2.责任链抽奖计算 [这里得到的是初步的奖品ID，之后需要根据ID处理后续抽奖]
        DefaultChainFactory.StrategyAwardVO chainStrategyAwardVO = raffleLogicChain(userId, strategyId);
        log.info("抽奖策略计算 - 责任链 {} {} {} {}", userId, strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getLogicModel());
        if (!DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode().equals(chainStrategyAwardVO.getLogicModel())){
            return RaffleAwardEntity.builder()
                    .awardId(chainStrategyAwardVO.getAwardId())
                    .build();
        }

        // 3.规则树抽奖过滤 [奖品ID，根据抽奖次数、库存、兜底策略判断最终的可获得奖品ID]
        DefaultTreeFactory.StrategyAwardVO treeStrategyAwardVO = raffleLogicTree(userId, strategyId, chainStrategyAwardVO.getAwardId());
        log.info("抽奖策略计算-规则树 {} {} {} {}", userId, strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());


        return  RaffleAwardEntity.builder()
                .awardId(treeStrategyAwardVO.getAwardId())
                .awardConfig(treeStrategyAwardVO.getAwardRuleValue())
                .build();
    }

    /**
     * 抽奖计算，责任链抽象方法
     *
     * @param userId 用户ID
     * @param strategyId 策略ID
     * @return 奖品ID
     */
    public abstract DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId);


    /**
     * 抽奖结果过滤，决策树抽象方法
     *
     * @param userId 用户ID
     * @param strategyId 策略ID
     * @param awardId 奖品ID
     * @return 过滤结果【奖品ID，根据抽奖次数判断、库存判断、兜底逻辑返回最终可获得奖品】
     */
    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId);


}
