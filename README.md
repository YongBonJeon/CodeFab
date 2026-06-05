# CodeFab

CodeFab 는 트리 순회(tree-walking) 방식의 인터프리터입니다.
파이프라인은 `Tokenizer → Parser → Optimizer → Checker → Executor` 순서로 동작합니다.

```
소스코드 → [Tokenizer] → 토큰 → [Parser] → AST
        → [Optimizer] 상수 폴딩 → [Checker] 의미분석 + 정적 바인딩 거리계산
        → [Executor] 실행
```

## 실행 방법 (공장 제어 쉘)

`factory.bat` 는 인터프리터 팩토리를 운용하는 인터페이스이며, 3가지 모드를 지원합니다.
(Windows PowerShell / cmd 환경 기준)

| 모드            | 명령                                    | 설명                          |
|----------------|----------------------------------------|-----------------------------|
| 프롬프트(REPL)  | `.\factory.bat`                        | 대화형 실행                    |
| 파일 모드        | `.\factory.bat run <파일경로>`           | `.txt` 소스 파일을 읽어 실행     |
| 디버그 모드      | `.\factory.bat debug <파일경로>`         | Stmt 단위로 멈추며 점검         |

```
PS> .\factory.bat
PS> .\factory.bat run scripts\feature_demo.txt
PS> .\factory.bat debug scripts\debug_demo.txt
```

> 모드 키워드 없이 파일 경로만 주면(`.\factory.bat <파일경로>`) 파일 모드로 동작합니다.
> 최초 실행 시 `factory.bat` 가 자동으로 실행 jar(`build\libs\factory.jar`)를 빌드합니다.
> 소스 변경 후에는 `.\gradlew.bat jar` 로 다시 빌드하세요.

| 개발용 명령           | 설명             |
|---------------------|----------------|
| `.\gradlew.bat test`  | 전체 유닛 테스트   |
| `.\gradlew.bat build` | 빌드 + 테스트 + jar |

### 1) 프롬프트 모드 (REPL)

- `>>>` 프롬프트에 코드를 입력하고, **빈 줄(엔터를 한 번 더)** 을 입력하면 실행됩니다.
- 여러 줄에 걸친 코드(함수 등)는 이어서 입력할 수 있으며, 연속 입력 줄은 `...` 으로 표시됩니다.
- 전역 변수 저장소는 세션 종료 전까지 유지됩니다.
- `exit` 또는 `quit` 입력 시 종료 (Ctrl+D 도 가능)

```
>>> print 1 + 2;
...                  (빈 줄 = 실행)
3
>>> Func add(a, b) {
...   return a + b;
... }
... print add(3, 7);
...                  (빈 줄 = 실행)
10
>>> exit
```

### 2) 파일 모드

- 파일이 없으면 명확한 오류 메시지 출력 후 종료
- 실행 중 런타임 오류 발생 시 **오류 발생 줄 번호와 함께** 출력하고 즉시 종료

```
PS> .\factory.bat run scripts\feature_demo.txt
```

### 3) 디버그 모드

소스를 한 `Stmt` 단위로 멈추며 실행 상태를 점검합니다. stepping 단위는 Stmt 이고,
`watch` 는 변수 저장소에서 직접 조회합니다.

```
PS> .\factory.bat debug scripts\debug_demo.txt
[DEBUG] 소스코드 로딩: scripts\debug_demo.txt
[DEBUG] 1번째 줄에서 정지 → var a = 3;
> step
[DEBUG] 2번째 줄에서 정지 → var b = a + 1;
> break 6
[DEBUG] 6번째 줄에 breakpoint 설정
> continue
[DEBUG] 6번째 줄에서 정지 (breakpoint) → print a;
```

| 명령              | 설명                                       |
|------------------|------------------------------------------|
| `step`           | 현재 Stmt 실행 후 다음 Stmt 에서 정지 (블록 진입 O) |
| `next`           | 현재 Stmt 실행 (같은 깊이의 다음 Stmt 에서 정지)    |
| `break <줄번호>`  | 해당 줄에 breakpoint 설정                     |
| `breakpoints`    | 현재 설정된 breakpoint 목록 출력               |
| `remove <줄번호>` | breakpoint 해제                            |
| `continue`       | 다음 breakpoint 까지 실행                     |
| `watch <변수명>`  | 변수를 감시 목록에 추가 (정지 시마다 값 자동 출력)    |
| `unwatch <변수명>`| 감시 목록에서 제거                            |
| `watches`        | 감시 중인 변수 목록과 값 출력 (가장 인접한 스코프)    |
| `inspect`        | 현재 스코프의 모든 변수와 값/타입 출력            |

