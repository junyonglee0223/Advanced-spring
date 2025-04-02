package hello.advanced.trace.strategy.code;

import hello.advanced.trace.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StrategyLogic2 implements Strategy {
    @Override
    public void call() {
        log.info("business logic 2 processing!!");
    }
}
