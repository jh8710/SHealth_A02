# 02 코드품질분석 보고서 Prompting

## Prompt

```text
@SHealth.java

[P] 시니어 Java 아키텍트 + 클린코드 전문가입니다.
[T] updateQuality()를 SOLID/Code Smell 관점에서 분석해줘.
    - SRP/OCP 위반 지점과 근거
    - Code Smell: Long Method, Magic Number, Duplicated Code, 복잡도
    - 리팩토링 우선순위 (1~5)와 이유
[F] Markdown 표 (문제점 | 위반 원칙/스멜 | 영향 | 개선 방향 | 우선순위)
    결과를 code_quality_report.md로 저장
```

## Generated Report

# SHealth Code Quality Report

> 요청 대상은 `updateQuality()`였지만, 현재 `SHealth.java`에는 해당 메서드가 존재하지 않습니다. 본 보고서는 `SHealth.java`에서 동일한 성격의 핵심 처리 메서드인 `calculateBmi()`를 기준으로 분석했습니다.

| 문제점 | 위반 원칙/스멜 | 영향 | 개선 방향 | 우선순위 |
|---|---|---|---|---|
| `calculateBmi()`가 파일 읽기, CSV 파싱, 누락 체중 보정, BMI 계산, 연령대별 통계 계산, 결과 저장까지 모두 수행함 | SRP 위반, Long Method | 변경 이유가 여러 개로 분산되어 테스트와 수정이 어렵고, 한 단계의 변경이 다른 단계의 회귀로 이어질 가능성이 큼 | `HealthDataReader`, `MissingWeightImputer`, `BmiCalculator`, `BmiStatisticsService`처럼 책임 단위로 분리 | 1 |
| 연령대별 결과를 `underweight20`, `normalweight20` 등 24개 필드와 `if-else` 체인으로 직접 매핑함 | OCP 위반, Duplicated Code | 80대 추가, BMI 등급 추가, 기준 변경 시 필드와 조건문을 반복 수정해야 하며 누락/오타 위험이 큼 | `Map<AgeGroup, BmiRatio>` 또는 `EnumMap` 기반 자료구조로 결과를 저장하고 연령대/등급 확장을 데이터 중심으로 처리 | 1 |
| BMI 등급 기준값 `18.5`, `23`, `25`, 비율 계산값 `100`, 연령 시작/끝/간격 `20`, `70`, `10`, 배열 크기 `10000`이 코드에 직접 노출됨 | Magic Number | 기준의 의미가 코드만으로 명확하지 않고, 정책 변경 시 여러 위치를 찾아 수정해야 함 | `BMI_UNDERWEIGHT_MAX`, `BMI_NORMAL_MAX`, `MIN_AGE_GROUP`, `MAX_AGE_GROUP`, `AGE_GROUP_STEP`, `MAX_RECORDS` 같은 상수 또는 설정 객체로 분리 | 2 |
| 연령대 반복문 내부에서 동일한 조건 `ages[i] >= a && ages[i] < a + 10`이 체중 보정과 통계 계산에서 반복됨 | Duplicated Code | 연령대 판정 기준이 바뀌면 여러 반복문을 함께 수정해야 하며 조건 불일치가 발생할 수 있음 | `getAgeGroup(age)` 또는 `AgeGroup.contains(age)`로 연령대 판정 로직을 캡슐화 | 2 |
| BMI 등급 판정 조건이 `if-else`로 직접 구현되어 있고 경계값 처리가 일관되지 않음. 예: `bmis[i] <= 18.5`, `> 18.5 && < 23`, `>= 23 && < 25`, `> 25`로 인해 BMI가 정확히 `25`인 경우 어떤 등급에도 포함되지 않음 | 복잡도, Magic Number, 잠재 버그 | 특정 경계값 데이터가 통계에서 누락되어 결과 비율이 부정확해질 수 있음 | `BmiCategory.from(bmi)` 같은 분류 메서드로 기준을 한 곳에 모으고 경계값 정책을 테스트로 고정 | 1 |
| 체중 평균 보정 시 해당 연령대에 유효 체중이 하나도 없으면 `sum / ageCount`에서 0으로 나눌 수 있음 | 복잡도, 예외/결측 처리 부재 | `NaN` 또는 비정상 BMI가 전파되어 이후 통계 결과가 왜곡될 수 있음 | 연령대별 평균 계산 결과를 별도 객체로 만들고, 표본이 없을 때의 정책을 명시적으로 처리 | 2 |
| CSV 데이터를 고정 크기 배열 `ages`, `heights`, `weights`, `bmis`에 인덱스로 병렬 저장함 | Primitive Obsession, Data Clumps | 데이터 간 관계가 타입으로 표현되지 않아 인덱스 불일치, 최대 건수 초과, 필드 추가 시 변경 비용이 커짐 | `HealthRecord` 객체 리스트로 모델링하고 BMI는 계산 값 또는 별도 결과 객체로 관리 | 3 |
| `calculateBmi()` 내부 중첩 반복문과 조건문이 많고, 연령대별 집계 후 결과 저장까지 한 흐름에 섞여 있음 | 복잡도, Long Method | 메서드의 인지 복잡도가 높아 리뷰와 디버깅 비용이 증가하고 테스트 단위가 커짐 | 단계별 private 메서드 추출 후, 통계 집계는 컬렉션 기반 루프 또는 집계 객체로 단순화 | 3 |
| `split(line, ',')`로 CSV를 직접 파싱하고 입력 검증 없이 `tokens.get(1)` 등 위치 기반 접근을 수행함 | SRP 위반, 취약한 파싱 로직 | 빈 값, 컬럼 부족, 쉼표 포함 값 등 입력 변형에 취약하고 파싱 실패가 전체 계산 로직과 강하게 결합됨 | CSV 파싱 책임을 분리하고, 최소한 컬럼 수 검증 및 레코드 변환 메서드를 도입 | 4 |
| `IOException`을 `printStackTrace()`로 처리한 뒤 계산을 계속 진행함 | 오류 처리 스멜, SRP 위반 | 파일 읽기 실패가 호출자에게 명확히 전달되지 않고, 빈 데이터 기반 결과가 정상처럼 사용될 수 있음 | 예외를 호출자에게 전달하거나 도메인 예외로 감싸고, 실패 시 계산을 중단 | 4 |
| `getBmiRatio()`도 `ageClass`와 `type` 숫자 코드에 따라 긴 `if-else` 체인을 사용함 | OCP 위반, Magic Number, Duplicated Code | `calculateBmi()`의 저장 구조와 같은 확장성 문제를 반복하며, 타입 코드의 의미가 호출부에 숨겨짐 | BMI 등급을 enum으로 표현하고 `getBmiRatio(AgeGroup ageGroup, BmiCategory category)` 형태로 조회 | 5 |

## 리팩토링 우선순위 기준

- `1`: 통계 결과 오류 또는 확장성 병목을 직접 유발하므로 가장 먼저 처리해야 합니다.
- `2`: 정책 변경과 데이터 결측 처리에 취약하여 조기 개선 효과가 큽니다.
- `3`: 구조 개선 효과는 크지만, 앞선 결함성 문제를 정리한 뒤 진행하는 편이 안전합니다.
- `4`: 입력/오류 처리 안정성 개선 항목으로, 운영 데이터 품질 요구 수준에 따라 우선순위를 조정할 수 있습니다.
- `5`: 현재 핵심 계산 오류보다는 API 표현력과 중복 제거 성격이 강해 후순위로 둘 수 있습니다.
