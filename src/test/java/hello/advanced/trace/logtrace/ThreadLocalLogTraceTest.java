package hello.advanced.trace.logtrace;

import hello.advanced.trace.TraceStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalLogTraceTest {
    ThreadLocalLogTrace trace = new ThreadLocalLogTrace();

    @Test
    void begin_end_level2(){
        TraceStatus status1 = trace.begin("stat1");
        TraceStatus status2 = trace.begin("stat2");

        trace.end(status2);
        trace.end(status1);
    }
    @Test
    void begin_exception_level2(){
        TraceStatus status1 = trace.begin("stat1");
        TraceStatus status2 = trace.begin("stat2");

        trace.exception(status2, new IllegalArgumentException());
        trace.exception(status1, new IllegalArgumentException());
    }


}