## 지원 문법 (Custom Language)

### 변수 · 출력 · 제어문

```
var a = 3;                       // 변수 선언
a = a + 1;                       // 대입
print a;                         // 출력

if (a > 0) print "big"; else print "small";
for (var i = 0; i < 3; i = i + 1) { print i; }

{ var a = 99; print a; }         // 블록 (스코프)
```

### 함수

```
Func add(a, b) {                 // 함수 선언 (Func / func 둘 다 가능)
  return a + b;
}
var ret = add(3, 7);             // 호출 + 반환값
print ret;                       // 10

Func fact(n) {                   // 재귀 호출
  if (n <= 1) return 1;
  return n * fact(n - 1);
}
print fact(5);                   // 120

Func nothing() { return; }       // return; → null(nil) 반환
```

함수 관련 오류(메시지 출력 후 오류 발생):

| 상황                  | 예시                       |
|----------------------|--------------------------|
| 함수 외부에서 return    | `return 5;`              |
| 파라미터 이름 중복       | `Func foo(a, a) { ... }` |
| 함수가 아닌 대상 호출    | `var x = "hi"; x();`     |
| 인자 개수 불일치        | `Func f(a,b,c){...} f(1,2);` |

### 정적 배열

```
var arr = Array(3);              // 크기 3, [null, null, null]
arr[0] = 10;
arr[1] = 20;
print arr[0];                    // 10
var i = 2;
arr[i - 1] = 7;                  // 인덱스로 표현식 사용 가능
```

배열 관련 런타임 오류:

| 상황                    | 예시                  |
|------------------------|---------------------|
| 범위를 벗어난 인덱스       | `arr[5]`            |
| 숫자가 아닌 인덱스        | `arr["hello"]`      |
| 배열이 아닌 대상에 인덱싱  | `var x = 10; x[0]`  |
| 배열 크기가 숫자가 아님    | `Array("hi")`       |

### 연산자

| 분류   | 연산자                                  |
|-------|----------------------------------------|
| 산술   | `+` `-` `*` `/` `%` (문자열 `+` 는 연결)    |
| 비교   | `>` `>=` `<` `<=`                       |
| 동등   | `==` `!=` (모든 타입)                     |
| 논리   | `and` `or` (단축 평가), `!`                |

### 내장 함수

| 함수         | 설명                              |
|-------------|---------------------------------|
| `Array(n)`  | 크기 n 의 배열 생성                  |
| `clock()`   | 현재 시각(ms) — 벤치마크용            |

## 실행 전 최적화

| 최적화        | 설명                                                                 |
|--------------|--------------------------------------------------------------------|
| 정적 바인딩    | Checker 가 지역 변수까지의 거리(distance)를 미리 계산 → 실행 시 O(1) 접근       |
| 상수 폴딩      | 런타임 이전에 100% 확정되는 상수식을 리터럴로 교체 → 루프 내 반복 연산 제거         |

예) `for (...) { total = total + (1 - 2*3*4*5/6 + 7+8+9) % 1000 % 30; }`
에서 괄호식은 실행 전에 리터럴 `5` 로 접혀, 루프마다 연산하지 않습니다.

## 설계 / 디자인 패턴

- **Visitor 패턴** — `Expr.Visitor` / `Stmt.Visitor` 로 AST 순회 (Checker / Optimizer / Executor)
- **Interpreter 패턴** — AST 노드를 직접 평가하는 트리 순회 인터프리터
- **Strategy 패턴** — `FactoryShell` 이 실행 모드(REPL / 파일 / 디버그)를 선택
- **Observer 패턴** — `ExecutionListener` 로 Executor 실행을 디버거가 관찰(stepping)

## 특이사항

- 함수 키워드는 `Func` 와 `func` 를 모두 허용합니다.
- 숫자는 내부적으로 모두 실수(`double`)이며, 정수는 `.0` 없이 출력됩니다.
- `print null` 은 `nil`, 배열 내부의 빈 값은 `[null, ...]` 로 표시됩니다.
- 3일차부터 TDD 는 필수가 아니지만, 기존 유닛 테스트는 유지하고 추가 기능에 대한
  유닛 테스트(함수 / 배열 / 최적화 / 정적 바인딩)를 새로 작성했습니다.

### 샘플 스크립트

- [feature_demo.txt](scripts/feature_demo.txt) — 함수 · 배열 · 상수 폴딩 예시
- [debug_demo.txt](scripts/debug_demo.txt) — 디버그 모드 예시
- [sample](scripts/sample) — 기본 문법 예시
