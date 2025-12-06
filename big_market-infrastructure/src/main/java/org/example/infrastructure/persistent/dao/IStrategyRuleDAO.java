package org.example.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.persistent.po.StrategyRule;

import java.util.List;

/**
 * 策略规则 DAO
 */
@Mapper
public interface IStrategyRuleDAO {

    List<StrategyRule> queryStrategyRuleList();

}
