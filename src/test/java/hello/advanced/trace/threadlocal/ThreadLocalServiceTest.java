package hello.advanced.trace.threadlocal;

import hello.advanced.trace.threadlocal.code.ThreadLocalService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ThreadLocalServiceTest {
    private final ThreadLocalService threadLocalService = new ThreadLocalService();

    @Test
    void field(){
        log.info("main thread start!!!");

        Runnable runnableA = () -> {
            threadLocalService.logic("userA");
        };
        Runnable runnableB = () -> {
            threadLocalService.logic("userB");
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
        log.info("main thread finish!!!");
    }

    private void sleep(int mills){
        try {
            Thread.sleep(mills);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
