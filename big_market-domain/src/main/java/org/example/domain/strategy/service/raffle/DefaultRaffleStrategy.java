package org.example.domain.strategy.service.raffle;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.strategy.model.entity.RaffleFactorEntity;
import org.example.domain.strategy.model.entity.RuleActionEntity;
import org.example.domain.strategy.model.entity.RuleMatterEntity;
import org.example.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import org.example.domain.strategy.respository.IStrategyRepository;
import org.example.domain.strategy.service.annotation.LogicStrategy;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.example.domain.strategy.service.rule.ILogicFilter;
import org.example.domain.strategy.service.rule.factory.DefaultLogicFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy{

    @Resource
    private DefaultLogicFactory factory;

    public DefaultRaffleStrategy(IStrategyDispatch strategyDispatch, IStrategyRepository strategyRepository, ResourceLoader resourceLoader) {
        super(strategyDispatch, strategyRepository, resourceLoader);
    }

    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        // 1.未配置抽奖中规则，直接放行
        if (logics == null || logics.length == 0){
            return  RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }
        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> filterMap = factory.openLogicFilter();

        // 黑名单规则优先过滤
        String ruleBlackList = Arrays.stream(logics)
                .filter(logic -> logic.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .findFirst()
                .orElse(null);

        if (StringUtils.isNotBlank(ruleBlackList)) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = filterMap.get(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());
            RuleMatterEntity ruleMatterEntity = RuleMatterEntity.builder()
                    .userId(raffleFactorEntity.getUserId())
                    .strategyId(raffleFactorEntity.getStrategyId())
                    .ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                    .build();
            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }

        // 顺序过滤剩余规则
        List<String> ruleList = Arrays.stream(logics)
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .collect(Collectors.toList());

        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
        for (String ruleModel : ruleList) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = filterMap.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = RuleMatterEntity.builder()
                    .userId(raffleFactorEntity.getUserId())
                    .strategyId(raffleFactorEntity.getStrategyId())
                    .ruleModel(ruleModel)
                    .build();
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            log.info("抽奖前规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(),
                     ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }

        return ruleActionEntity;
    }

    /**
     * 执行抽奖中规则校验
     * @param raffleFactorEntity 抽奖因子对象
     * @param logics 规则模型
     * @return 规则校验结果
     */
    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        // 1.未配置抽奖中规则，直接放行
        if (logics == null || logics.length == 0){
            return  RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }

        // 2.从规则工厂中获取所有一注册的规则策略
        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> filterMap = factory.openLogicFilter();

        // 3.按照规则模型顺序执行中间态规则
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity = null;
        for (String ruleModel : logics) {
            // 3.1 根据ruleModel获取对应的规则策略实现
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = filterMap.get(ruleModel);
            // 3.2 构建规则执行所需的上下文信息
            RuleMatterEntity ruleMatterEntity = RuleMatterEntity.builder()
                    .strategyId(raffleFactorEntity.getStrategyId())
                    .userId(raffleFactorEntity.getUserId())
                    .awardId(raffleFactorEntity.getAward())
                    .ruleModel(ruleModel)
                    .build();
            // 3.3 执行规则校验
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 3.4 若规则未通过，则立即中断流程并返回结果
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }
        return ruleActionEntity;
    }
}
