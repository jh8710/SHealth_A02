# 09 기능 개선 보고서

## 1. 작업 개요

이번 작업에서는 SHealth BMI 프로젝트에 추가 기능을 구현하고, 기존 구조를 SRP 관점에서 리팩토링했다.

요청된 구현 항목은 다음과 같다.

- SRP에 따른 책임 분리 리팩토링
- 특정 연령대의 BMI 분포 비율 계산 기능 추가
- `height`가 `0`인 경우에 대한 평균치 보정 로직 추가
- BMI 정상 범위 사용자 목록 조회 기능 추가
- 전체 사용자 대비 각 BMI 범주 비율 계산 기능 추가
- 구현 후 `mvn test` 실행 및 Green 확인

## 2. 분석 및 구현 대상 파일

- `src/main/java/com/bestreviewer/SHealth.java`
- `src/main/java/com/bestreviewer/BMICalculator.java`
- `src/main/java/com/bestreviewer/BmiDataReader.java`
- `src/main/java/com/bestreviewer/BmiRecord.java`
- `src/main/java/com/bestreviewer/BmiCategory.java`
- `src/main/java/com/bestreviewer/BmiStatistics.java`
- `src/test/java/com/bestreviewer/SHealthBMITest.java`
- `docs/README.md`

## 3. SRP 기반 책임 분리

기존 `SHealth`는 CSV 파일 읽기, BMI 계산, 누락값 보정, 통계 계산, 외부 API 제공 책임이 함께 섞여 있었다.

리팩토링 후 책임은 다음과 같이 분리했다.

- `SHealth`: 외부에서 사용하는 파사드와 기존 호환 API 제공
- `BmiDataReader`: CSV 파일 읽기 및 `BmiRecord` 생성
- `BMICalculator`: BMI 계산, BMI 범주 분류, 누락값 보정, 통계 계산
- `BmiRecord`: 사용자별 BMI 입력 데이터 표현
- `BmiCategory`: 저체중, 정상, 과체중, 비만 범주와 기존 코드값 관리
- `BmiStatistics`: 범주별 건수와 비율 계산 결과 표현

이를 통해 파일 입출력과 순수 계산 로직이 분리되어 테스트 작성과 기능 확장이 쉬워졌다.

## 4. 추가 기능 구현 내용

### 4.1 특정 연령대 BMI 분포 비율

`SHealth.getBmiDistributionRatio(int ageClass)`를 추가했다.

이 메서드는 특정 연령대, 예를 들어 `20`을 전달하면 20대 사용자만 대상으로 저체중, 정상, 과체중, 비만 비율을 `Map<BmiCategory, Double>` 형태로 반환한다.

기존 호환 API인 `getBmiRatio(int ageClass, int type)`도 유지했다. 기존 타입 코드는 다음과 같다.

- `100`: 저체중
- `200`: 정상
- `300`: 과체중
- `400`: 비만

### 4.2 Height 0 평균 보정

기존에는 체중이 `0`인 경우 같은 연령대 평균 체중으로 보정하는 흐름이 있었다.

이번 작업에서는 키가 `0`인 경우도 같은 방식으로 보정하도록 정리했다. 같은 연령대의 `0`이 아닌 키 값만 평균 계산에 사용하고, 해당 평균값을 누락된 키에 적용한 뒤 BMI를 계산한다.

### 4.3 BMI 정상 범위 사용자 목록 조회

`SHealth.getNormalBmiUserIds()`를 추가했다.

이 메서드는 BMI 정상 범위인 `18.5 < BMI < 23.0`에 해당하는 사용자 ID 목록을 반환한다.

내부적으로는 `BMICalculator.findNormalRangeUsers(...)`와 `findNormalRangeUserIds(...)`를 통해 정상 범위 사용자를 필터링한다.

### 4.4 전체 사용자 대비 BMI 범주 비율

`SHealth.getOverallBmiCategoryRatios()`를 추가했다.

이 메서드는 전체 사용자 수를 기준으로 저체중, 정상, 과체중, 비만 비율을 계산해 `Map<BmiCategory, Double>` 형태로 반환한다.

### 4.5 BMI 경계값 보정

BMI 계산은 소수 연산을 사용하므로 `25.0` 같은 경계값에서 부동소수점 오차가 발생할 수 있다.

예를 들어 수학적으로는 `25.0`이어야 하는 값이 내부 표현상 `24.999999999`처럼 계산되면 비만이 아닌 과체중으로 분류될 수 있다.

이를 방지하기 위해 BMI 분류 경계에 작은 허용 오차를 적용했다.

## 5. 테스트 보강

`SHealthBMITest`에 다음 기능 검증을 추가 및 유지했다.

- BMI 계산 검증
- BMI 범주 경계값 분류 검증
- 체중과 키 누락값의 연령대 평균 보정 검증
- 특정 연령대 BMI 분포 비율 검증
- BMI 정상 범위 사용자 조회 검증
- 전체 사용자 대비 BMI 범주 비율 검증
- `SHealth` 공개 API 기반 통합 검증

Golden Master 테스트도 함께 유지하여 기존 텍스트 출력 결과가 의도치 않게 변경되지 않는지 확인했다.

## 6. 테스트 실행 결과

다음 명령으로 전체 테스트를 실행했다.

```bash
mvn test
```

실행 결과는 다음과 같다.

```text
Running com.bestreviewer.SHealthBMITest
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0

Running com.bestreviewer.TexttestFixtureGoldenMasterTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

전체 42개 테스트가 통과했으며, 기능 개선 후에도 Golden Master 회귀 테스트가 Green 상태임을 확인했다.

## 7. 결론

이번 기능 개선으로 `SHealth`의 책임이 줄어들고, BMI 계산과 통계 로직이 테스트 가능한 구조로 분리되었다.

또한 특정 연령대 분포 조회, 키 누락 보정, 정상 BMI 사용자 ID 조회, 전체 BMI 범주 비율 계산 기능을 추가해 요구사항을 충족했다.

최종 검증은 `mvn test`로 수행했으며 전체 테스트가 성공했다.
