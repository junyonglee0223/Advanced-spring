package hello.advanced.trace;

public class TraceStatus {
    private TraceId traceId;
    private Long startTimeMs;
    private String message;

    public TraceStatus(TraceId traceId, Long startTimeMs, String message){
        this.traceId = traceId;
        this.startTimeMs = startTimeMs;
        this.message = message;
    }
    public TraceId getTraceId(){return traceId;}
    public Long getStartTimeMs(){return startTimeMs;}
    public String getMessage(){return message;}
}
