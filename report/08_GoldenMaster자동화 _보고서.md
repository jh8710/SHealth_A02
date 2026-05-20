# 08 Golden Master 자동화 보고서

## 1. 작업 개요

이번 작업에서는 `TexttestFixture.java`의 텍스트 출력 결과를 기준으로 Golden Master 회귀 테스트를 설계하고 구현했다.

요청된 구현 항목은 다음과 같다.

- 기준 출력인 `golden_master_expected.txt`를 생성하고 보관하는 방법 정리
- 테스트에서 actual 출력과 기준 파일을 비교하는 방법 구현
- CI에서 `mvn test`만으로 자동 실행되는 Maven/JUnit 5 구성 확인
- ApprovalTests 적용 가능성 검토
- 테스트 코드, 파일 저장/비교 구현, 실행 방법 문서화

## 2. 분석 및 구현 대상 파일

- `src/test/java/com/bestreviewer/TexttestFixture.java`
- `src/test/java/com/bestreviewer/TexttestFixtureGoldenMasterTest.java`
- `src/test/resources/golden-master/shealth_golden_master_input.csv`
- `src/test/resources/golden-master/golden_master_expected.txt`
- `pom.xml`
- `docs/README.md`

요청에는 `GildedRose.java`도 포함되어 있었지만, 현재 워크스페이스에서는 해당 파일을 찾을 수 없었다. 따라서 실제 존재하는 BMI 프로젝트의 `TexttestFixture.java` 출력 기반으로 Golden Master를 구성했다.

## 3. Golden Master 기준 출력 설계

Golden Master의 입력 데이터는 다음 파일에 고정했다.

```text
src/test/resources/golden-master/shealth_golden_master_input.csv
```

기준 출력은 다음 파일에 보관한다.

```text
src/test/resources/golden-master/golden_master_expected.txt
```

`TexttestFixture.run(...)`은 고정 CSV 입력을 읽고 20대부터 70대까지의 BMI 카테고리 비율을 텍스트로 출력한다. 이 출력 문자열을 기준 파일과 비교해 의도하지 않은 회귀를 감지한다.

현재 기준 출력 예시는 다음과 같다.

```text
20 - underweight = 20.000000, normal = 40.000000, overweight = 20.000000, obesity = 20.000000
30 - underweight = 20.000000, normal = 40.000000, overweight = 20.000000, obesity = 20.000000
40 - underweight = 20.000000, normal = 40.000000, overweight = 20.000000, obesity = 20.000000
50 - underweight = 0.000000, normal = 0.000000, overweight = 0.000000, obesity = 0.000000
60 - underweight = 0.000000, normal = 0.000000, overweight = 0.000000, obesity = 0.000000
70 - underweight = 0.000000, normal = 0.000000, overweight = 0.000000, obesity = 0.000000
```

## 4. 기준 출력 생성 및 갱신 방법

기준 출력은 테스트 코드에서 `updateGoldenMaster` 시스템 프로퍼티를 통해 갱신할 수 있도록 했다.

```bash
mvn test -DupdateGoldenMaster=true
```

이 명령은 `TexttestFixture.run(...)`의 현재 actual 출력을 `src/test/resources/golden-master/golden_master_expected.txt`에 다시 쓴다.

주의할 점은 Golden Master 갱신은 테스트 실패를 덮어쓰기 위한 용도가 아니라는 것이다. 출력 변경이 의도된 동작 변경인지 먼저 확인한 뒤, 리뷰 가능한 변경으로 기준 파일을 갱신해야 한다.

## 5. actual 출력과 파일 비교 구현

`TexttestFixtureGoldenMasterTest`는 다음 흐름으로 동작한다.

1. 클래스패스에서 `golden-master/shealth_golden_master_input.csv`를 찾는다.
2. 해당 CSV 경로를 `TexttestFixture.run(...)`에 전달해 actual 출력을 생성한다.
3. 일반 실행에서는 클래스패스의 `golden-master/golden_master_expected.txt`를 읽는다.
4. `-DupdateGoldenMaster=true` 실행에서는 소스 리소스의 기준 파일을 actual로 갱신한 뒤 비교한다.
5. Windows와 Unix 환경 차이를 줄이기 위해 줄바꿈을 `\n` 기준으로 정규화한다.
6. JUnit 5 `assertEquals(...)`로 expected와 actual을 비교한다.

비교 실패 시에는 Golden Master 출력이 변경되었음을 알리고, 의도된 변경일 때만 갱신 명령을 실행하라는 메시지를 제공한다.

## 6. CI 자동 실행 구성

`pom.xml`은 Java 21과 JUnit 5 기반 테스트 실행을 위해 다음 구성을 사용한다.

- `maven.compiler.release`를 `21`로 설정
- `junit-jupiter`를 test dependency로 사용
- `maven-surefire-plugin` `3.2.5`를 통해 JUnit Platform 테스트 실행
- `src/test/resources`를 test resource로 복사

따라서 CI에서는 별도 스크립트 없이 다음 명령만 실행하면 Golden Master 테스트가 함께 수행된다.

```bash
mvn test
```

## 7. 테스트 실행 결과

다음 명령으로 전체 테스트를 실행했다.

```bash
mvn test
```

실행 결과는 다음과 같다.

```text
Running com.bestreviewer.SHealthBMITest
Tests run: 40, Failures: 0, Errors: 0, Skipped: 0

Running com.bestreviewer.TexttestFixtureGoldenMasterTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Golden Master 테스트 1건을 포함해 전체 41개 테스트가 통과했다.

## 8. ApprovalTests 적용 가능성

현재 프로젝트에는 `approvaltests` 의존성이 포함되어 있어 ApprovalTests 방식으로도 확장할 수 있다.

적용 시에는 `TexttestFixture.run(...)`의 actual 문자열을 ApprovalTests의 approved/received 파일 비교 방식으로 검증할 수 있다. 이 방식은 로컬 개발자가 diff 도구로 변경 내용을 확인하기 좋다는 장점이 있다.

다만 이번 구현에서는 CI 안정성과 단순성을 우선해 JUnit 5의 파일 기반 비교를 기본 방식으로 선택했다. 외부 diff 도구나 ApprovalTests 러너 설정 없이도 `mvn test`에서 동일하게 동작하기 때문이다.

## 9. 실행 방법 요약

일반 회귀 테스트 실행:

```bash
mvn test
```

의도된 출력 변경 후 기준 파일 갱신:

```bash
mvn test -DupdateGoldenMaster=true
```

갱신 후 확인:

```bash
mvn test
```

## 10. 결론

`TexttestFixture.java` 출력 기반 Golden Master 회귀 테스트를 구성해, BMI 통계 출력 형식과 값이 의도치 않게 바뀌는 상황을 자동으로 감지할 수 있게 했다.

기준 입력과 기준 출력은 `src/test/resources/golden-master` 아래에 보관되며, CI에서는 `mvn test`만으로 기존 단위 테스트와 Golden Master 테스트가 함께 실행된다.
