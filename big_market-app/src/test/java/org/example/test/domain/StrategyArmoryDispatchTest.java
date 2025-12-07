package org.example.test.domain;


import lombok.extern.slf4j.Slf4j;
import org.example.domain.strategy.service.armory.IStrategyArmory;
import org.example.domain.strategy.service.armory.IStrategyDispatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyArmoryDispatchTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IStrategyDispatch strategyDispatch;


    @Before
    public void test_strategyArmory(){
        boolean success = strategyArmory.assembleLotteryStrategy(100001L);
        log.info("测试结果: {}", success);
    }

    @Test
    public void test_getAssembleRandomVal(){
        Long strategyId = 100002L;
        log.info("测试结果:{} - 奖品ID值", strategyDispatch.getRandomAwardId(strategyId));
        log.info("测试结果:{} - 奖品ID值", strategyDispatch.getRandomAwardId(strategyId));
        log.info("测试结果:{} - 奖品ID值", strategyDispatch.getRandomAwardId(strategyId));
    }

    @Test
    public void test_getRandomAwardId(){
        Long strategyId = 100001L;
        log.info("测试结果: {} - 奖品ID值", strategyDispatch.getRandomAwardId(strategyId));
    }

    @Test
    public void test_getRandomAwardId_ruleWeightValue(){
        Long strategyId = 100001L;
        log.info("测试结果:{} - 4000 策略配置", strategyDispatch.getRandomAwardId(strategyId, "4000:102,103,104,105"));
        log.info("测试结果:{} - 5000 策略配置", strategyDispatch.getRandomAwardId(strategyId, "5000:102,103,104,105,106,107"));
        log.info("测试结果:{} - 6000 策略配置", strategyDispatch.getRandomAwardId(strategyId, "6000:102,103,104,105,106,107,108,109"));
    }

}

