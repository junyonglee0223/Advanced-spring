# Advanced Spring Project

---
# 250320
## [Advanced Start]

기본적인 MVC 패턴을 기반으로 `Controller - Service - Repository` 구조로 구현합니다.

#### OrderRepositoryV0 (Repository Layer)
- **클래스 역할**: 데이터 저장 기능을 담당합니다.
- **메서드**:
    - `save(String itemId)`:
        - 아이템을 저장하고 1초 동안 `sleep` 합니다.
        - `itemId` 값이 `"ex"`인 경우 `IllegalArgumentException` 예외를 발생시킵니다.
    - `sleep(int millis)`:
        - `Thread.sleep()`을 이용해 지정된 시간 동안 대기합니다.
        - `InterruptedException` 발생 시 `printStackTrace()`를 호출하여 기록합니다.

#### OrderServiceV0 (Service Layer)
- **클래스 역할**: 비즈니스 로직을 담당합니다.
- **메서드**:
    - `orderItem(String itemId)`:
        - `orderRepository.save(itemId)`를 호출하여 데이터를 저장합니다.

#### OrderControllerV0 (Controller Layer)
- **클래스 역할**: 클라이언트 요청을 처리합니다.
- **필드**:
    - `orderService` (final)
- **메서드**:
    - `@GetMapping("/v0/request")`
        - **파라미터**: `itemId (String)`
        - `orderService.orderItem(itemId)` 호출 후 `"ok"` 반환


### 로그 추적기 (Trace) 구현

로그 추적을 위해 `advanced.trace` 패키지 내부에 관련 클래스를 추가합니다.

#### TraceId 클래스
- **필드**:
    - `id (String)`
    - `level (int)`
- **생성자**:
    - `TraceId()`: 랜덤 ID 생성 (`UUID.randomUUID().toString().substr(0, 8)`) 및 `level = 0` 초기화
    - `TraceId(String id, int level)`: 기존 ID와 레벨을 유지하여 생성
- **메서드**:
    - `createNextId() -> TraceId`: 현재 ID 유지, `level + 1`
    - `createPreviousId() -> TraceId`: 현재 ID 유지, `level - 1`
    - `isFirstLevel() -> boolean`: `level == 0` 여부 반환
    - `getId() -> String`: ID 반환
    - `getLevel() -> int`: 레벨 반환

#### TraceStatus 클래스
- **필드**:
    - `traceId (TraceId)`
    - `startTimeMs (Long)`
    - `message (String)`
- **생성자**:
    - `TraceStatus(TraceId traceId, Long startTimeMs, String message)`
- **메서드**:
    - 모든 필드 값 가져오기 (`getter` 제공)

### 로그 처리 로직 (`HelloTraceV1`)

`advanced.trace.hellotrace` 패키지에 로그 추적 기능을 구현합니다.

#### HelloTraceV1 (로그 처리 컴포넌트)
- **어노테이션**:
    - `@Component` (Spring 자동 인식 가능)
    - `@Slf4j` (로깅)
- **상수 정의**:
    - `START_PREFIX = "-->"`
    - `COMPLETE_PREFIX = "<--"`
    - `EX_PREFIX = "<X-"`
- **메서드**:
    - `begin(String message) -> TraceStatus`
        - `TraceId` 생성 후 로그 출력
        - `startTimeMs` 저장
    - `end(TraceStatus status)`: 완료 로그 출력 (`complete(status, null)` 호출)
    - `exception(TraceStatus status, Exception e)`: 예외 발생 시 로그 출력 (`complete(status, e)` 호출)
    - `complete(TraceStatus status, Exception e) -> void`
        - `System.currentTimeMillis()`를 활용해 실행 시간 계산
        - 정상 완료 시 `COMPLETE_PREFIX` 로그 출력
        - 예외 발생 시 `EX_PREFIX` 로그 출력
    - `addSpace(String prefix, int level) -> String`
        - 들여쓰기 적용하여 트레이스 레벨을 시각적으로 구분

### 테스트 코드 (`HelloTraceV1Test`)

#### begin_end 테스트
```log
16:12:19.052 [Test worker] INFO hello.advanced.trace.hellotrace.HelloTraceV1 -- [a7bb4051] trace_test
16:12:19.065 [Test worker] INFO hello.advanced.trace.hellotrace.HelloTraceV1 -- [a7bb4051] trace_test time = 18ms
```

#### begin_exception 테스트
```log
16:15:43.252 [Test worker] INFO hello.advanced.trace.hellotrace.HelloTraceV1 -- [c1d1f26c] trace_exception_test
16:15:43.264 [Test worker] INFO hello.advanced.trace.hellotrace.HelloTraceV1 -- [c1d1f26c] trace_exception_test time = 23ms ex = java.lang.IllegalArgumentException
```

---
# 250321
## [Advanced Start 2]
### Advanced V1 구현 정리

V1에서는 기존 MVC 구조(`Controller - Service - Repository`)에 예외 처리 로직을 명시적으로 추가하였고, 로그 추적기를 활용해 전체 호출 흐름을 기록할 수 있도록 구현하였습니다.


### 주요 변경 사항
- 모든 레이어에 try-catch 블록 추가
- 예외 발생 시 로그 추적기를 통해 예외 정보 및 수행 시간 출력


