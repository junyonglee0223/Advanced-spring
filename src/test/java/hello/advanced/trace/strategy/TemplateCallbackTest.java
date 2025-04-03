package hello.advanced.trace.strategy;

import hello.advanced.trace.strategy.code.template.Callback;
import hello.advanced.trace.strategy.code.template.TimeLogTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TemplateCallbackTest {
    @Test
    void callbackV1(){
        TimeLogTemplate template = new TimeLogTemplate();
        template.execute(new Callback() {
            @Override
            public void call() {
                log.info("business logic 1 processing!!");
            }
        });

        template.execute(new Callback() {
            @Override
            public void call() {
                log.info("business logic 2 processing!!");
            }
        });
    }

    @Test
    void callbackV2(){
        TimeLogTemplate template = new TimeLogTemplate();
        template.execute(() -> log.info("business logic 1 processing!!"));
        template.execute(() -> log.info("business logic 2 processing!!"));
    }
}
