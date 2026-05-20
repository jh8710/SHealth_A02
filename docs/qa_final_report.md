# SHealth Java QA Final Report

## 1. Executive Summary

SHealth BMI 프로젝트의 QA 활동은 요구사항 분석, 코드 품질 진단, TDD 기반 테스트 작성, 리팩토링, 기능 개선, 테스트 실행, Golden Master 회귀 테스트까지 9단계로 진행되었다.

최종 기준으로 `mvn test`는 전체 42건 모두 통과했다. JaCoCo는 별도 Maven 플러그인 설정 없이 CLI로 생성했으며, 전체 프로덕션 코드 기준 라인 커버리지는 88.2%, 브랜치 커버리지는 89.2%다. 단, `SHealthBMI` 콘솔 진입점이 JaCoCo상 0%로 남아 전체 instruction 커버리지는 74.7%에 그친다. 핵심 도메인/파사드 코드에서 `SHealthBMI`를 제외하면 instruction 94.5%, line 92.7%로 QA 목표선 80%를 충분히 초과한다.

실행 결함은 0건이지만, QA 관점의 잠재 리스크는 입력 검증, 예외 정책, CLI 진입점 테스트, 일부 불명확 요구사항에 남아 있다. 다음 레거시 프로젝트에서는 요구사항 불명확성 해소, characterisation test 선행, 리팩토링 전후 커버리지 게이트, 결함 taxonomy 유지, AI 산출물 검증 루프를 표준화하는 것이 좋다.

## 2. Test Completion And Coverage

### 2.1 Test Execution Result

검증 명령:

```bash
mvn org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report
```

최종 테스트 결과:

| Test Class | Tests | Failures | Errors | Skipped | Result |
|---|---:|---:|---:|---:|---|
| `com.bestreviewer.SHealthBMITest` | 41 | 0 | 0 | 0 | Pass |
| `com.bestreviewer.TexttestFixtureGoldenMasterTest` | 1 | 0 | 0 | 0 | Pass |
| Total | 42 | 0 | 0 | 0 | Pass |

테스트 완료율은 실행 기준 42/42, 100%다. 요구사항 분석에서 도출한 주요 시나리오 24건 중 자동화로 충분히 검증된 항목은 약 21건이며, 남은 3건은 정책 결정 또는 보강 테스트가 필요하다.

| Requirement Area | Coverage Status | Notes |
|---|---|---|
| BMI 계산식 및 cm-to-m 변환 | Covered | 정상 계산 테스트로 검증 |
| BMI 경계값 분류 | Covered | `18.5`, `25.0` 포함 |
| 20대/30대/40대 통계 | Covered | 각 연령대 5명 이상, 4개 카테고리 비율 검증 |
| `weight=0` 평균 보정 | Covered | 연령대별 평균 `63.25kg` 검증 |
| CSV 헤더 제외 및 API 통합 | Covered | 임시 CSV 기반 `SHealth.calculateBmi()` 검증 |
| Golden Master 출력 회귀 | Covered | 기준 입력/출력 파일 비교 |
| 전체 유효 체중이 없는 연령대 | Gap | 요구사항 정책 미정 |
| malformed CSV 처리 | Gap | CSV 컬럼 부족/비수치 입력에 대한 기대 정책 보강 필요 |
| CLI 진입점 `SHealthBMI.main` | Gap | JaCoCo 기준 미커버 |

### 2.2 JaCoCo Coverage Against Target

프로젝트 `pom.xml`에는 JaCoCo rule 또는 coverage threshold가 정의되어 있지 않다. 따라서 본 보고서는 QA 기본 목표를 line 80%, branch 80%로 두고 판단했다.

전체 프로덕션 코드 기준:

| Metric | Covered / Total | Coverage | Target | Status |
|---|---:|---:|---:|---|
| Instruction | 856 / 1,146 | 74.7% | 80.0% | Below target |
| Branch | 66 / 74 | 89.2% | 80.0% | Met |
| Line | 179 / 203 | 88.2% | 80.0% | Met |
| Complexity | 82 / 95 | 86.3% | 80.0% | Met |
| Method | 53 / 58 | 91.4% | 80.0% | Met |

