package org.example.domain.strategy.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardEntity {

    /**  抽奖策略ID  */
    private Long strategyId;

    /**  抽奖奖品ID  */
    private Integer awardId;

    /**  奖品对接标识  */
    private String awardKey;

    /**  奖品配置信息  */
    private String awardConfig;

    /**  奖品内容描述  */
    private String awardDesc;

}