### 호출 예시

#### 정상 요청
```
GET http://localhost:8080/v1/request?itemId=hello
```

**로그 출력:**
```
2025-03-21T12:15:31.342+09:00  INFO ... orderController.request()
2025-03-21T12:15:31.345+09:00  INFO ... orderService.orderItem()
2025-03-21T12:15:31.345+09:00  INFO ... orderRepository.save()
2025-03-21T12:15:32.347+09:00  INFO ... orderRepository.save() time = 1002ms
2025-03-21T12:15:32.348+09:00  INFO ... orderService.orderItem() time = 1003ms
2025-03-21T12:15:32.350+09:00  INFO ... orderController.request() time = 1008ms
```

#### 예외 발생 요청
```
GET http://localhost:8080/v1/request?itemId=ex
```

**로그 출력:**
```
2025-03-21T12:15:36.213+09:00  INFO ... orderController.request()
2025-03-21T12:15:36.214+09:00  INFO ... orderService.orderItem()
2025-03-21T12:15:36.214+09:00  INFO ... orderRepository.save()
2025-03-21T12:15:36.214+09:00  INFO ... orderRepository.save() time = 0ms ex = java.lang.IllegalArgumentException: exception occurs!!
2025-03-21T12:15:36.215+09:00  INFO ... orderService.orderItem() time = 1ms ex = java.lang.IllegalArgumentException: exception occurs!!
2025-03-21T12:15:36.215+09:00  INFO ... orderController.request() time = 2ms
```

---
### Advanced V2 구현 정리

`HelloTraceV2`에서는 `HelloTraceV1`에 이어 **동기 호출에 대한 로그 추적 기능**을 확장하여, 중첩된 메서드 호출 간의 관계를 파악할 수 있도록 `beginSync` 메서드를 추가 구현하였습니다.


### 주요 변경 사항
- `beginSync(TraceId beforeTraceId, String message)` 메서드 추가
    - 기존 `TraceId`에서 `createNextId()`를 호출하여 다음 단계의 `TraceId` 생성
    - 이를 기반으로 하위 호출에 대한 `TraceStatus` 반환
- 중첩 호출 간 레벨(level)을 기준으로 로그에 시각적 구분(`|-->`, `|<--`, `|<X-`)을 제공


### 호출 예시 - 정상 흐름
```log
2025-03-21T12:38:38.873+09:00  INFO ... HelloTraceV2Test : Started HelloTraceV2Test in 4.106 seconds
2025-03-21T12:38:40.773+09:00  INFO ... HelloTraceV2     : [e6a79afd] stat1
2025-03-21T12:38:40.777+09:00  INFO ... HelloTraceV2     : [e6a79afd] |-->stat2
2025-03-21T12:38:40.777+09:00  INFO ... HelloTraceV2     : [e6a79afd] |<--stat2 time = 1ms
2025-03-21T12:38:40.777+09:00  INFO ... HelloTraceV2     : [e6a79afd] stat1 time = 4ms
```

- `stat1`은 최상위 메서드 호출
- `stat2`는 `beginSync`를 통해 하위 단계로 호출됨
- 동일한 `traceId`를 유지하며 `level`에 따라 `|-->` 등으로 표현


### 호출 예시 - 예외 흐름
```log
2025-03-21T12:45:53.336+09:00  INFO ... HelloTraceV2Test : Started HelloTraceV2Test in 4.062 seconds
2025-03-21T12:45:55.130+09:00  INFO ... HelloTraceV2     : [bd1e2d78] stat1
2025-03-21T12:45:55.134+09:00  INFO ... HelloTraceV2     : [bd1e2d78] |-->stat2
2025-03-21T12:45:55.134+09:00  INFO ... HelloTraceV2     : [bd1e2d78] |<X-stat2 time = 0ms ex = java.lang.IllegalArgumentException
2025-03-21T12:45:55.135+09:00  INFO ... HelloTraceV2     : [bd1e2d78] stat1 time = 5ms ex = java.lang.IllegalArgumentException
```

- `stat2` 호출 도중 예외가 발생하여 `<X-` 접두어로 출력됨
- 상위 메서드인 `stat1`도 동일한 예외를 전파받아 처리

---
### Advanced V2 - TraceId 동기 전파 및 로그 추적 적용

V2 버전에서는 `HelloTraceV2`의 `beginSync` 메서드를 활용하여 로그 추적의 레벨(Level)을 명확하게 설정하였습니다.

Controller → Service → Repository로 호출이 전파될 때, `TraceId` 객체를 넘기고, 이를 기반으로 로그 레벨이 시각적으로 표현되도록 구현하였습니다.

### 핵심 구현 내용
- `OrderControllerV2`, `OrderServiceV2`, `OrderRepositoryV2` 생성
- 각 레이어에서 `try-catch`로 예외를 처리하고, `HelloTraceV2`를 이용해 로그 추적 수행
- 메서드 호출 간 `TraceStatus`의 `TraceId`를 `beginSync`로 넘겨 하위 레벨 로그로 연결

### 정상 호출 예시
```
GET http://localhost:8080/v2/request?itemId=hello
```

