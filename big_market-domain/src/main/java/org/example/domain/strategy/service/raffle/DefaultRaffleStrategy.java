package org.example.domain.strategy.service.raffle;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.strategy.model.vo.RuleTreeVO;
import org.example.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import org.example.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.AbstractRaffleStrategy;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.example.domain.strategy.service.rule.chain.ILogicChain;
import org.example.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import org.example.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import org.example.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import org.springframework.stereotype.Service;


/**
 * 默认抽奖策略实现类
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    /**
     * 构造器注入
     *
     * @param repository    数据访问仓储
     * @param dispatch  策略分发器
     * @param defaultChainFactory   默认责任链工厂
     * @param treeFactory   默认规则树工厂
     */
    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch dispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory treeFactory) {
        super(repository, dispatch, defaultChainFactory, treeFactory);
    }

    /**
     * 抽奖责任链模式入口
     *
     * @param userId 用户ID
     * @param strategyId 策略ID
     * @return 抽奖结果
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        // 1. 查询该 策略ID + 奖品ID 对应的规则模型Key
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModel(strategyId, awardId);
        // 2.若奖品未配置规则模型，则直接返回该奖品
        if (strategyAwardRuleModelVO == null) {
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        // 3.根据规则模型查询规则树
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if (ruleTreeVO == null) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        // 4.通过工厂构建决策树引擎
        IDecisionTreeEngine treeEngine = treeFactory.openLogicTree(ruleTreeVO);
        // 5.执行规则树处理：根据用户、策略、奖品做规则判定并输出最终结果
        return treeEngine.process(userId, strategyId, awardId);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        return repository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        repository.updateStrategyAwardStock(strategyId, awardId);
    }
}
