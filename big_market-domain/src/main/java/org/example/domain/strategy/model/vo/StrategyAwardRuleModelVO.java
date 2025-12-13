package org.example.domain.strategy.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import org.example.types.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖策略奖品规则模型值对象
 *
 * 用于承载从数据库中查询得到的规则模型集合数据
 * 该对象本身不具备唯一业务ID，仅作为规则解析与分发的中间值对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardRuleModelVO {

    private String ruleModels;

    /**
     * 获取抽奖中规则列表
     * @return 抽奖中规则数组
     */
    public String[] raffleCenterRuleModelList() {
        List<String> ruleModelList = new ArrayList<>();
        String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
        // 1.筛选属于抽奖中规则的规则模型
        for (String ruleModelValue : ruleModelValues) {
            if (DefaultLogicFactory.LogicModel.isCenter(ruleModelValue)) {
                ruleModelList.add(ruleModelValue);
            }
        }
        // 2.转换为数组返回
        return ruleModelList.toArray(new String[0]);
    }

}
