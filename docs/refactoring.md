# BMI Refactoring Summary

## Overview

`BMICalculator.java`를 새로 추가하고, 기존 `SHealth`에 섞여 있던 BMI 계산, 카테고리 분류, 통계 계산 책임을 분리했다.

리팩토링 목표는 다음과 같다.

- Magic Number를 상수와 enum으로 추출
- 한 함수가 하나의 책임만 갖도록 분리
- 파일 입출력과 순수 계산 로직을 나눠 테스트하기 쉬운 구조로 개선
- 추가 요구사항인 연령대별 BMI 분포, 정상 범위 사용자 조회, 전체 카테고리 비율 계산 제공

## Changed Files

- `src/main/java/com/bestreviewer/BMICalculator.java`
  - BMI 계산, BMI 카테고리 분류, 연령대별 통계 계산을 담당한다.
  - `calculateAgeGroupDistribution()`으로 특정 연령대 BMI 분포 비율을 계산한다.
  - `findNormalRangeUsers()`로 BMI 정상 범위 사용자를 조회한다.
  - `calculateOverallCategoryRatio()`로 전체 대비 각 BMI 카테고리 비율을 계산한다.

- `src/main/java/com/bestreviewer/BmiCategory.java`
  - BMI 카테고리와 기존 타입 코드 값을 enum으로 관리한다.
  - `100`, `200`, `300`, `400` 같은 magic number를 의미 있는 이름으로 대체한다.

- `src/main/java/com/bestreviewer/BmiRecord.java`
  - 사용자 BMI 계산에 필요한 `id`, `age`, `weight`, `height` 데이터를 표현한다.

- `src/main/java/com/bestreviewer/BmiStatistics.java`
  - 카테고리별 건수와 비율 계산 결과를 캡슐화한다.

- `src/main/java/com/bestreviewer/SHealth.java`
  - CSV 파일 로딩과 기존 호환 API 제공 역할만 담당하도록 축소했다.
  - 기존 `getBmiRatio(int ageClass, int type)` 호출은 유지하면서 내부 계산은 `BMICalculator`에 위임한다.

- `src/test/java/com/bestreviewer/SHealthBMITest.java`
  - 기존 테스트에 추가 요구사항 검증 테스트를 보강했다.

## Clean Code Improvements

### Magic Number Extraction

BMI 기준값과 연령대 계산 기준을 `BMICalculator`의 상수로 분리했다.

- `CENTIMETERS_PER_METER`
- `AGE_GROUP_SIZE`
- `UNDERWEIGHT_MAX_BMI`
- `NORMAL_MAX_BMI`
- `OVERWEIGHT_MAX_BMI`

BMI 카테고리 코드는 `BmiCategory` enum으로 분리했다.

### Single Responsibility

기존 `SHealth.calculateBmi()`는 파일 읽기, 누락값 보정, BMI 계산, 카테고리별 집계, 결과 저장을 모두 담당했다.

리팩토링 후 책임은 다음처럼 분리됐다.

- `SHealth`: 파일 로딩과 기존 API 호환
- `BMICalculator`: BMI 계산과 통계 계산
- `BmiRecord`: 입력 데이터 표현
- `BmiStatistics`: 통계 결과 표현
- `BmiCategory`: BMI 카테고리와 타입 코드 표현

### Testability

파일을 읽지 않아도 `List<BmiRecord>`만 전달하면 BMI 계산과 통계 계산을 테스트할 수 있다.

이를 통해 다음 로직을 독립적으로 검증할 수 있다.

- BMI 계산
- 누락된 체중과 키의 연령대 평균 보정
- BMI 카테고리 분류
- 연령대별 BMI 분포 비율
- BMI 정상 범위 사용자 목록
- 전체 대비 각 카테고리 비율

## Added Features

### Specific Age Group BMI Distribution

`BMICalculator.calculateAgeGroupDistribution(records, ageGroup)`을 통해 특정 연령대의 BMI 카테고리 비율을 계산한다.

예: `ageGroup`이 `20`이면 20대 사용자만 대상으로 저체중, 정상, 과체중, 비만 비율을 계산한다.

### Normal BMI User List

`BMICalculator.findNormalRangeUsers(records)`를 통해 BMI 정상 범위 사용자 목록을 조회한다.

정상 범위는 기존 BMI 기준에 따라 `18.5 < BMI < 23.0`으로 분류된다.

### Overall Category Ratio

`BMICalculator.calculateOverallCategoryRatio(records)`를 통해 전체 사용자 대비 각 BMI 카테고리 비율을 계산한다.

## Verification

다음 명령으로 테스트를 검증했다.

```bash
mvn -o test
```

결과:

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

참고로 `mvn -s settings.xml test`는 사내 Maven 저장소 접속 타임아웃으로 실패했지만, 로컬 캐시를 사용하는 오프라인 모드에서는 정상 통과했다.
