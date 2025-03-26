package hello.advanced.trace.threadlocal.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalService {
    private ThreadLocal<String> nameStore = new ThreadLocal<>();

    public String logic(String name){
        log.info("save! name = {} -> nameStore = {}", name, nameStore.get());
        nameStore.set(name);
        sleep(1000);
        log.info("check! nameStore = {}", nameStore.get());
        return nameStore.get();
    }

    private void sleep(int mills){
        try {
            Thread.sleep(mills);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