핵심 도메인/파사드 코드 기준, 즉 미실행 콘솔 어댑터 `SHealthBMI`를 제외한 수치:

| Metric | Covered / Total | Coverage | Target | Status |
|---|---:|---:|---:|---|
| Instruction | 856 / 906 | 94.5% | 80.0% | Met |
| Branch | 66 / 74 | 89.2% | 80.0% | Met |
| Line | 179 / 193 | 92.7% | 80.0% | Met |
| Method | 53 / 56 | 94.6% | 80.0% | Met |

Coverage 해석:

- 핵심 계산 로직인 `BMICalculator`, `BmiRecord`, `BmiStatistics`, `BmiDataReader`, `SHealth`는 실질적으로 목표를 충족한다.
- 전체 instruction 커버리지가 목표 미달인 주된 원인은 `SHealthBMI` 콘솔 출력 진입점이 JaCoCo에서 0%로 집계되기 때문이다.
- 다음 개선은 `SHealthBMI.main()`을 직접 실행하는 smoke test를 추가하거나, 해당 클래스가 단순 실행 어댑터라면 coverage rule에서 제외하는 방향이 적절하다.

## 3. Defect Pattern Analysis

### 3.1 Executed Defects

최종 `mvn test` 기준 재현된 실행 결함은 없다.

| Type | Count | Severity | Summary |
|---|---:|---|---|
| Test failure | 0 | N/A | 42건 모두 통과 |
| Runtime error | 0 | N/A | Surefire 결과 오류 없음 |
| Confirmed functional defect | 0 | N/A | 현재 테스트 기준 기대값/실제값 차이 없음 |

### 3.2 Potential Defects And Quality Risks

초기 코드 품질 분석과 코드 스멜 탐지에서 식별된 잠재 결함은 대부분 리팩토링과 테스트로 완화되었다. QA 리스크로 추적해야 할 패턴은 다음과 같다.

| Item Type | Count | Representative Pattern | Current Status |
|---|---:|---|---|
| Functional correctness | 3 | BMI `25.0` 경계 누락, cm-to-m 변환, 누락값 평균 정책 | 경계/변환은 테스트로 고정, 전체 누락값 정책은 보강 필요 |
| Design maintainability | 5 | God Class, Long Method, Magic Number, duplicated age/category mapping, primitive data clumps | `BMICalculator`, `BmiRecord`, `BmiStatistics`, `BmiCategory`로 상당 부분 개선 |
| Input/error handling | 2 | malformed CSV, 파일 읽기 실패 정책 | 기본 파싱은 분리됐으나 실패 정책 테스트 추가 필요 |
| Test/coverage gap | 1 | `SHealthBMI.main()` 미커버 | smoke test 또는 coverage 제외 기준 필요 |

심각도별로 보면 초기 품질 리스크는 Critical 2건, High 4건, Medium 4건, Low 1건으로 분류된다. 실행 결함은 0건이지만, 레거시 구조에서 발생 가능한 결함은 기능 로직보다 “경계값 + 중복 조건 + 불명확 정책” 조합에 집중되어 있었다.

주요 결함 패턴:

| Pattern | Risk | QA Learning |
|---|---|---|
| Boundary condition defect | `BMI > 25`처럼 경계 하나가 누락되면 통계 비율이 조용히 틀어짐 | 요구사항 분석 단계에서 경계값을 테스트명과 데이터에 직접 반영해야 함 |
| Missing value policy ambiguity | 같은 연령대 유효 표본이 없을 때 `NaN` 또는 비정상 보정 가능 | 결측 정책은 구현 전에 명시하고 negative test로 고정해야 함 |
| Structural duplication | 연령대/카테고리별 필드와 if-else 반복은 수정 누락을 유발 | enum, map, 도메인 객체로 데이터 중심 구조를 만든 뒤 테스트해야 함 |
| Weak input contract | CSV 컬럼 부족, 비수치 값, 파일 실패가 정상 결과처럼 흐를 수 있음 | parser 단위 테스트와 실패 정책 테스트가 필요함 |
| Adapter coverage blind spot | 콘솔 main이 실제 출력 경로인데 커버리지에서 빠짐 | smoke test 또는 Golden Master와 연결된 실행 경로를 확보해야 함 |

