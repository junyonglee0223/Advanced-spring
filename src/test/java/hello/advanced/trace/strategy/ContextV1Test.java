package hello.advanced.trace.strategy;

import hello.advanced.trace.strategy.code.StrategyLogic1;
import hello.advanced.trace.strategy.code.StrategyLogic2;
import hello.advanced.trace.strategy.code.strategy.ContextV1;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ContextV1Test {

    @Test
    void strategyV0(){
        logic1();
        logic2();
    }

    private void logic1(){
        long startTime = System.currentTimeMillis();

        log.info("business logic 1 processing!!");

        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime = {}", resultTime);
    }
    private void logic2(){
        long startTime = System.currentTimeMillis();

        log.info("business logic 2 processing!!");

        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime = {}", resultTime);
    }

    @Test
    void strategyV1(){

        Strategy strategy1 = new StrategyLogic1();
        ContextV1 context1 = new ContextV1(strategy1);
        context1.execute();

        Strategy strategy2 = new StrategyLogic2();
        ContextV1 context2 = new ContextV1(strategy2);
        context2.execute();
    }

    @Test
    void strategyV2(){
        Strategy strategy1 = new Strategy() {
            @Override
            public void call() {
                log.info("business logic 1 processing!!");
            }
        };
        ContextV1 context1 = new ContextV1(strategy1);

        Strategy strategy2 = new Strategy() {
            @Override
            public void call() {
                log.info("business logic 2 processing!!");
            }
        };
        ContextV1 context2 = new ContextV1(strategy2);

        log.info("strategyLogic1 = {}", strategy1.getClass());
        context1.execute();
        log.info("strategyLogic2 = {}", strategy2.getClass());
        context2.execute();
    }

    @Test
    void strategyV3(){
        ContextV1 context1 = new ContextV1(new Strategy() {
            @Override
            public void call() {
                log.info("business logic 1 processing!!");
            }
        });
        ContextV1 context2 = new ContextV1(new Strategy() {
            @Override
            public void call() {
                log.info("business logic 2 processing!!");
            }
        });

        log.info("context1 = {}", context1.getClass());
        context1.execute();

        log.info("context2 = {}", context2.getClass());
        context2.execute();
    }

    @Test
    void strategyV4(){
        ContextV1 context1 = new ContextV1(() -> log.info("business logic 1 processing!!"));
        ContextV1 context2 = new ContextV1(() -> log.info("business logic 2 processing!!"));

        context1.execute();
        context2.execute();
    }
}
