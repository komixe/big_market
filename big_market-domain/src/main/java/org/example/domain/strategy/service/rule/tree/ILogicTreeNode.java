package org.example.domain.strategy.service.rule.tree;


import org.example.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * 规则树接口
 */
public interface ILogicTreeNode {

    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue);
}
