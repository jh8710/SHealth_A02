# 01 요구사항분석 보고서 Prompting

## Prompt

```text
@SHealthRequirements.txt @README.md

[P] 시니어 Java QA 엔지니어 관점에서
[C] SHEALTH_A02 Java 프로젝트 (Java 21, Maven, JUnit 5)
[T] 요구사항을 분석해서 아래를 정리해줘.
    1) 나이대(20·30·40대)별저체중/정상/과체중/비만통계계산
    2) 누락체중(weight=0): 동일연령대평균으로보정
    3) 체중(kg) / 키(m)²
    4) BMI 기준: ≤18.5 저체중| 18.5<x<23 정상| 23≤x<25 과체중| ≥25 비만
    5) 입력: shealth.dat (id,age,weight,height CSV 형식)
    6) 테스트해야 할 주요 시나리오 목록 (번호)
[F] Markdown (표 + 번호 목록). requirements_analysis.md로 저장
```

## Generated Report

# SHEALTH_A02 Requirements Analysis

## 1. Functional Requirements Summary

| No. | Requirement | Expected Behavior | QA Notes |
|---:|---|---|---|
| 1 | Age-group BMI statistics | Calculate BMI category statistics for each age group. The requested target groups are the 20s, 30s, and 40s. | Age group should be determined by decade: 20s = 20-29, 30s = 30-39, 40s = 40-49. Boundary ages such as 20, 29, 30, 39, 40, and 49 must be tested. |
| 2 | BMI categories per age group | For each age group, calculate the distribution of underweight, normal, overweight, and obese users. | Statistics should be based on users after missing weight correction is applied. Define whether the output is count, ratio, or percentage before implementation or test assertion. |
| 3 | Missing weight correction | If `weight=0`, treat the value as missing and replace it with the average weight of the same age group. | The average must be calculated from non-zero weights in the same age group. A `0` weight must never be included in the average. |
| 4 | BMI formula | BMI = `weight(kg) / height(m)^2`. | Input height is CSV height value from `shealth.dat`; README describes height as centimeters, so it must be converted to meters before calculation. |
| 5 | BMI classification | `BMI <= 18.5` = underweight, `18.5 < BMI < 23` = normal, `23 <= BMI < 25` = overweight, `BMI >= 25` = obese. | Boundary values `18.5`, just over `18.5`, `23.0`, just under `25.0`, and `25.0` are mandatory test cases. |
| 6 | Input file | Read `shealth.dat` in CSV format: `id,age,weight,height`. | Header row must be skipped. Each data row should be parsed as ID, age, weight in kg, height in cm. |

## 2. Age-Group Statistics Requirement

| Age Group | Age Range | Required BMI Buckets | Calculation Target |
|---|---:|---|---|
| 20s | 20-29 | Underweight, normal, overweight, obese | Users whose corrected BMI belongs to each category |
| 30s | 30-39 | Underweight, normal, overweight, obese | Users whose corrected BMI belongs to each category |
| 40s | 40-49 | Underweight, normal, overweight, obese | Users whose corrected BMI belongs to each category |

## 3. Missing Weight Correction Rule

| Case | Input | Expected Correction |
|---|---|---|
| Non-missing weight | `weight > 0` | Use the original weight. |
| Missing weight | `weight = 0` | Replace with the average non-zero weight from the same age group. |
| Multiple missing weights in same age group | More than one row has `weight = 0` | Apply the same same-age-group non-zero average to each missing row. |
| No valid same-age-group weight | All rows in the age group have `weight = 0` | Requirement is ambiguous. QA should request a defined behavior, such as validation error, skipped record, or default policy. |

## 4. BMI Classification Rules

| BMI Range | Category | Boundary Expectation |
|---:|---|---|
| `BMI <= 18.5` | Underweight | `18.5` is underweight. |
| `18.5 < BMI < 23` | Normal | Values just above `18.5` and just below `23` are normal. |
| `23 <= BMI < 25` | Overweight | `23.0` is overweight. |
| `BMI >= 25` | Obese | `25.0` is obese. |

## 5. Major Test Scenarios

1. Given a user with valid weight and height, when BMI is calculated, then the result uses `weight(kg) / height(m)^2`.
2. Given height is provided in centimeters, when BMI is calculated, then height is converted to meters before squaring.
3. Given BMI is exactly `18.5`, when the user is classified, then the category is underweight.
4. Given BMI is greater than `18.5` and less than `23`, when the user is classified, then the category is normal.
5. Given BMI is exactly `23.0`, when the user is classified, then the category is overweight.
6. Given BMI is greater than or equal to `25.0`, when the user is classified, then the category is obese.
7. Given an age `20`, when age group is calculated, then the user belongs to the 20s group.
8. Given an age `29`, when age group is calculated, then the user belongs to the 20s group.
9. Given an age `30`, when age group is calculated, then the user belongs to the 30s group.
10. Given an age `39`, when age group is calculated, then the user belongs to the 30s group.
11. Given an age `40`, when age group is calculated, then the user belongs to the 40s group.
12. Given an age `49`, when age group is calculated, then the user belongs to the 40s group.
13. Given a 20s user has `weight=0`, when missing weight correction runs, then the weight is replaced by the average non-zero weight of 20s users.
14. Given a 30s user has `weight=0`, when missing weight correction runs, then the weight is replaced by the average non-zero weight of 30s users.
15. Given a 40s user has `weight=0`, when missing weight correction runs, then the weight is replaced by the average non-zero weight of 40s users.
16. Given multiple users in the same age group have `weight=0`, when correction runs, then all missing weights are replaced using the same non-zero age-group average.
17. Given an age group contains both valid and missing weights, when average weight is calculated, then `weight=0` rows are excluded from the average.
18. Given all users in an age group have `weight=0`, when correction runs, then the system follows a clearly defined error or fallback policy.
19. Given a CSV file has the header `id,age,weight,height`, when the file is read, then the header is skipped and only data rows are processed.
20. Given valid `shealth.dat` rows across 20s, 30s, and 40s, when statistics are calculated, then each age group reports underweight, normal, overweight, and obese distributions.
21. Given corrected BMI values across all four categories in one age group, when statistics are calculated, then the sum of category counts equals the number of users in that age group.
22. Given statistics are expressed as percentages, when one age group has all four categories, then the category percentages sum to approximately `100%` allowing for floating-point rounding.
23. Given malformed CSV data, such as missing fields or non-numeric weight, when the file is parsed, then the expected validation behavior should be defined and tested.
24. Given a user has height `0`, when BMI calculation is attempted, then the expected behavior should be defined because the current stated requirement only mandates `weight=0` correction.

## 6. QA Risks and Clarifications

| Topic | Risk or Ambiguity | Recommendation |
|---|---|---|
| Statistics unit | Requirement says "statistics" but does not explicitly say count, ratio, or percentage. | Define expected output type before final assertions. Existing code appears to use percentage ratios. |
| Age groups | User request focuses on 20s, 30s, and 40s, while README examples mention broader groups. | Prioritize 20s, 30s, and 40s for this scope, and add extensibility tests if broader groups are required later. |
| Missing weight average | Behavior is undefined when an age group has no non-zero weight. | Add a requirement decision before implementation. |
| Height units | Input field is named `height`, while README describes height in centimeters and BMI requires meters. | Tests must verify centimeter-to-meter conversion. |
| BMI boundary `25.0` | Obesity rule is `BMI >= 25`. | Include an explicit `25.0` test to prevent off-by-one or comparison mistakes. |
