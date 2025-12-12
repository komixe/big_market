package org.example.domain.strategy.service.rule.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.strategy.model.entity.RuleActionEntity;
import org.example.domain.strategy.service.annotation.LogicStrategy;
import org.example.domain.strategy.service.rule.ILogicFilter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则工厂
 *
 * 该工厂在系统启动时自动收集所有实现ILogicFilter接口并标注了@LogicStrategy注解的规则过滤器
 * 将它们按照逻辑类型LogicModel提供的code字段注册到Map中
 */
@Service
public class DefaultLogicFactory {

    /**
     * 存放已注册的过滤器映射表
     *
     * key = LogicModel.code（例如 "rule_weight"、"rule_blacklist"）
     * value = 对应的 ILogicFilter 实现类实例
     */
    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();


    /**
     * 构造函数
     * Spring启动时会自动注入系统中所有实现了ILogicFilter接口的Bean
     * 将对应的逻辑类型（LogicModel.code）与具体 Filter 实例注册到 Map 中
     *
     * @param logicFilters Spring注入的所有ILogicFilter Bean
     */
    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        logicFilters.forEach(logic -> {
            // 从当前filter类上读取@LogicStrategy注解
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
            if (null != strategy) {
                logicFilterMap.put(strategy.logicMode().getCode(), logic);
            }
        });
    }

    /**
     * 对外暴露过滤器映射表
     *
     * 外部可以通过此方法获得所有逻辑过滤器，并按照规则类型code来匹配对应过滤器
     */
    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    /**
     * 逻辑枚举 - LogicModel
     * 用于定义系统中可支持的逻辑规则类型
     */
    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        /** 根据抽奖权重过滤，决定可参与抽奖的策略范围 */
        RULE_WIGHT("rule_weight","【抽奖前规则】根据抽奖权重返回可抽奖范围KEY","before"),

        /** 黑名单规则过滤，命中黑名单直接拒绝抽奖 */
        RULE_BLACKLIST("rule_blacklist","【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回","before"),

        /** 抽奖n次后，对应奖品可解锁抽奖 */
        RULE_LOCK("rule_lock","【抽奖中规则】抽奖n次后，对应奖品可解锁抽奖","center"),

        /** 幸运奖兜底 */
        RULE_LUCK_AWARD("rule_luck_award", "【抽奖后规则】幸运奖兜底", "after")
        ;

        private final String code;
        private final String info;

        private final String type;

        public static boolean isCenter(String code){
            return "center".equals(LogicModel.valueOf(code.toUpperCase()).type);
        }

        public static boolean isAfter(String code){
            return "after".equals(LogicModel.valueOf(code.toUpperCase()).type);
        }

    }

}
