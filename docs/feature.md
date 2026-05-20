# BMI Feature Summary

## Overview

SHealth BMI 프로젝트에 기능 개선과 SRP 기반 리팩토링을 반영했다.

핵심 변경 사항은 다음과 같다.

- `SHealth`의 파일 읽기 책임을 `BmiDataReader`로 분리
- BMI 계산과 통계 계산은 `BMICalculator`에서 담당
- BMI 카테고리 비율 결과는 `BmiStatistics`에서 관리
- 특정 연령대 BMI 분포 조회 API 추가
- 키가 `0`인 경우 같은 연령대 평균 키로 보정
- 정상 BMI 범위 사용자 ID 조회 API 추가
- 전체 사용자 대비 BMI 범주 비율 조회 API 추가

## Changed Files

- `src/main/java/com/bestreviewer/SHealth.java`
  - 외부 호출용 파사드 역할을 담당한다.
  - 기존 `getBmiRatio(ageClass, type)` API를 유지한다.
  - 신규 조회 API를 제공한다.

- `src/main/java/com/bestreviewer/BmiDataReader.java`
  - CSV 파일을 읽고 `BmiRecord` 목록으로 변환한다.

- `src/main/java/com/bestreviewer/BMICalculator.java`
  - BMI 계산, BMI 범주 분류, 누락값 보정, 통계 계산을 담당한다.

- `src/main/java/com/bestreviewer/BmiStatistics.java`
  - BMI 범주별 건수와 비율을 캡슐화한다.
  - `toRatioMap()`으로 범주별 비율 맵을 반환한다.

- `src/test/java/com/bestreviewer/SHealthBMITest.java`
  - 기능 개선 항목에 대한 테스트를 추가했다.

## Public APIs

### `getBmiDistributionRatio(int ageClass)`

특정 연령대의 BMI 범주별 비율을 반환한다.

```java
Map<BmiCategory, Double> ratios = sHealth.getBmiDistributionRatio(20);
```

### `getNormalBmiUserIds()`

BMI 정상 범위 사용자 ID 목록을 반환한다.

정상 범위는 다음 기준을 따른다.

```text
18.5 < BMI < 23.0
```

### `getOverallBmiCategoryRatios()`

전체 사용자 대비 BMI 범주별 비율을 반환한다.

```java
Map<BmiCategory, Double> ratios = sHealth.getOverallBmiCategoryRatios();
```

### `getBmiRatio(int ageClass, int type)`

기존 호환 API도 유지한다.

```text
100 = UNDERWEIGHT
200 = NORMAL
300 = OVERWEIGHT
400 = OBESITY
```

## Missing Value Correction

체중 또는 키가 `0`이면 누락값으로 간주한다.

보정 방식은 다음과 같다.

1. 같은 연령대의 사용자만 대상으로 한다.
2. `0`이 아닌 값만 평균 계산에 사용한다.
3. 누락된 체중은 평균 체중으로 보정한다.
4. 누락된 키는 평균 키로 보정한다.
5. 보정 후 BMI를 계산하고 범주를 분류한다.

## BMI Boundary Handling

BMI 범주는 다음 기준을 따른다.

- `BMI <= 18.5`: 저체중
- `18.5 < BMI < 23.0`: 정상
- `23.0 <= BMI < 25.0`: 과체중
- `25.0 <= BMI`: 비만

소수 연산으로 인해 경계값 근처에서 오차가 발생할 수 있어 작은 허용 오차를 적용했다.

## Verification

다음 명령으로 검증했다.

```bash
mvn test
```

결과:

```text
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
