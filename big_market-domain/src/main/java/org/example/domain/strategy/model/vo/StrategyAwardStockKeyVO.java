package org.example.domain.strategy.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 策略奖品库存Key标识值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAwardStockKeyVO {

    /** 策略ID */
    private Long strategyId;

    /** 奖品ID */
    private Integer awardId;
}
