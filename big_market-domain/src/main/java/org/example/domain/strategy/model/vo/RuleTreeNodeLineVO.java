package org.example.domain.strategy.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 规则树节点指向线对象，用于衔接 from->to 节点链路关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleTreeNodeLineVO {

    /** 规则树ID */
    private String treeId;

    /** 规则Key节点From */
    private String ruleNodeFrom;

    /** 规则Key节点To */
    private String ruleNodeTo;

    /** 限定类型；1:=;2:>;3:<;4:>=;5<=;6:enum[枚举范围] */
    private RuleLimitTypeVO ruleLimitType;

    /** 限定值 */
    private RuleLogicCheckTypeVO ruleLimitValue;


}
