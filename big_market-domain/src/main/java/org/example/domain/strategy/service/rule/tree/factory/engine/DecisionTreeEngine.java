package org.example.domain.strategy.service.rule.tree.factory.engine;


import org.example.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import org.example.domain.strategy.model.vo.RuleTreeNodeLineVO;
import org.example.domain.strategy.model.vo.RuleTreeNodeVO;
import org.example.domain.strategy.model.vo.RuleTreeVO;
import org.example.domain.strategy.service.rule.tree.ILogicTreeNode;
import org.example.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

import java.util.List;
import java.util.Map;

/**
 * 决策树引擎
 */
public class DecisionTreeEngine implements IDecisionTreeEngine{

    /**
     * 规则节点执行器映射表
     * key: 规则标识  value: 对应的规则逻辑处理节点
     */
    private final Map<String, ILogicTreeNode> logicTreeNodeMap;

    /** 决策树结构定义 */
    private final RuleTreeVO ruleTreeVO;

    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeMap,  RuleTreeVO ruleTreeVO) {
        this.logicTreeNodeMap = logicTreeNodeMap;
        this.ruleTreeVO = ruleTreeVO;
    }


    /**
     * 决策树执行
     *
     * @param userId 用户ID
     * @param strategyId  策略ID
     * @param awardId   奖品ID
     * @return  决策树执行完成后的策略结果
     */
    @Override
    public DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId) {
        DefaultTreeFactory.StrategyAwardVO strategyAwardData = null;

        // 1.获取决策树根节点
        String nextNode = ruleTreeVO.getTreeRootRuleNode();
        // 2.决策树节点映射表（nodeId -> nodeVO）
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        RuleTreeNodeVO ruleTreeNode = treeNodeMap.get(nextNode);

        // 3.只要存在下一个节点，就继续执行
        while (null != nextNode){
            // 3.1 根据节点规则key找到对应的逻辑执行器
            ILogicTreeNode logicTreeNode = logicTreeNodeMap.get(ruleTreeNode.getRuleKey());

            // 3.2 执行当前节点的规则逻辑
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId);

            // 3.3 本次规则判断结果
            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckTypeVO();

            // 3.4 本次节点产出的策略结果
            strategyAwardData = logicEntity.getStrategyAwardVO();

            // 3.5 根据判断结果 + 当前节点的连线，计算下一节点
            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
            ruleTreeNode = treeNodeMap.get(nextNode);
        }
        return strategyAwardData;
    }

    /**
     * 根据规则判断结果，计算下一节点
     *
     * @param matterValue 当前节点规则执行后的结果值
     * @param ruleTreeNodeLineVOList 当前节点的所有连线
     * @return 下一节点，若无则返回null
     */
    private String nextNode(String matterValue, List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList){
        // 1.当前节点没有连线，说明已到达叶子节点
        if (null == ruleTreeNodeLineVOList || ruleTreeNodeLineVOList.isEmpty()){
            return null;
        }
        // 2.遍历所有节点，找到第一个满足条件的跳转路径
        for (RuleTreeNodeLineVO nodeLine : ruleTreeNodeLineVOList){
            if(decisionLogic(matterValue, nodeLine)){
                return nodeLine.getRuleNodeTo();
            }
        }
        // 3.未命中任何连线，说明规则树配置异常
        throw new RuntimeException("决策树引擎，nextNode计算失败，未找到可执行节点！");
    }


    /**
     * 单挑规则连线的判断逻辑，判断当前规则执行结果是否满足该连线的限制条件
     *
     * @param matterValue 当前规则节点的执行结果
     * @param nodeLine 规则连线定义
     * @return 是否命中该连线
     */
    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine){
        switch (nodeLine.getRuleLimitType()){
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}
