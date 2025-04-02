package hello.advanced.trace.strategy;

import hello.advanced.trace.strategy.code.StrategyLogic1;
import hello.advanced.trace.strategy.code.StrategyLogic2;
import hello.advanced.trace.strategy.code.strategy.ContextV2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ContextV2Test {
    @Test
    void strategyV1(){
        ContextV2 context = new ContextV2();
        context.execute(new StrategyLogic1());
        context.execute(new StrategyLogic2());
    }

    @Test
    void strategyV2(){
        ContextV2 context = new ContextV2();
        context.execute(new Strategy() {
            @Override
            public void call() {
                log.info("business logic 1 processing!!");
            }
        });
        context.execute(new Strategy() {
            @Override
            public void call() {
                log.info("business logic 2 processing!!");
            }
        });
    }

    @Test
    void strategyV3(){
        ContextV2 context = new ContextV2();
        context.execute(() -> log.info("business logic 1 processing!!"));
        context.execute(() -> log.info("business logic 2 processing!!"));
    }
}
