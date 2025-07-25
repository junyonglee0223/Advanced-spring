package hello.advanced.trace.strategy.code.strategy;

import hello.advanced.trace.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextV1 {
    private final Strategy strategy;

    public ContextV1(Strategy strategy) {
        this.strategy = strategy;
    }
    public void execute(){
        long startTime = System.currentTimeMillis();

        strategy.call();

        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime = {}", resultTime);
    }
}