## 4. Review Of The 9 QA Stages

| Stage | Assessment | Effective Points | Improvement Points |
|---|---|---|---|
| 1. 요구사항 분석 | Effective | BMI 공식, 경계값, 연령대, 누락 체중 규칙을 테스트 가능한 시나리오로 분해 | 전체 유효 체중 없음, malformed CSV, 통계 단위 같은 정책은 더 일찍 확정 필요 |
| 2. 코드 품질 분석 | Effective | SRP/OCP, Magic Number, 중복 로직을 우선순위화 | 코드 스멜 보고서와 중복되는 항목은 하나의 risk register로 통합 필요 |
| 3. 코드 스멜 탐지 | Effective | God Class, Long Method, BMI 25 경계 위험을 명확히 식별 | 스멜별 “테스트로 막을 증상”을 함께 연결하면 실행력이 더 좋아짐 |
| 4. TDD 테스트 먼저 작성 | Very effective | Red 단계에서 필요한 도메인 타입과 API를 자연스럽게 도출 | 실패 로그와 expected behavior를 더 세밀히 보관하면 회귀 추적성이 좋아짐 |
| 5. Refactoring | Very effective | 계산 로직과 파일 입출력을 분리해 테스트 가능성을 크게 높임 | 리팩토링 직후 커버리지 baseline을 남기면 개선 효과가 더 명확함 |
| 6. 테스트 케이스 작성 | Very effective | 20/30/40대, 4개 카테고리, 누락값, API 통합까지 자동화 | CSV negative case와 all-missing age group 케이스 보강 필요 |
| 7. 테스트 실행/결함 분석 | Effective | 42건 Green 상태와 결함 없음 상태를 확인 | 실패가 없을 때도 잠재 결함과 미검증 리스크를 별도 defect backlog로 유지 필요 |
| 8. Golden Master 자동화 | Very effective | 출력 회귀를 파일 비교로 자동 감지하고 CI 친화적으로 구성 | 의도된 변경 승인 절차와 diff review rule을 문서화하면 더 안정적 |
| 9. 기능 개선 | Effective | SRP 적용, height 보정, 정상 사용자 ID, 전체 비율 API까지 확장 | 기능 추가 범위가 넓어질 때 acceptance criteria와 coverage gate를 먼저 선언 필요 |

가장 효과적이었던 단계는 4, 5, 6, 8단계다. TDD로 요구사항을 실행 가능한 형태로 바꾸고, 리팩토링으로 구조를 분리한 뒤, 단위/통합/Golden Master 테스트를 함께 붙인 흐름이 품질 개선의 중심이었다.

개선이 필요한 단계는 1, 7, 9단계다. 요구사항의 미정 정책을 backlog로 남긴 점, 테스트 Green 상태에서 결함 분석이 “확정 결함 없음”에 머문 점, 기능 개선 범위가 넓어질 때 coverage gate가 사전에 선언되지 않은 점은 다음 프로젝트에서 보완해야 한다.

## 5. Best Practices For The Next Legacy Project

1. Start with characterization tests before refactoring. 레거시 코드는 의도를 모르는 상태에서 구조를 바꾸기 쉽기 때문에, 현재 동작을 Golden Master 또는 API 통합 테스트로 먼저 고정한다.

2. Convert requirements into boundary-first test data. BMI처럼 숫자 정책이 있는 도메인은 대표값보다 경계값이 중요하다. `18.5`, `23.0`, `25.0`, 연령대 `20/29/30/39/40/49` 같은 값을 테스트 데이터에 명시한다.