**로그 출력:**
```
2025-03-21T14:21:04.575+09:00  INFO ... orderController.request()
2025-03-21T14:21:04.582+09:00  INFO ... |-->orderService.orderItem()
2025-03-21T14:21:04.583+09:00  INFO ... | |-->orderRepository.save()
2025-03-21T14:21:05.596+09:00  INFO ... | |<--orderRepository.save() time = 1012ms
2025-03-21T14:21:05.598+09:00  INFO ... |<--orderService.orderItem() time = 1016ms
2025-03-21T14:21:05.598+09:00  INFO ... orderController.request() time = 1023ms
```
- `TraceId`는 동일하게 유지되며, 깊이에 따라 `|`, `-->`, `<--` 등의 접두어가 사용됨
- 각 메서드의 수행 시간이 함께 출력되어 성능 분석에도 용이함

### 예외 발생 예시
```
GET http://localhost:8080/v2/request?itemId=ex
```

**로그 출력:**
```
2025-03-21T14:21:48.159+09:00  INFO ... orderController.request()
2025-03-21T14:21:48.159+09:00  INFO ... |-->orderService.orderItem()
2025-03-21T14:21:48.160+09:00  INFO ... | |-->orderRepository.save()
2025-03-21T14:21:48.160+09:00  INFO ... | |<X-orderRepository.save() time = 0ms ex = java.lang.IllegalArgumentException: exception occurs!!
2025-03-21T14:21:48.160+09:00  INFO ... |<X-orderService.orderItem() time = 1ms ex = java.lang.IllegalArgumentException: exception occurs!!
2025-03-21T14:21:48.161+09:00  INFO ... orderController.request() time = 2ms
```
- 하위 메서드에서 발생한 예외는 상위로 전파되며, 모든 레이어에서 `<X-` 접두어와 함께 예외 로그 출력


---

## [Local Thread 1]
### 로그 추적 기능 구현 (LogTrace & FieldLogTrace)

### 구현 목표
Spring 기반의 어플리케이션에서 메서드 호출 흐름과 예외 발생 상황을 효과적으로 추적하고, 계층적 로그 레벨을 통해 직관적인 분석이 가능하도록 로그 추적 기능을 구현하였습니다.


### 주요 구현 내용

####  `LogTrace` 인터페이스 정의
- 공통 로그 추적 기능을 정의하기 위한 인터페이스입니다.
- 메서드 시작, 종료, 예외 로그를 각각 처리할 수 있도록 `begin()`, `end()`, `exception()` 메서드를 정의합니다.

#### `FieldLogTrace` 클래스
- `LogTrace`의 구현체로, 필드(`traceHolder`)를 이용하여 현재 스레드의 trace 정보를 저장하고 관리합니다.
- `traceId`를 내부적으로 관리하며, 호출 깊이에 따라 로그 레벨을 조절합니다.

####  traceHolder 활용
- 로그 시작 시 새로운 TraceId를 생성하거나 레벨을 증가시키고,
- 로그 종료 또는 예외 발생 시 TraceId 레벨을 감소시키며 trace 흐름을 관리합니다.
- **주의:** 초기 구현에서는 TraceId 감소 로직이 누락되어 로그 레벨 중복 현상이 발생 → 이후 개선하여 올바른 trace depth 유지 가능



### 개선 사항 및 테스트 과정

####  테스트 과정 중 발견된 문제
- TraceId 감소 기능이 누락되어 하위 호출이 종료되어도 로그의 레벨이 유지되어 잘못된 들여쓰기로 출력됨.


#### 개선 후 로그 예시
- TraceId가 레벨에 따라 적절히 증가/감소하며, 정상적인 호출 흐름과 예외 흐름을 구분 가능

##### 정상 흐름
```
[c28d9d31] orderController.request()
[c28d9d31] |-->orderService.orderItem()
[c28d9d31] | |-->orderRepository.save()
[c28d9d31] | |<--orderRepository.save() time = 1009ms
[c28d9d31] |<--orderService.orderItem() time = 1012ms
[c28d9d31] orderController.request() time = 1016ms
```

##### 예외 흐름
```
[d161b855] orderController.request()
[d161b855] |-->orderService.orderItem()
[d161b855] | |-->orderRepository.save()
[d161b855] | |<X-orderRepository.save() time = 1ms ex = java.lang.IllegalArgumentException: exception occurs!!
[d161b855] |<X-orderService.orderItem() time = 1ms ex = java.lang.IllegalArgumentException: exception occurs!!
[d161b855] orderController.request() time = 3ms
```


### 추가 변경 사항

####  `TraceId` 파라미터 제거
- 기존에는 각 컴포넌트 간 traceId를 파라미터로 전달했으나, `FieldLogTrace`에서 내부적으로 상태를 관리함에 따라 해당 파라미터는 제거되었습니다.
- 코드의 간결성과 일관성 향상

---
# 250326
## [Local Thread 1] (cont)
### 동시성 문제 테스트 (FieldLogTrace의 문제점과 ThreadLocal 실험)

### 구현 배경

Spring 기반의 트레이싱 기능 구현 시, `FieldLogTrace`에서는 `traceHolder`라는 필드 변수를 통해 로그 상태를 전역적으로 관리합니다. 그러나 이 방식은 멀티 쓰레드 환경에서 **동시성 문제**를 유발할 수 있으며, 이를 테스트로 확인해보기 위해 `ThreadLocal` 기반 실험을 진행했습니다.


