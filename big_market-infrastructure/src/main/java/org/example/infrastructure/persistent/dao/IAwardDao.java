package org.example.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.infrastructure.persistent.po.Award;

import java.util.List;

/**
 * 抽奖奖品 DAO
 */
@Mapper
public interface IAwardDao {

    List<Award> queryAwardList();
}
