package org.example.domain.strategy.service.annotation;

import org.example.domain.strategy.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于标记ILogicFilter的实现类属于哪一种策略类型
 *
 * <p>业务场景：</p>
 * <ul>
 *     <li>系统中存在多种不同的抽奖规则过滤器（ILogicFilter 的实现类）</li>
 *     <li>每个过滤器都有自己的逻辑类型（例如：权重过滤、黑名单过滤等）</li>
 *     <li>工厂 DefaultLogicFactory 会在启动时扫描这些过滤器，读取该注解中的逻辑类型字段</li>
 *     <li>最终把过滤器按 LogicModel.code 注册到 Map 中，便于运行时按类型快速获取规则实现</li>
 * </ul>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicStrategy {

    /**
     * 逻辑策略类型: 用于指定当前过滤器属于哪一种逻辑类型
     */
    DefaultLogicFactory.LogicModel logicMode();

}

