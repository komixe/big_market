package org.example.domain.strategy.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

}
