# 09 기능 개선 및 결함 관리 보고서 Prompting

## 1. 사용한 프롬프트들

### 1.1 기능 개선 구현 프롬프트

```text
@SHealth.java @SHealthBMITest.java @README.md

아래 추가 기능을 구현해줘.
- SRP에 따른 책임 분리등 리팩토링 
- 특정 연령대의 BMI 분포 비율 계산 기능 추가
- Height가 0인 경우에 대한 평균치 보정 로직 추가 
- BMI 정상 범위 사용자 목록 조회 기능 추가
- 전체 사용자 대비 각 BMI 범주 비율 계산 기능 추가
- 구현 후 mvn test 실행해서 Green 확인
  (실패하면 자동 수정)
```

### 1.2 실제 소스 반영 요청 프롬프트

```text
구현한 내용 소스에 반영해줘
```

### 1.3 보고서 내보내기 요청 프롬프트

```text
이번에 한 내용을 report 폴더에 09_feature_보고서.md, docs/feature.md 파일로 r각각 내보내주고, 
프롬프트를 포함해서 Prompting 폴더에 09_결함관리_보고서-Prompting.md 파일로 내보내줘
```

## 2. 프롬프트 의도

이 프롬프트는 SHealth BMI 프로젝트에 기능 개선을 적용하고, 구현 과정에서 발생하는 테스트 실패나 결함을 자동으로 수정해 최종적으로 Green 상태를 만드는 것을 목표로 한다.

핵심 의도는 다음과 같다.

- 기존 코드의 책임을 SRP 관점에서 분리한다.
- BMI 통계 기능을 연령대별, 전체 사용자 기준으로 확장한다.
- 체중뿐 아니라 키 누락값도 평균 보정 대상에 포함한다.
- 정상 BMI 사용자 목록을 조회할 수 있게 한다.
- 테스트를 먼저 보강하고 `mvn test`로 검증한다.
- 구현 내용과 검증 결과를 문서로 남긴다.

## 3. 수행 내용

먼저 현재 프로젝트의 소스 구조를 확인했다.

초기 확인 과정에서 상위 경로의 Maven 프로젝트와 실제 워크스페이스인 `SHealth_A02` 프로젝트가 서로 다른 소스 트리를 가지고 있음을 확인했다.

따라서 최종적으로 실제 Git 작업 폴더인 `SHealth_A02`의 다음 파일들에 기능을 반영했다.

- `src/main/java/com/bestreviewer/SHealth.java`
- `src/main/java/com/bestreviewer/BmiDataReader.java`
- `src/main/java/com/bestreviewer/BMICalculator.java`
- `src/main/java/com/bestreviewer/BmiStatistics.java`
- `src/test/java/com/bestreviewer/SHealthBMITest.java`
- `docs/README.md`

## 4. 구현 상세

### 4.1 SRP 기반 책임 분리

`SHealth`가 담당하던 CSV 파일 읽기 책임을 `BmiDataReader`로 분리했다.

`SHealth`는 다음 역할만 수행하도록 정리했다.

- CSV 파일 경로를 받아 계산 흐름 시작
- 기존 `getBmiRatio(ageClass, type)` 호환 API 제공
- 신규 기능 조회 API 제공

계산 로직은 `BMICalculator`, 통계 결과 표현은 `BmiStatistics`, 데이터 표현은 `BmiRecord`와 `BmiCategory`가 담당하도록 구성했다.

### 4.2 연령대별 BMI 분포 비율

`SHealth.getBmiDistributionRatio(int ageClass)`를 추가했다.

이 API는 특정 연령대의 BMI 범주별 비율을 `Map<BmiCategory, Double>`로 반환한다.

### 4.3 Height 0 평균 보정

키가 `0`인 경우 같은 연령대의 평균 키로 보정하도록 했다.

평균 계산 시에는 `0`인 값은 제외한다. 따라서 누락값이 평균을 왜곡하지 않는다.

### 4.4 BMI 정상 범위 사용자 목록 조회

`SHealth.getNormalBmiUserIds()`를 추가했다.

BMI 정상 범위는 기존 요구사항에 따라 `18.5 < BMI < 23.0`으로 판단한다.

### 4.5 전체 사용자 대비 BMI 범주 비율

`SHealth.getOverallBmiCategoryRatios()`를 추가했다.

전체 사용자 수를 기준으로 저체중, 정상, 과체중, 비만 비율을 반환한다.

### 4.6 BMI 경계값 결함 보정

테스트 실행 중 BMI 경계값에서 부동소수점 오차로 인해 `25.0` 근처 값이 과체중으로 분류될 수 있는 문제가 확인되었다.

이를 해결하기 위해 BMI 분류 경계에 작은 허용 오차를 적용했다.

## 5. 테스트 실행 및 결함 관리

구현 후 `mvn test`를 실행했고, 테스트 결과를 확인하면서 실패 원인을 수정했다.

주요 확인 사항은 다음과 같다.

- 테스트 기대값이 실제 평균 보정 정책과 맞는지 확인
- BMI `25.0` 경계값이 비만으로 안정적으로 분류되는지 확인
- 신규 API가 실제 `SHealth` 공개 인터페이스로 동작하는지 확인
- Golden Master 회귀 테스트가 기존 출력 변경을 감지하는지 확인

최종 테스트 결과는 다음과 같다.

```text
Running com.bestreviewer.SHealthBMITest
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0

Running com.bestreviewer.TexttestFixtureGoldenMasterTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 6. 생성 산출물

이번 요청으로 생성한 문서는 다음과 같다.

- `report/09_feature_보고서.md`
- `docs/feature.md`
- `Prompting/09_결함관리_보고서-Prompting.md`

## 7. 결론

기능 개선 프롬프트를 기반으로 SRP 리팩토링과 BMI 통계 확장 기능을 구현했다.

구현 후 테스트 실패 가능성을 확인하고, 경계값 분류 문제를 보정해 최종적으로 `mvn test` Green 상태를 달성했다.
