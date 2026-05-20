# Defect List

## Summary

현재까지 실행한 `mvn test` 기준으로 재현된 테스트 실패나 확정 결함은 없다.

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Defects

| ID | Severity | ItemType | Steps | Expected | Actual | Root Cause | Fix Summary |
|---|---|---|---|---|---|---|---|
| N/A | Info | Test Result | 1. `mvn test` 실행<br>2. `SHealthBMITest` 전체 결과 확인 | 모든 테스트가 통과해야 한다. | `37`개 테스트 모두 통과했고 실패/에러는 발생하지 않았다. | 재현된 결함 없음 | 코드 수정 불필요 |

## Notes

- 현재 작업본에서는 `SHealth.java`와 `SHealthBMITest.java` 기준 결함이 재현되지 않았다.
- 결함이 새로 발견되면 위 표에 `[ID] [Severity] [ItemType] [Steps] [Expected] [Actual] [Root Cause] [Fix Summary]` 형식으로 추가한다.