3. Keep a single QA risk register. 요구사항 리스크, 코드 스멜, 결함, 테스트 갭을 따로 문서화하면 중복이 생긴다. Item Type, Severity, Evidence, Test Coverage, Owner, Status를 한 표로 관리한다.

4. Separate pure domain logic from I/O early. 파일 읽기와 계산을 분리하면 단위 테스트 속도와 정확도가 올라가며, 리팩토링 중 회귀 원인도 빨리 좁힐 수 있다.

5. Define coverage gates and exclusions explicitly. 단순 CLI adapter처럼 테스트 가치가 낮은 코드는 제외 기준을 정하고, 핵심 도메인 로직에는 line/branch 80% 이상의 gate를 적용한다.

## 6. Cursor AI Utilization Impact

정량 요약:

| Area | Before / Risk | After / Result | Impact |
|---|---|---|---|
| Test automation | 초기 핵심 테스트 4건 수준 | 최종 42건 Green | 테스트 검증 범위 대폭 확대 |
| Coverage | 측정 기준 없음 | 전체 line 88.2%, branch 89.2%; 핵심 line 92.7% | 목표 80% 대비 핵심 로직 초과 달성 |
| Defect discovery | BMI 경계값, God Class, Magic Number가 코드에 잠재 | BMI `25.0` 경계와 구조적 리스크를 조기 식별 | 기능 결함이 운영 출력으로 전파되기 전 차단 |
| Regression safety | 수동 출력 확인 필요 | Golden Master 1건 자동화 | 출력 회귀 자동 감지 가능 |
| Time efficiency | README 기준 전체 활동 계획 6시간 | 9개 단계 산출물, 테스트, 리팩토링, 보고서까지 같은 실습 범위 내 수행 | 별도 타임 트래킹은 없으나 QA 리드 추정 30-40% 분석/문서화 시간 단축 |

정성 요약:

- 요구사항을 테스트 가능한 시나리오로 바꾸는 속도가 빨라졌다.
- 코드 스멜을 단순 나열하지 않고, 결함 가능성과 리팩토링 우선순위로 연결할 수 있었다.
- TDD 과정에서 필요한 도메인 타입이 자연스럽게 드러나 구현 구조가 단순해졌다.
- 반복적인 테스트 데이터, `@ParameterizedTest`, 보고서 초안 작성에서 생산성 향상이 컸다.
- 반대로, AI 산출물은 coverage 목표, 결함 수, 파일명 불일치처럼 사실 검증이 필요한 항목을 포함할 수 있어 최종 QA 리드는 반드시 실제 빌드와 리포트로 검증해야 한다.

## 7. Remaining Risks And Recommendations

| Priority | Recommendation | Rationale |
|---:|---|---|
| P1 | `SHealthBMI.main()` smoke test 추가 또는 coverage 제외 rule 정의 | 전체 instruction coverage 미달 원인을 제거하거나 합리적으로 제외 |
| P1 | all-missing age group 정책 결정 및 테스트 추가 | 평균 보정의 가장 위험한 결측 edge case |
| P2 | malformed CSV, non-numeric field, missing column 테스트 추가 | 운영 데이터 품질 문제에 대한 회복력 강화 |
| P2 | JaCoCo Maven plugin과 coverage threshold를 `pom.xml`에 명시 | CI에서 coverage regression 자동 차단 |
| P3 | QA risk register를 `docs` 또는 `report`에 단일 파일로 유지 | 코드 스멜, 결함, 테스트 갭의 추적성 향상 |

## 8. Final QA Sign-off

현재 작업본은 핵심 BMI 계산, 누락값 보정, 연령대별 통계, 신규 조회 API, Golden Master 회귀 테스트 관점에서 릴리스 가능한 Green 상태다.

단, 운영 품질 기준으로는 `SHealthBMI` 실행 경로 커버리지, CSV 오류 입력 정책, 전체 누락값 보정 정책을 추가로 보강한 뒤 coverage gate를 CI에 고정하는 것을 권장한다.