### 문제 상황 예시

#### FieldLogTrace의 동시성 문제

`traceHolder` 필드가 공용으로 사용되므로, 여러 쓰레드가 동시에 요청을 처리할 경우 TraceId가 서로 뒤섞이며 로그 출력에 혼란이 발생합니다. 심지어 아래와 같은 **NullPointerException**까지 유발될 수 있습니다:

```text
java.lang.NullPointerException: Cannot invoke "hello.advanced.trace.TraceId.isFirstLevel()" because "this.traceIdHolder" is null
```


### 테스트 구성

#### Gradle 설정
`test`에서도 Lombok 사용이 가능하도록 아래 설정을 `build.gradle`에 추가:

```groovy
testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
```


### 테스트 클래스 구성

#### FieldService.java
패키지: `hello.advanced.trace.threadlocal`


- `logic(String name)`: 이름 저장 후 1초 대기, 저장된 이름을 로그로 출력
- `sleep(int millis)`: thread sleep 처리



#### FieldServiceTest.java
패키지: `hello.advanced.trace.threadlocal`

- `FieldService`의 동시성 문제를 테스트하기 위한 클래스
- 두 개의 쓰레드(`userA`, `userB`)를 생성하여 `logic()` 메서드를 각각 실행
- `Thread.sleep()`을 통해 두 쓰레드의 실행 간격을 조절하여,  
  **동시 실행 시 전역 변수(nameStore)가 덮어쓰기 되는 현상**을 확인


### 테스트 결과

#### 정상 실행 (Thread 간 충분한 간격 유지)

- `threadA`의 작업이 먼저 끝난 뒤 `threadB`가 시작됨
- `nameStore`가 쓰레드 간 공유되어 있지 않음 (문제가 드러나지 않음)

```text
[thread-A] save! name = userA -> nameStore = null
[thread-A] check! nameStore = userA
[thread-B] save! name = userB -> nameStore = userA
[thread-B] check! nameStore = userB
```

#### 문제 발생 (Thread 간 간격 짧음)

- `threadB`가 `threadA`가 끝나기 전에 시작됨
- `nameStore`가 공유되어 덮어쓰기 발생
- `threadA`에서도 `userB`를 출력하게 되어 동시성 문제 확인 가능

```text
[thread-A] save! name = userA -> nameStore = null
[thread-B] save! name = userB -> nameStore = userA
[thread-A] check! nameStore = userB
[thread-B] check! nameStore = userB
```
---

## [local thread 2]
### ThreadLocal을 이용한 동시성 문제 해결

### 구현 배경

기존 `FieldService`는 단순한 인스턴스 필드를 통해 상태를 저장하기 때문에 멀티 쓰레드 환경에서 전역 상태 공유로 인해 동시성 문제가 발생한다. 이를 해결하기 위해 `ThreadLocal`을 사용하여 쓰레드별 독립적인 저장소를 가지도록 개선하였다.

### ThreadLocalService 클래스

패키지: `hello.advanced.trace.threadlocal.code`

- `nameStore` 필드를 `ThreadLocal<String>` 타입으로 선언
- `ThreadLocal`의 주요 메서드 사용:
    - `set(value)` : 현재 쓰레드에 데이터 저장
    - `get()` : 현재 쓰레드에 저장된 데이터 조회
    - `remove()` : 쓰레드 작업 종료 후 저장소 정리

```java
private ThreadLocal<String> nameStore = new ThreadLocal<>();
```

`remove()` 호출을 생략하면 메모리 누수가 발생할 수 있으므로, 사용 후 반드시 호출하여 쓰레드 로컬 상태를 초기화해야 한다.

### ThreadLocalServiceTest 테스트

패키지: `hello.advanced.trace.threadlocal`

- `ThreadLocalService`를 이용해 두 개의 쓰레드(`userA`, `userB`)를 각각 실행
- 이전 테스트와 구조는 동일하지만 내부 구현만 `ThreadLocal`로 변경
- 실행 간격이 짧아도 동시성 문제가 발생하지 않음을 확인

### 실행 결과 예시

```
12:11:52.407 [thread-A] save! name = userA -> nameStore = null
12:11:52.516 [thread-B] save! name = userB -> nameStore = null
12:11:53.419 [thread-A] check! nameStore = userA
12:11:53.529 [thread-B] check! nameStore = userB
```

두 쓰레드는 각각 독립된 `ThreadLocal` 저장소를 사용하기 때문에 서로의 데이터에 간섭하지 않음. 이를 통해 전역 필드로 인한 동시성 문제를 완전히 해결할 수 있다.

---
## local thread 2
### ThreadLocal 기반 로그 추적기 구현

### 구현 배경

기존의 `FieldLogTrace`는 전역 필드(`traceHolder`)를 통해 상태를 저장했기 때문에, 멀티 쓰레드 환경에서 로그 트레이스 정보가 서로 뒤섞이는 **동시성 문제**가 발생하였다. 이를 해결하기 위해 각 쓰레드마다 독립적인 저장소를 가지는 `ThreadLocal`을 사용한 로그 추적기를 새로 구현하였다.

### ThreadLocalLogTrace 클래스

패키지: `hello.advanced.trace.logtrace`

