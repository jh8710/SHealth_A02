# Unit Test Progress Summary

## Overview

`report`의 06, 07, 08번 보고서를 기준으로 BMI 프로젝트의 단위 테스트 작성, 테스트 실행 및 결함 분석, Golden Master 회귀 테스트 자동화 진행 상황을 정리했다.

현재 테스트 범위는 `SHealthBMITest` 중심의 BMI 계산 검증과 `TexttestFixtureGoldenMasterTest` 기반의 텍스트 출력 회귀 검증으로 구성되어 있다.

## Test Scope

| 구분 | 대상 | 진행 내용 | 상태 |
|---|---|---|---|
| 06 테스트 케이스 작성 | `SHealthBMITest.java`, `BmiRecord.java`, `BMICalculator.java`, `SHealth.java` | 20대, 30대, 40대 BMI 통계 계산과 누락 체중 보정, 비정상 입력 예외 처리 테스트 작성 | 완료 |
| 07 테스트 실행 결함 분석 | `SHealthBMITest.java`, `SHealth.java` 및 BMI 계산 관련 클래스 | `mvn test` 실행 결과와 결함 여부 확인, 수정 필요성 검토 | 완료 |
| 08 Golden Master 자동화 | `TexttestFixture.java`, `TexttestFixtureGoldenMasterTest.java`, golden-master 리소스 | 텍스트 출력 기준 파일 비교 방식의 회귀 테스트 자동화 | 완료 |

## 06 Test Case Writing

`SHealthBMITest.java`에는 20대, 30대, 40대별 BMI 통계 테스트가 추가되었다. 각 연령대는 최소 5개 데이터를 사용하며, 저체중, 정상, 과체중, 비만, `weight=0` 누락 체중 사례를 포함한다.

검증한 핵심 규칙은 다음과 같다.

- 20대, 30대, 40대 각각에 대해 저체중 20.0%, 정상 40.0%, 과체중 20.0%, 비만 20.0% 비율을 검증했다.
- `weight=0`은 누락 체중으로 보고 동일 연령대의 유효 체중 평균인 `63.25kg`으로 보정되는지 확인했다.
- 음수 나이, 음수 체중, 음수 키는 `IllegalArgumentException`으로 차단되도록 테스트했다.
- 임시 CSV 파일을 사용해 `SHealth.calculateBmi()`와 `SHealth.getBmiRatio()` 흐름을 함께 검증했다.

테스트 코드는 `@DisplayName`, Given-When-Then 주석, `@ParameterizedTest`, `@CsvSource`, `assertAll()`을 활용해 반복 검증과 의도 표현을 정리했다.

## 07 Test Execution And Defect Analysis

전체 테스트는 `mvn test`로 실행했으며, 현재 작업본에서는 실패가 재현되지 않았다.

```text
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`SHealthBMITest` 40건과 Golden Master 테스트 1건이 모두 통과했다. 따라서 보고서 작성 시점 기준으로 기대값과 실제값의 차이는 없으며, 즉시 수정해야 할 `SHealth.java` 결함도 확인되지 않았다.

결함 분석 결과는 다음과 같다.

- 현재 심각도는 `Info`로 분류했다.
- BMI 계산, 누락값 보정, 카테고리 분류, 연령대별 통계, CSV API 통합 흐름은 Green 상태다.
- `Item` 클래스 수정은 필요하지 않았다.
- 수정 diff는 `no changes required`로 정리했다.

향후 유사 결함이 발생할 경우 우선 점검할 위치는 CSV 로딩, `getBmiRatio()` 조회 로직, CSV 토큰을 `BmiRecord`로 변환하는 흐름이다.

## 08 Golden Master Automation

`TexttestFixture.java`의 텍스트 출력 결과를 기준으로 Golden Master 회귀 테스트를 구성했다. 이 테스트는 BMI 통계 출력 형식과 값이 의도치 않게 바뀌는 상황을 자동으로 감지한다.

사용한 리소스는 다음과 같다.

- 입력 데이터: `src/test/resources/golden-master/shealth_golden_master_input.csv`
- 기준 출력: `src/test/resources/golden-master/golden_master_expected.txt`
- 테스트 코드: `src/test/java/com/bestreviewer/TexttestFixtureGoldenMasterTest.java`

`TexttestFixtureGoldenMasterTest`는 고정 CSV 입력으로 actual 출력을 생성한 뒤, 기준 출력 파일과 문자열을 비교한다. Windows와 Unix 환경 차이를 줄이기 위해 줄바꿈은 `\n` 기준으로 정규화했다.

일반 회귀 테스트는 다음 명령으로 실행한다.

```bash
mvn test
```

의도된 출력 변경 후 기준 파일을 갱신할 때는 다음 명령을 사용한다.

```bash
mvn test -DupdateGoldenMaster=true
```

기준 파일 갱신은 실패를 덮어쓰기 위한 용도가 아니라, 출력 변경이 의도된 동작 변경임을 확인한 뒤 리뷰 가능한 변경으로 반영하기 위한 절차다.

## Maven And CI Status

프로젝트는 Java 21, JUnit 5, Maven Surefire 기반으로 테스트를 실행한다. `pom.xml`에는 `src/test/resources` 리소스 복사와 JUnit Platform 실행 구성이 포함되어 있어, CI에서는 별도 스크립트 없이 `mvn test`만으로 단위 테스트와 Golden Master 테스트가 함께 실행된다.

최종 확인된 테스트 결과는 다음과 같다.

| 테스트 클래스 | 테스트 수 | 결과 |
|---|---:|---|
| `com.bestreviewer.SHealthBMITest` | 40 | 통과 |
| `com.bestreviewer.TexttestFixtureGoldenMasterTest` | 1 | 통과 |
| 전체 | 41 | 통과 |

## Conclusion

06번 보고서에서는 BMI 단위 테스트와 통합 흐름 검증을 보강했고, 07번 보고서에서는 전체 테스트 Green 상태와 결함 없음 상태를 확인했다. 08번 보고서에서는 텍스트 출력 기반 Golden Master 테스트를 추가해 기존 계산 결과와 출력 형식의 회귀를 자동으로 감지할 수 있게 했다.

현재 기준으로 `mvn test`는 전체 41개 테스트 통과 상태이며, 단위 테스트와 Golden Master 회귀 테스트는 CI에서 함께 실행 가능한 상태다.
