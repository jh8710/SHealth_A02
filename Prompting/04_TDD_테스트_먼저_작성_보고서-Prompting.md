# 04 TDD 테스트 먼저 작성 보고서 Prompting

## Prompt

```text
BMI 계산기에 대해 TDD를 시작할게.
실패하는 JUnit 5 테스트를 먼저 작성해줘:
1. BMI 계산 로직 (정상 입력)
2. 평균 보정 (weight=0, height=0)
3. BMI 카테고리 분류
4. 연령대별 통계 계산
각 테스트는 should_[결과]_when_[조건] 네이밍
```

## Generated Report

# TDD 테스트 먼저 작성 보고서

## 작업 개요

BMI 계산기 프로그램에 대해 TDD 사이클의 첫 단계인 실패하는 JUnit 5 테스트를 먼저 작성했습니다. 기존 `SHealthBMITest`에 있던 단순 실패 테스트를 제거하고, BMI 도메인의 핵심 요구사항을 검증하는 4개의 테스트로 교체했습니다.

## 테스트 작성 대상

| 구분 | 테스트 메서드 | 검증 내용 |
|---|---|---|
| BMI 계산 로직 | `should_calculate_bmi_when_weight_and_height_are_valid` | 정상 체중과 신장을 입력했을 때 BMI가 올바르게 계산되는지 검증 |
| 평균 보정 | `should_apply_average_correction_when_weight_and_height_are_zero` | `weight=0`, `height=0`인 데이터가 같은 연령대의 평균 체중/신장으로 보정되는지 검증 |
| BMI 카테고리 분류 | `should_classify_bmi_category_when_bmi_boundary_values_are_given` | 저체중, 정상, 과체중, 비만 경계값이 올바른 카테고리로 분류되는지 검증 |
| 연령대별 통계 계산 | `should_calculate_age_group_statistics_when_records_have_different_age_groups` | 20대, 30대 데이터의 BMI 카테고리별 비율이 올바르게 계산되는지 검증 |

## 테스트 코드 위치

- `src/test/java/com/bestreviewer/SHealthBMITest.java`

## 설계한 테스트 API

테스트는 다음 도메인 타입과 메서드가 존재한다고 가정하고 작성했습니다.

| 타입 | 역할 |
|---|---|
| `BMICalculator` | BMI 계산, 평균 보정, 카테고리 분류, 통계 계산을 담당 |
| `BmiRecord` | 연령, 체중, 신장 데이터를 표현 |
| `BmiCategory` | `UNDERWEIGHT`, `NORMAL`, `OVERWEIGHT`, `OBESITY` BMI 분류를 표현 |
| `BmiStatistics` | 연령대별 BMI 카테고리 비율을 표현 |

## 테스트 실행 결과

`mvn test`를 실행하여 실패 상태를 확인했습니다. 현재는 구현 코드가 아직 없기 때문에 테스트 컴파일 단계에서 실패합니다.

주요 실패 원인:

- `BMICalculator` 클래스를 찾을 수 없음
- `BmiRecord` 클래스를 찾을 수 없음
- `BmiCategory` enum을 찾을 수 없음
- `BmiStatistics` 클래스를 찾을 수 없음

이는 TDD의 Red 단계에 해당합니다. 다음 단계에서는 위 타입과 메서드를 최소 구현하여 테스트를 통과시키는 Green 단계를 진행하면 됩니다.

## 다음 구현 방향

1. `BMICalculator` 클래스를 생성한다.
2. `calculateBmi(double weight, double height)`를 구현한다.
3. `BmiRecord`, `BmiCategory`, `BmiStatistics` 도메인 타입을 추가한다.
4. 평균 보정과 연령대별 통계 계산을 테스트가 요구하는 수준에서 최소 구현한다.
5. 테스트 통과 후 기존 `SHealth`의 긴 메서드와 매직 넘버를 새 도메인 로직으로 리팩토링한다.