- 기존 `FieldLogTrace`의 구조와 로직을 그대로 유지
- `traceHolder` 필드를 `ThreadLocal<TraceId>` 타입으로 선언하여, 쓰레드마다 독립적인 TraceId 관리

```java
private ThreadLocal<TraceId> traceHolder = new ThreadLocal<>();
```

- 주요 메서드 사용 방식:
    - `traceHolder.set(new TraceId())` : 새로운 트레이스 시작 시 설정
    - `traceHolder.get()` : 현재 쓰레드의 트레이스 상태 조회
    - `traceHolder.remove()` : 모든 로그 종료 시 반드시 호출하여 메모리 누수 방지

### 테스트 결과

#### 단위 테스트 실행

- `begin_end_level2()`, `begin_exception_level2()` 테스트를 통해 중첩 호출 및 예외 발생 상황 확인

```text
[77a027bd] stat1
[77a027bd] |-->stat2
[77a027bd] |<--stat2 time = 1ms
[77a027bd] stat1 time = 28ms
```

```text
[1c04b783] stat1
[1c04b783] |-->stat2
[1c04b783] |<X-stat2 time = 1ms ex = java.lang.IllegalArgumentException
[1c04b783] stat1 time = 20ms ex = java.lang.IllegalArgumentException
```

### 실제 어플리케이션 적용

```java
@Bean
public LogTrace logTrace() {
    // 기존 FieldLogTrace 제거
    return new ThreadLocalLogTrace();
}
```

### 실제 요청 테스트 결과

- 여러 사용자의 요청이 동시에 들어오더라도 각 요청이 독립적인 로그 트레이스를 생성하여 출력됨
- TraceId가 각각 다르고, 로그 들여쓰기도 요청별로 올바르게 작동함

```text
[5c190322] orderController.request()
[64cb9e97] orderController.request()
[2c8d738c] orderController.request()
...
[5c190322] orderController.request() time = 1006ms
[64cb9e97] orderController.request() time = 1007ms
[2c8d738c] orderController.request() time = 1006ms
```

---
# 250402
## [Template Method Pattern and Callback Pattern]

### Template Method 패턴을 활용한 로직 리팩토링

기존 로직은 **비즈니스 로직**과 **log trace**가 한 클래스 내에 함께 구현되어 있어 다음과 같은 문제점이 있었습니다:

- **중복 코드**가 발생함
- 비즈니스 로직 변경 시 **로직과 로그 처리**가 함께 수정되어야 하므로 **리팩토링 비용**이 큼

이를 해결하기 위해 **변하는 영역과 변하지 않는 영역을 분리**하고자 했으며, **Template Method 패턴**을 활용하였습니다.



### 구현 구조

```
test
└── .. 
    └── template
        └── code
            ├── AbstractTemplate.java
            ├── SubClassLogic1.java
            └── SubClassLogic2.java

```


### AbstractTemplate

- `AbstractTemplate` 클래스는 **공통적인 시간 측정 로직**을 담당합니다.
- `call()` 메서드는 `protected abstract void call()`로 선언되어 있으며, **변하는 영역**은 이 메서드를 오버라이딩하여 구현합니다.
- `execute()` 메서드는 다음과 같은 흐름을 수행합니다:
    1. 시작 시간 측정
    2. `call()` 실행
    3. 종료 시간 측정 및 로그 출력


### SubClassLogic1, SubClassLogic2

- `AbstractTemplate`을 상속받아 각각 `call()` 메서드를 구현
- 현재는 단순하게 로그 출력 (`"business logic 1 processing!!"`, `"business logic 2 processing!!"`) 으로 구현되어 있음


### TemplateMethodTest

- `templateMethodV1()` 테스트 메서드에서 각각의 서브 클래스 인스턴스를 생성하고 `execute()`를 호출
- 결과적으로 각 로직의 실행 시간 로그가 출력됨


### 실행 결과 예시

```
10:41:12.987 [Test worker] INFO hello.advanced.trace.template.code.SubClassLogic1 -- business logic 1 processing!!
10:41:12.995 [Test worker] INFO hello.advanced.trace.template.code.AbstractTemplate -- resultTime = 13
10:41:12.999 [Test worker] INFO hello.advanced.trace.template.code.SubClassLogic2 -- business logic 2 processing!!
10:41:13.000 [Test worker] INFO hello.advanced.trace.template.code.AbstractTemplate -- resultTime = 1
```

### 익명 내부 클래스 활용

이전 방식처럼 별도의 SubClass를 만들어 `AbstractTemplate`을 상속받아 사용할 수도 있지만,  
정말 간단한 로직이거나 단일 메서드만 필요한 경우에는 **직접 클래스를 생성할 필요 없이 익명 내부 클래스(anonymous inner class)** 를 활용할 수 있습니다.

### 실행 결과 예시 (익명 내부 클래스)

```
11:01:39.741 [Test worker] INFO hello.advanced.trace.template.TemplateMethodTest -- class name 1 = class hello.advanced.trace.template.TemplateMethodTest$1
11:01:39.752 [Test worker] INFO hello.advanced.trace.template.TemplateMethodTest -- business logic 1 processing!!
11:01:39.752 [Test worker] INFO hello.advanced.trace.template.code.AbstractTemplate -- resultTime = 1
11:01:39.753 [Test worker] INFO hello.advanced.trace.template.TemplateMethodTest -- class name 2 = class hello.advanced.trace.template.TemplateMethodTest$2
11:01:39.754 [Test worker] INFO hello.advanced.trace.template.TemplateMethodTest -- business logic 2 processing!!
11:01:39.755 [Test worker] INFO hello.advanced.trace.template.code.AbstractTemplate -- resultTime = 1

```
---

