# 05 Refactoring 보고서

## 1. 리팩토링 개요

이번 작업에서는 BMI 계산 기능을 클린코드 원칙에 맞게 리팩토링했다. 기존에는 `SHealth` 클래스 안에 파일 읽기, BMI 계산, 누락값 보정, 카테고리 분류, 통계 계산 로직이 모두 포함되어 있었다.

리팩토링 후에는 계산 책임을 `BMICalculator`로 분리하고, BMI 데이터와 통계 결과를 별도 클래스로 표현하도록 구조를 개선했다.

## 2. 리팩토링 목표

- Magic Number를 상수와 enum으로 추출
- 한 함수가 하나의 책임만 갖도록 분리
- 파일 입출력과 계산 로직을 분리하여 테스트하기 쉬운 구조로 개선
- 특정 연령대 BMI 분포 비율 계산 기능 추가
- BMI 정상 범위 사용자 목록 조회 기능 추가
- 전체 대비 각 BMI 카테고리 비율 계산 기능 추가

## 3. 주요 변경 파일

### `BMICalculator.java`

BMI 계산과 통계 계산의 중심 클래스다.

- BMI 계산
- BMI 카테고리 분류
- 누락된 체중과 키의 연령대 평균 보정
- 특정 연령대 BMI 분포 비율 계산
- BMI 정상 범위 사용자 목록 조회
- 전체 대비 각 카테고리 비율 계산

### `BmiCategory.java`

BMI 카테고리를 enum으로 분리했다.

- `UNDERWEIGHT`
- `NORMAL`
- `OVERWEIGHT`
- `OBESITY`

기존 타입 코드인 `100`, `200`, `300`, `400`은 enum 내부 값으로 관리하도록 변경했다.

### `BmiRecord.java`

BMI 계산에 필요한 사용자 데이터를 표현하는 클래스다.

- 사용자 ID
- 나이
- 체중
- 키

### `BmiStatistics.java`

BMI 카테고리별 건수와 비율을 관리하는 통계 결과 클래스다.

### `SHealth.java`

기존 메인 흐름과 호환성을 유지하면서 책임을 줄였다.

- CSV 파일 로딩 담당
- 기존 `getBmiRatio(int ageClass, int type)` API 유지
- 실제 계산은 `BMICalculator`에 위임

## 4. 클린코드 개선 내용

### Magic Number 제거

BMI 기준값과 연령대 계산 기준을 상수로 추출했다.

- `CENTIMETERS_PER_METER`
- `AGE_GROUP_SIZE`
- `UNDERWEIGHT_MAX_BMI`
- `NORMAL_MAX_BMI`
- `OVERWEIGHT_MAX_BMI`

BMI 카테고리 코드는 `BmiCategory` enum으로 관리하여 숫자의 의미가 코드에 드러나도록 했다.

### SRP 적용

기존 `SHealth.calculateBmi()`는 여러 책임을 동시에 수행했다.

리팩토링 후에는 다음과 같이 책임을 분리했다.

- `SHealth`: 파일 로딩과 기존 API 호환
- `BMICalculator`: BMI 계산과 통계 계산
- `BmiRecord`: BMI 입력 데이터 표현
- `BmiStatistics`: 통계 결과 표현
- `BmiCategory`: BMI 카테고리 표현

### 테스트 용이성 개선

파일을 읽지 않아도 `List<BmiRecord>`를 직접 전달하여 계산 로직을 테스트할 수 있게 되었다.

이를 통해 BMI 계산, 카테고리 분류, 연령대별 통계, 정상 범위 사용자 조회, 전체 카테고리 비율 계산을 독립적으로 검증할 수 있다.

## 5. 추가 구현 기능

### 특정 연령대 BMI 분포 비율 계산

`calculateAgeGroupDistribution(records, ageGroup)` 메서드를 추가했다.

예를 들어 `ageGroup` 값이 `20`이면 20대 사용자만 대상으로 저체중, 정상, 과체중, 비만 비율을 계산한다.

### BMI 정상 범위 사용자 목록 조회

`findNormalRangeUsers(records)` 메서드를 추가했다.

BMI 정상 범위는 기존 기준에 따라 `18.5 < BMI < 23.0`으로 분류된다.

### 전체 대비 각 카테고리 비율 계산

`calculateOverallCategoryRatio(records)` 메서드를 추가했다.

전체 사용자 기준으로 저체중, 정상, 과체중, 비만 비율을 계산한다.

## 6. 테스트 결과

다음 명령으로 테스트를 수행했다.

```bash
mvn -o test
```

테스트 결과는 다음과 같다.

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

참고로 `mvn -s settings.xml test`는 사내 Maven 저장소 접속 타임아웃으로 실패했으나, 로컬 캐시를 사용하는 오프라인 모드에서는 정상 통과했다.

## 7. 결론

이번 리팩토링을 통해 BMI 계산 로직의 책임이 명확히 분리되었고, magic number가 줄어들었으며, 주요 계산 기능을 파일 입출력과 독립적으로 테스트할 수 있게 되었다.

또한 요구사항으로 추가된 연령대별 BMI 분포, 정상 범위 사용자 조회, 전체 카테고리 비율 계산 기능을 `BMICalculator` 중심으로 제공하도록 구현했다.
