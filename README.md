# CodeFab

## 실행 방법

| 목적             | 명령                                      |
|----------------|-----------------------------------------|
| 모든 유닛 테스트      | `./gradlew test`                        |
| Prompt Shell   | `./gradlew run`                         |
| 스크립트 파일 실행     | `./gradlew run --args='scripts/sample'` |
| 빌드 + 테스트 + jar | `./gradlew build`                       |

Prompt Shell 예시:

```
>>> var a = 5;
>>> var b = 10;
>>> print a + b;
15
>>> exit
```

## 지원 문법

### Expression

| 종류       | 예시                        |
|----------|---------------------------|
| Literal  | `3`, `"hi"`, `true`       |
| Variable | `a`                       |
| Assign   | `a = 3`                   |
| Binary   | `1 + 2`, `a > 0`, `2 * 3` |
| Unary    | `-x`, `!isExist`          |
| Logical  | `a and b`, `a or b`       |
| Grouping | `(1 + 2)`                 |

### Statement

| 종류         | 예시                                        |
|------------|-------------------------------------------|
| Expression | `a + 1;`                                  |
| Print      | `print a;`                                |
| VarDeclare | `var a = 3;`                              |
| Block      | `{ ... }`                                 |
| If         | `if (a > 0) {...} else {...}`             |
| For        | `for (var i = 0; i < 3; i = i + 1) {...}` |

### 샘플 스크립트
[sample](scripts/sample)
