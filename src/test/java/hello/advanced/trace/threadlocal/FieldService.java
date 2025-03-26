package hello.advanced.trace.threadlocal;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FieldService {
    private String nameStore;

    public String logic(String name){
        log.info("save! name = {} -> nameStore = {}", name, nameStore);
        nameStore = name;
        sleep(1000);
        log.info("check! nameStore = {}", nameStore);
        return nameStore;
    }

    private void sleep(int mills){
        try{
            Thread.sleep(mills);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