## [Template Method Pattern and Callback Pattern] (cont)
### 템플릿 메서드 패턴과 콜백 패턴

기존 로직은 비즈니스 로직과 로그 추적(Log Trace)이 함께 구현되어 있어 중복 코드가 많고, 리팩토링 시 유지보수 비용이 컸습니다. 이를 해결하기 위해 **템플릿 메서드 패턴(Template Method Pattern)** 을 활용하여 **변하는 부분(비즈니스 로직)** 과 **변하지 않는 부분(공통 처리 로직: 로그 추적)** 을 분리하였습니다.

### 디렉토리 구조

```
src
└── main
    └── ..
        ├── trace
        │   └── template
        │       └── AbstractTemplate.java
        └── app
            └── v4
                ├── OrderControllerV4.java
                ├── OrderServiceV4.java
                └── OrderRepositoryV4.java
```

### AbstractTemplate<T>

- 제네릭 타입 `<T>`을 적용한 추상 클래스
- 공통 로그 처리 로직은 이 클래스에서 담당하고, 비즈니스 로직은 `call()` 메서드를 통해 하위 클래스에서 구현합니다.

외부에서 로그 추적 객체(`LogTrace`)를 주입받습니다.


시작 로그 → `call()` 실행 → 종료 로그  
예외 발생 시 예외 로그 처리



### 실제 적용: app v4

기존 `OrderController`, `OrderService`, `OrderRepository`의 로직에서 로그 추적 코드가 중복되어 있었음  
`app.v4` 패키지를 생성하고, 내부 클래스에서 직접 로그 처리하던 방식 대신 `AbstractTemplate`을 활용함  
각 클래스 내부에서 익명 내부 클래스를 사용하여 `call()` 메서드를 구현하고, `execute()`로 실행

#### 주의 사항
`AbstractTemplate`이 제네릭 타입이므로, `call()`과 `execute()`의 반환 타입도 명시해야 함  
예: `new AbstractTemplate<String>(trace) { ... }`

### 실행 로그 예시

```
2025-04-02T11:48:20.441+09:00  INFO 27520 --- [advanced] [nio-8080-exec-1] h.a.trace.logtrace.ThreadLocalLogTrace   : [f53791a9] OrderController.request()
2025-04-02T11:48:20.445+09:00  INFO 27520 --- [advanced] [nio-8080-exec-1] h.a.trace.logtrace.ThreadLocalLogTrace   : [f53791a9] |-->OrderService.orderItem()
2025-04-02T11:48:20.445+09:00  INFO 27520 --- [advanced] [nio-8080-exec-1] h.a.trace.logtrace.ThreadLocalLogTrace   : [f53791a9] | |-->OrderRepository.save()
2025-04-02T11:48:21.450+09:00  INFO 27520 --- [advanced] [nio-8080-exec-1] h.a.trace.logtrace.ThreadLocalLogTrace   : [f53791a9] | |<--OrderRepository.save() time = 1004ms
2025-04-02T11:48:21.462+09:00  INFO 27520 --- [advanced] [nio-8080-exec-1] h.a.trace.logtrace.ThreadLocalLogTrace   : [f53791a9] |<--OrderService.orderItem() time = 1017ms
2025-04-02T11:48:21.509+09:00  INFO 27520 --- [advanced] [nio-8080-exec-1] h.a.trace.logtrace.ThreadLocalLogTrace   : [f53791a9] OrderController.request() time = 1021ms
```
---
## [Template Method Pattern and Callback Pattern] (cont)

### 전략(Strategy) 패턴을 활용한 비즈니스 로직 분리

기존 로직은 공통 처리(예: 실행 시간 측정)과 비즈니스 로직이 한 메서드 안에 함께 구현되어 있어 변경과 재사용이 어려운 구조였습니다. 이를 개선하기 위해 **전략(Strategy) 패턴**을 적용하였습니다.

비즈니스 로직을 독립된 전략(Strategy)으로 분리하고, 공통 로직은 Context가 관리하도록 설계하여 유지보수성과 유연성을 높였습니다.

### 디렉토리 구조

```
test
└── ..
    └── trace
        └── strategy
            ├── ContextV1Test.java
            ├── Strategy.java
            └── code
                ├── StrategyLogic1.java
                ├── StrategyLogic2.java
                └── strategy
                    └── ContextV1.java
```

### Strategy 인터페이스
- 비즈니스 로직의 공통 인터페이스
- 모든 전략 클래스는 `call()` 메서드를 구현

### StrategyLogic1, StrategyLogic2
- 각각의 비즈니스 로직을 별도로 정의하여 필요에 따라 쉽게 교체 가능

### ContextV1
- 공통 처리 로직(시간 측정)은 `ContextV1`에서 담당
- 실행할 전략은 생성자 주입을 통해 외부에서 설정

### ContextV1Test
- 전략을 외부에서 주입하여 `ContextV1`을 통해 실행
- 동일한 Context 로직을 다양한 전략과 조합 가능

