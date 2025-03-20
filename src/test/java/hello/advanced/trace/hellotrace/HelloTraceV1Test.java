package hello.advanced.trace.hellotrace;

import hello.advanced.trace.TraceStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloTraceV1Test {
    @Test
    void begin_test(){
        HelloTraceV1 helloTraceV1 = new HelloTraceV1();
        TraceStatus status = helloTraceV1.begin("trace_test");
        helloTraceV1.end(status);
    }

    @Test
    void begin_end(){
        HelloTraceV1 helloTraceV1 = new HelloTraceV1();
        TraceStatus status = helloTraceV1.begin("trace_exception_test");
        helloTraceV1.exception(status, new IllegalArgumentException());
    }

}