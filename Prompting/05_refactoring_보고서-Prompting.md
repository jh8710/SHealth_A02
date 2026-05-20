# 05 Refactoring 보고서 Prompting

## 1. 사용한 프롬프트

```text
@BMICalculator.java 현재 코드를 클린코드 원칙으로 리팩토링해줘:
- Magic Number → 상수 추출
- 한 함수 하나의 책임 (SRP)
- 테스트하기 쉽도록 분리

추가 구현:
- 특정 연령대 BMI 분포 비율 계산
- BMI 정상 범위 사용자 목록 조회
- 전체 대비 각 카테고리 비율 계산
```

## 2. 프롬프트 의도

이 프롬프트는 기존 BMI 계산 코드를 단순히 동작하도록 유지하는 것이 아니라, 클린코드 관점에서 구조를 개선하도록 요청한다.

핵심 의도는 다음과 같다.

- BMI 기준값과 타입 코드 같은 magic number 제거
- 하나의 메서드에 몰려 있는 여러 책임 분리
- 파일 입출력과 계산 로직을 분리하여 테스트 가능성 향상
- 기존 기능을 유지하면서 새로운 통계 기능 추가

## 3. 리팩토링 결과 요약

이번 작업에서는 `BMICalculator.java`를 새로 추가하고, 기존 `SHealth` 클래스에 섞여 있던 BMI 계산 로직을 분리했다.

주요 변경 사항은 다음과 같다.

- `BMICalculator`: BMI 계산, 카테고리 분류, 통계 계산 담당
- `BmiCategory`: BMI 카테고리와 기존 타입 코드 관리
- `BmiRecord`: BMI 계산 대상 사용자 데이터 표현
- `BmiStatistics`: 카테고리별 통계 결과 표현
- `SHealth`: CSV 파일 로딩과 기존 API 호환 담당

## 4. 클린코드 개선 내용

### Magic Number 추출

BMI 기준값과 연령대 계산 기준을 상수로 분리했다.

- `CENTIMETERS_PER_METER`
- `AGE_GROUP_SIZE`
- `UNDERWEIGHT_MAX_BMI`
- `NORMAL_MAX_BMI`
- `OVERWEIGHT_MAX_BMI`

BMI 카테고리 코드는 `BmiCategory` enum으로 이동했다.

### SRP 적용

기존 `SHealth.calculateBmi()`는 다음 작업을 모두 수행했다.

- CSV 파일 읽기
- 누락값 보정
- BMI 계산
- BMI 카테고리 분류
- 연령대별 통계 계산
- 결과 저장

리팩토링 후에는 각 책임이 별도 클래스로 분리되었다.

- `SHealth`: 파일 로딩과 기존 API 제공
- `BMICalculator`: 계산 로직
- `BmiRecord`: 데이터 모델
- `BmiStatistics`: 통계 결과 모델
- `BmiCategory`: 카테고리 모델

### 테스트 용이성 개선

계산 로직이 파일 입출력과 분리되었기 때문에 테스트에서 `List<BmiRecord>`를 직접 전달할 수 있다.

따라서 실제 데이터 파일 없이도 다음 기능을 검증할 수 있다.

- BMI 계산
- 누락된 체중과 키의 연령대 평균 보정
- BMI 카테고리 분류
- 특정 연령대 BMI 분포 비율 계산
- BMI 정상 범위 사용자 목록 조회
- 전체 대비 각 카테고리 비율 계산

## 5. 추가 구현 기능

### 특정 연령대 BMI 분포 비율 계산

`calculateAgeGroupDistribution(records, ageGroup)` 메서드를 추가했다.

전달된 연령대에 해당하는 사용자만 필터링한 뒤, BMI 카테고리별 비율을 계산한다.

### BMI 정상 범위 사용자 목록 조회

`findNormalRangeUsers(records)` 메서드를 추가했다.

BMI가 정상 범위인 사용자만 목록으로 반환한다.

### 전체 대비 각 카테고리 비율 계산

`calculateOverallCategoryRatio(records)` 메서드를 추가했다.

전체 사용자 기준으로 저체중, 정상, 과체중, 비만 비율을 계산한다.

## 6. 테스트 결과

다음 명령으로 테스트를 수행했다.

```bash
mvn -o test
```

결과:

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`mvn -s settings.xml test`는 사내 Maven 저장소 접속 타임아웃으로 실패했지만, 오프라인 모드에서는 정상 통과했다.

## 7. 최종 정리

프롬프트에서 요구한 클린코드 원칙에 따라 BMI 계산 책임을 분리하고, magic number를 상수와 enum으로 대체했다.

또한 추가 요구사항인 특정 연령대 BMI 분포 비율 계산, BMI 정상 범위 사용자 목록 조회, 전체 대비 각 카테고리 비율 계산 기능을 구현하고 테스트로 검증했다.