### 실행 결과 예시

```
15:32:47.323 [Test worker] INFO hello.advanced.trace.strategy.code.StrategyLogic1 -- business logic 1 processing !!
15:32:47.334 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 13
15:32:47.339 [Test worker] INFO hello.advanced.trace.strategy.code.StrategyLogic2 -- business logic 2 processing!!
15:32:47.341 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 2
```

---
## [Template Method Pattern and Callback Pattern] (cont)
### 전략(Strategy) 패턴 - 다양한 구현 방식

`Strategy` 패턴은 실행 시점에 다양한 전략(비즈니스 로직)을 유연하게 주입할 수 있는 패턴입니다. Java에서는 이를 여러 방식으로 구현할 수 있으며, 특히 **익명 내부 클래스**와 **람다 표현식**을 활용하면 더욱 간결하게 작성할 수 있습니다. 아래는 동일한 `ContextV1`을 사용하여 전략을 전달하는 다양한 방법입니다.

### 1. 익명 내부 클래스 변수로 선언

`Strategy` 인터페이스를 익명 내부 클래스로 직접 구현한 후 변수로 선언하여 Context에 주입합니다.

```java
Strategy strategyLogic1 = new Strategy() {
    @Override
    public void call() {
        log.info("business logic 1 processing!!");
    }
};
log.info("strategyLogic1 = {}", strategyLogic1.getClass());
ContextV1 context1 = new ContextV1(strategyLogic1);
context1.execute();
```

실행 로그 예시:
```
15:51:04.489 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- strategyLogic1 = class hello.advanced.trace.strategy.ContextV1Test$1
15:51:04.502 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- business logic 1 processing!!
15:51:04.503 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 1
15:51:04.503 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- strategyLogic2 = class hello.advanced.trace.strategy.ContextV1Test$2
15:51:04.504 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- business logic 2 processing!!
15:51:04.505 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 1
```

### 2. 익명 내부 클래스를 직접 Context 생성자에 전달

Strategy 객체를 변수로 따로 선언하지 않고, Context 객체를 생성할 때 바로 익명 클래스를 전달하는 방식입니다.

```java
ContextV1 context1 = new ContextV1(new Strategy() {
    @Override
    public void call() {
        log.info("business logic 1 processing!!");
    }
});
```

실행 로그 예시:
```
15:58:11.654 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- context1 = class hello.advanced.trace.strategy.code.strategy.ContextV1
15:58:11.668 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- business logic 1 processing!!
15:58:11.668 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 0
15:58:11.669 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- context2 = class hello.advanced.trace.strategy.code.strategy.ContextV1
15:58:11.669 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- business logic 2 processing!!
15:58:11.669 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 0
```

### 3. 람다 표현식으로 전략 전달

`Strategy` 인터페이스는 함수형 인터페이스(추상 메서드 1개)이므로, 람다 표현식으로도 전략을 간단하게 전달할 수 있습니다.

```java
ContextV1 context1 = new ContextV1(() -> log.info("business logic 1 processing!!"));
context1.execute();
```

실행 로그 예시:
```
16:02:20.190 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- business logic 1 processing!!
16:02:20.202 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 15
16:02:20.208 [Test worker] INFO hello.advanced.trace.strategy.ContextV1Test -- business logic 2 processing!!
16:02:20.210 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV1 -- resultTime = 2
```
---
## [Template Method Pattern and Callback Pattern] (cont)
### 전략(Strategy) 패턴 - 실행 시점 전략 주입 방식 (ContextV2)

기존의 `ContextV1`은 생성자 주입을 통해 전략(`Strategy`)을 설정하고, 이후 실행 시 동일한 전략을 사용하는 방식입니다. 반면 `ContextV2`는 **실행 시점에 전략을 파라미터로 전달**하는 방식으로 전략의 유연성을 더 높였습니다.

이 방식은 실행할 때마다 다른 전략을 전달할 수 있으므로, **한정된 컨텍스트 안에서 전략만 유연하게 바꾸고 싶은 경우**에 적합합니다.

### 디렉토리 구조

```
test
└── ..
    └── trace
        └── strategy
            ├── ContextV2Test.java
            ├── Strategy.java
            └── code
                ├── StrategyLogic1.java
                ├── StrategyLogic2.java
                └── strategy
                    └── ContextV2.java
```

### ContextV2
- 전략은 `execute()` 메서드 호출 시점에 파라미터로 전달
- 실행할 때마다 다른 전략을 선택 가능


### 실행 로그 예시

```
17:01:53.317 [Test worker] INFO hello.advanced.trace.strategy.code.StrategyLogic1 -- business logic 1 processing !!
17:01:53.327 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV2 -- resultTime = 13
17:01:53.336 [Test worker] INFO hello.advanced.trace.strategy.code.StrategyLogic2 -- business logic 2 processing!!
17:01:53.336 [Test worker] INFO hello.advanced.trace.strategy.code.strategy.ContextV2 -- resultTime = 0
```

### 정리

- `ContextV1`은 **전략을 한 번 설정하고 여러 번 재사용**할 수 있어, 주로 **애플리케이션 조립 시점(설정 클래스 등)**에서 사용하기 적합함
- `ContextV2`는 **전략을 실행 시점에 유연하게 바꿔 사용**할 수 있어, **간단한 코드의 일부분만 다르게 실행하고 싶은 경우**에 더 적합함
- 두 방식 모두 상황에 따라 전략적으로 선택 가능하며, **재사용성**과 **유연성** 측면에서 각각 장단점이 있음

