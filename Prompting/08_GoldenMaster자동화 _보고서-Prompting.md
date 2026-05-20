# 08 Golden Master 자동화 보고서 Prompting

## 1. 사용한 프롬프트들

### 1.1 Golden Master 자동화 구현 프롬프트

```text
@TexttestFixture.java @GildedRose.java

[P] 회귀 테스트(Approval/Golden Master) 설계 전문가입니다.
[C] Java 21, JUnit 5, Maven
[T] TexttestFixture 출력 기반 Golden Master를 설계/구현해줘.
    1) 기준 출력(golden_master_expected.txt)을 생성/보관하는 방법
    2) 테스트에서 actual 출력과 파일 비교하는 방법
    3) CI에서 mvn test로 자동 실행되는 구성
    4) (선택) Approvals 테스트 적용 가능성
[F] 테스트 코드 + 파일 저장/비교 구현 + 실행 방법
```

### 1.2 보고서 및 프롬프트 내보내기 요청

```text
이번에 한 내용을 report 폴더에 08_GoldenMaster자동화 _보고서.md 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더에 08_GoldenMaster자동화 _보고서-Prompting.md 파일로 내보내줘
```

## 2. 프롬프트 의도

이 프롬프트는 기존 텍스트 출력 기반 프로그램에 Golden Master 회귀 테스트를 추가하도록 요청한다.

핵심 의도는 다음과 같다.

- `TexttestFixture.run(...)`의 출력 문자열을 회귀 테스트 기준으로 삼는다.
- 기준 입력 CSV와 기준 출력 txt 파일을 테스트 리소스로 보관한다.
- actual 출력과 expected 파일을 JUnit 5 테스트에서 비교한다.
- CI에서 `mvn test`만 실행해도 Golden Master 테스트가 자동 수행되도록 한다.
- ApprovalTests 도입 가능성을 함께 검토한다.
- 구현 내용과 실행 방법을 보고서로 남긴다.

## 3. 수행 내용

먼저 워크스페이스에서 `TexttestFixture.java`, `GildedRose.java`, `pom.xml`, 테스트 리소스 파일을 확인했다.

확인 결과 `GildedRose.java`는 현재 프로젝트에서 찾을 수 없었고, 실제 대상은 SHealth BMI 프로젝트의 `TexttestFixture.java`였다. 따라서 `TexttestFixture.run(...)` 출력 기반으로 Golden Master 테스트를 구성했다.

구현 및 정리한 내용은 다음과 같다.

- `src/test/resources/golden-master/shealth_golden_master_input.csv`를 고정 입력으로 사용
- `src/test/resources/golden-master/golden_master_expected.txt`를 기준 출력으로 사용
- `TexttestFixtureGoldenMasterTest`에서 actual 출력과 기준 파일 비교
- `-DupdateGoldenMaster=true` 옵션으로 기준 출력 갱신 가능
- 줄바꿈 차이를 줄이기 위해 CRLF/CR을 LF로 정규화
- `mvn test`로 전체 테스트와 Golden Master 테스트 실행 확인
- `docs/README.md`에 실행 방법과 ApprovalTests 적용 가능성 기록

## 4. 테스트 실행 결과

다음 명령으로 테스트를 실행했다.

```bash
mvn test
```

실행 결과는 다음과 같이 전체 Green 상태였다.

```text
Running com.bestreviewer.SHealthBMITest
Tests run: 40, Failures: 0, Errors: 0, Skipped: 0

Running com.bestreviewer.TexttestFixtureGoldenMasterTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 5. 생성 보고서

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

## 4. 기준 출력 생성 및 갱신 방법

기준 출력은 테스트 코드에서 `updateGoldenMaster` 시스템 프로퍼티를 통해 갱신할 수 있도록 했다.

```bash
mvn test -DupdateGoldenMaster=true
```

이 명령은 `TexttestFixture.run(...)`의 현재 actual 출력을 `src/test/resources/golden-master/golden_master_expected.txt`에 다시 쓴다.

## 5. actual 출력과 파일 비교 구현

`TexttestFixtureGoldenMasterTest`는 고정 CSV 입력을 읽어 actual 출력을 만들고, 클래스패스의 `golden_master_expected.txt`와 비교한다.

일반 실행에서는 기준 파일을 읽기만 하며, `-DupdateGoldenMaster=true` 실행에서만 소스 리소스의 기준 출력 파일을 갱신한다.

## 6. CI 자동 실행 구성

`pom.xml`은 Java 21, JUnit 5, Maven Surefire 기반으로 구성되어 있다. 따라서 CI에서는 다음 명령만 실행하면 Golden Master 테스트가 자동 포함된다.

```bash
mvn test
```

## 7. ApprovalTests 적용 가능성

현재 프로젝트에는 `approvaltests` 의존성이 포함되어 있어 approved/received 파일 비교 방식으로 확장할 수 있다.

다만 이번 구현에서는 외부 diff 도구 없이 CI에서 안정적으로 동작하는 JUnit 5 파일 비교 방식을 기본으로 선택했다.

## 8. 이번 세션 산출물

- `report/08_GoldenMaster자동화 _보고서.md`
- `Prompting/08_GoldenMaster자동화 _보고서-Prompting.md`
- `src/test/java/com/bestreviewer/TexttestFixtureGoldenMasterTest.java`
- `src/test/resources/golden-master/golden_master_expected.txt`
- `src/test/resources/golden-master/shealth_golden_master_input.csv`
