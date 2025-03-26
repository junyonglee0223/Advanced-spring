package hello.advanced.trace.threadlocal;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FieldServiceTest {
    private FieldService fieldService = new FieldService();

    @Test
    void field(){
        Runnable runnableA = () -> {
            fieldService.logic("userA");
        };
        Runnable runnableB = () -> {
            fieldService.logic("userB");
        };

        Thread threadA = new Thread(runnableA);
        threadA.setName("thread-A");

        Thread threadB = new Thread(runnableB);
        threadB.setName("thread-B");

        threadA.start();
        //sleep(2000);
        sleep(100);
        threadB.start();

        sleep(3000);

        log.info("main thread exist");
    }

    private void sleep(int mills){
        try {
            Thread.sleep(mills);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