---
# 250402
## [Template Method Pattern and Callback Pattern] (cont)
### 템플릿 콜백 패턴 구현

기존 전략 패턴에서는 전략(Strategy) 인터페이스를 정의하고, Context에서 해당 전략을 실행하는 구조를 가졌습니다. 이 구조를 더 간단하게 만들기 위해 **템플릿 콜백 패턴(Template Callback Pattern)** 을 사용하여 구현했습니다.

템플릿 콜백 패턴은 고정된 템플릿 메서드 로직 안에서, **변하는 부분만 콜백으로 분리** 하여 유연하게 처리할 수 있도록 합니다.

### 디렉토리 구조

```
test
└── ..
    └── strategy
        ├── TemplateCallbackTest.java
        └── code
            └── template
                ├── Callback.java
                └── TimeLogTemplate.java
```

### Callback 인터페이스
- 메서드가 하나뿐인 인터페이스로, 함수형 인터페이스로도 활용 가능
- 익명 내부 클래스 또는 람다로 간편하게 구현 가능

### TimeLogTemplate 클래스
- 공통 처리 로직인 시간 측정은 템플릿으로 제공
- 비즈니스 로직은 콜백으로 전달받아 실행

### TemplateCallbackTest 테스트 클래스
- `callbackV1`, `callbackV2` 두 테스트 모두 익명 내부 클래스를 사용
- `callbackV2`는 `Callback`이 단일 메서드를 가지므로 **람다 표현식**으로 간단하게 작성 가능

### 실행 로그 예시

```
10:50:27.590 [Test worker] INFO hello.advanced.trace.strategy.TemplateCallbackTest -- business logic 1 processing!!
10:50:27.598 [Test worker] INFO hello.advanced.trace.strategy.code.template.TimeLogTemplate -- resultTime = 13
10:50:27.603 [Test worker] INFO hello.advanced.trace.strategy.TemplateCallbackTest -- business logic 2 processing!!
10:50:27.604 [Test worker] INFO hello.advanced.trace.strategy.code.template.TimeLogTemplate -- resultTime = 1
```
---
## [Template Method Pattern and Callback Pattern] (cont)
### 템플릿 콜백 패턴을 실제 서비스에 적용 (app v5)

기존 로그 추적(LogTrace) 기능은 서비스, 컨트롤러, 리포지토리 등 각 계층에서 직접 로그 시작/종료/예외를 처리하고 있었습니다. 이로 인해 중복되는 코드가 많고, 로직이 변경될 경우 유지보수가 어려웠습니다.

이를 개선하기 위해 템플릿 콜백 패턴을 적용하여 공통 로직은 `TraceTemplate`이 담당하고, 실제 비즈니스 로직은 콜백 형태로 위임하는 구조를 구성했습니다.

### 디렉토리 구조

```
src
└── ...
    └── trace
        └── callback
            ├── TraceCallback.java
            └── TraceTemplate.java
    └── advanced
        └── app
            └── v5
                ├── OrderControllerV5.java
                ├── OrderServiceV5.java
                └── OrderRepositoryV5.java
```

### TraceCallback<T> 인터페이스

```java
public interface TraceCallback<T> {
    T call();
}
```

- 템플릿 콜백에서 실행할 비즈니스 로직을 정의하는 인터페이스
- 제네릭 타입 `<T>`를 사용하여 반환값을 유연하게 설정 가능
- 함수형 인터페이스이기 때문에 람다식으로도 전달 가능

### TraceTemplate 클래스
- 공통 로그 처리 로직을 내부에 포함
- 로그 메시지와 콜백을 인자로 받아 실행
- 예외 처리와 실행 시간 측정까지 일괄 처리

### 서비스 계층(app v5) 적용

`app.v4`에서 복사한 컨트롤러, 서비스, 리포지토리 클래스에 템플릿 콜백을 적용합니다.

- 모든 클래스에서 기존 `LogTrace` 필드는 제거
- 대신 `TraceTemplate`을 필드로 선언하고, 생성자 주입으로 `LogTrace`를 설정한 `TraceTemplate`을 전달받음
- 기존 `trace.begin()`, `trace.end()` 등을 사용하던 부분을 `template.execute()`로 변경
- 내부 비즈니스 로직은 `TraceCallback<T>`의 `call()` 메서드로 전달

예시:
```java
return traceTemplate.execute("OrderService.orderItem()", () -> {
    orderRepository.save(itemId);
    return null;
});
```

### 실행 로그 예시

```
2025-04-03T11:39:50.666+09:00  INFO OrderController.request()
2025-04-03T11:39:50.670+09:00  INFO |-->OrderService.orderItem()
2025-04-03T11:39:50.671+09:00  INFO | |-->OrderRepository.save()
2025-04-03T11:39:51.686+09:00  INFO | |<--OrderRepository.save() time = 1015ms
2025-04-03T11:39:51.687+09:00  INFO |<--OrderService.orderItem() time = 1017ms
2025-04-03T11:39:51.687+09:00  INFO OrderController.request() time = 1022ms
```





