# 10 QA 종합 검토 최종 보고서 Prompting

## 1. 사용한 프롬프트

### 1.1 QA 종합 검토 보고서 생성 프롬프트

```text
@requirements_analysis.md @code_quality_report.md @test_plan.md @defect_report.md
@SHealth.java @SHealthBMITest.java 

[P] QA 리드 엔지니어 관점에서
[T] SHealth Java 프로젝트의 QA 활동을 종합 검토해줘.
    1) 테스트 완료율/커버리지 (목표 대비 JaCoCo 수치)
    2) 결함 패턴 분석 (아이템 타입별/심각도별)
    3) 9단계 중 효과적이었던 단계 & 개선 필요 단계
    4) 다음 레거시 프로젝트를 위한 Best Practice 5가지
    5) Cursor AI 활용 효과 (시간 단축/결함 조기 발견/커버리지 향상) 정량·정성 요약
[F] Markdown 최종 보고서. qa_final_report.md로 저장
```

### 1.2 보고서 및 프롬프트 내보내기 요청

```text
이번에 한 내용을 report 폴더에 10_QA_종합_검토(최종_보고서).md 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더에 10_QA_종합_검토(최종_보고서)-Prompting.md 파일로 내보내줘
```

## 2. 프롬프트 의도

이 프롬프트는 SHealth Java BMI 프로젝트에서 수행한 1-9단계 QA 활동을 QA 리드 엔지니어 관점으로 종합 평가하기 위한 것이다.

핵심 의도는 다음과 같다.

- 요구사항 분석, 코드 품질 분석, 코드 스멜 탐지, TDD, 리팩토링, 테스트 케이스 작성, 결함 분석, Golden Master 자동화, 기능 개선까지의 QA 흐름을 통합 검토한다.
- 최종 테스트 완료율과 JaCoCo 커버리지를 실제 실행 결과 기반으로 정리한다.
- 실행 결함과 잠재 품질 리스크를 아이템 타입별, 심각도별로 분리한다.
- 효과적이었던 단계와 개선이 필요한 단계를 구분해 다음 프로젝트의 개선 포인트를 도출한다.
- Cursor AI 활용 효과를 정량 지표와 정성 평가로 함께 요약한다.

## 3. 수행 내용

참조한 주요 산출물은 다음과 같다.

- `docs/requirements_analysis.md`
- `docs/code_quality_report.md`
- `docs/unittest.md`
- `docs/defect_list.md`
- `docs/code_smell.md`
- `docs/refactoring.md`
- `docs/feature.md`
- `report/01_요구사항분석_보고서.md`
- `report/02_코드품질분석_보고서.md`
- `report/03_코드_스멜_탐지_보고서.md`
- `report/04_TDD_테스트_먼저_작성_보고서.md`
- `report/05_refactoring_보고서.md`
- `report/06_테스트_케이스_작성_보고서.md`
- `report/07_테스트_실행_결함_분석_보고서.md`
- `report/08_GoldenMaster자동화 _보고서.md`
- `report/09_feature_보고서.md`
- `src/main/java/com/bestreviewer/SHealth.java`
- `src/test/java/com/bestreviewer/SHealthBMITest.java`

최종 검증을 위해 다음 명령을 실행했다.

```bash
mvn org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report
```

실행 결과 요약:

```text
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

JaCoCo 주요 수치:

| Scope | Instruction | Branch | Line | Method |
|---|---:|---:|---:|---:|
| 전체 프로덕션 코드 | 74.7% | 89.2% | 88.2% | 91.4% |
| 핵심 도메인/파사드 코드 | 94.5% | 89.2% | 92.7% | 94.6% |

## 4. 생성 보고서

최종 보고서는 다음 파일로 생성했다.

```text
report/10_QA_종합_검토(최종_보고서).md
```

보고서에는 다음 항목을 포함했다.

- Executive Summary
- 테스트 완료율 및 JaCoCo 커버리지
- 결함 패턴 분석
- 9단계 QA 활동 효과 및 개선점
- 다음 레거시 프로젝트 Best Practice 5가지
- Cursor AI 활용 효과 정량·정성 요약
- 남은 리스크와 권고사항
- 최종 QA Sign-off

## 5. 최종 보고서 요약

최종 기준으로 `mvn test`는 전체 42건 모두 통과했다. 실행 결함은 0건이며, 핵심 BMI 계산, 누락값 보정, 연령대별 통계, 신규 조회 API, Golden Master 회귀 테스트는 Green 상태다.

JaCoCo 기준 전체 line coverage는 88.2%, branch coverage는 89.2%로 QA 목표선 80%를 충족했다. 다만 `SHealthBMI` 콘솔 진입점이 0%로 남아 전체 instruction coverage는 74.7%이며, 해당 클래스의 smoke test 추가 또는 coverage 제외 rule 정의가 필요하다.

가장 효과적이었던 단계는 TDD 테스트 먼저 작성, 리팩토링, 테스트 케이스 작성, Golden Master 자동화였다. 개선이 필요한 단계는 요구사항 분석, 테스트 실행/결함 분석, 기능 개선 단계였으며, 특히 미정 정책과 coverage gate를 더 일찍 확정할 필요가 있다.

다음 레거시 프로젝트를 위한 Best Practice는 다음 5가지로 정리했다.

1. 리팩토링 전에 characterization test를 먼저 작성한다.
2. 요구사항을 경계값 중심의 테스트 데이터로 변환한다.
3. QA risk register를 단일 파일로 관리한다.
4. 순수 도메인 로직과 I/O를 초기에 분리한다.
5. coverage gate와 제외 기준을 명시적으로 정의한다.

## 6. 산출물

이번 단계에서 생성한 파일은 다음과 같다.

- `qa_final_report.md`
- `report/10_QA_종합_검토(최종_보고서).md`
- `Prompting/10_QA_종합_검토(최종_보고서)-Prompting.md`
