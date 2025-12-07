package org.example.domain.strategy.service.armory;

/**
 * 策略装配库，负责初始化策略计算
 */
public interface IStrategyArmory {

    Boolean assembleLotteryStrategy(Long strategyId);
}
