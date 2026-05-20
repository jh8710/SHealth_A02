package com.bestreviewer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SHealthBMITest {
    private static final double DELTA = 0.001;

    @Test
    @DisplayName("Given 유효한 체중과 키가 있을 때 When BMI를 계산하면 Then cm를 m로 변환해 BMI를 반환한다")
    void should_calculate_bmi_when_weight_and_height_are_valid() {
        // Given
        BMICalculator calculator = new BMICalculator();

        // When
        double bmi = calculator.calculateBmi(72.0, 180.0);

        // Then
        assertEquals(22.222, bmi, DELTA);
    }

    @Test
    @DisplayName("Given 체중과 키가 모두 누락됐을 때 When 보정하면 Then 동일 연령대 평균 체중과 평균 키를 적용한다")
    void should_apply_average_correction_when_weight_and_height_are_zero() {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(25, 60.0, 170.0),
                new BmiRecord(27, 80.0, 180.0),
                new BmiRecord(29, 0.0, 0.0)
        );

        // When
        List<BmiRecord> correctedRecords = calculator.correctByAgeGroupAverage(records);

        // Then
        assertAll(
                () -> assertEquals(70.0, correctedRecords.get(2).getWeight(), DELTA),
                () -> assertEquals(175.0, correctedRecords.get(2).getHeight(), DELTA)
        );
    }

    @Test
    @DisplayName("Given BMI 경계값이 있을 때 When 분류하면 Then 요구사항의 카테고리 경계를 따른다")
    void should_classify_bmi_category_when_bmi_boundary_values_are_given() {
        // Given
        BMICalculator calculator = new BMICalculator();

        // When & Then
        assertAll(
                () -> assertEquals(BmiCategory.UNDERWEIGHT, calculator.classify(18.5)),
                () -> assertEquals(BmiCategory.NORMAL, calculator.classify(22.9)),
                () -> assertEquals(BmiCategory.OVERWEIGHT, calculator.classify(24.9)),
                () -> assertEquals(BmiCategory.OBESITY, calculator.classify(25.0))
        );
    }

    @Test
    @DisplayName("Given 여러 연령대 기록이 있을 때 When 통계를 계산하면 Then 연령대별 BMI 비율을 반환한다")
    void should_calculate_age_group_statistics_when_records_have_different_age_groups() {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(21, 50.0, 170.0),
                new BmiRecord(25, 65.0, 170.0),
                new BmiRecord(31, 70.0, 170.0),
                new BmiRecord(35, 80.0, 170.0)
        );

        // When
        Map<Integer, BmiStatistics> statistics = calculator.calculateStatistics(records);

        // Then
        BmiStatistics twenties = statistics.get(20);
        BmiStatistics thirties = statistics.get(30);
        assertAll(
                () -> assertEquals(50.0, twenties.getUnderweightRatio(), DELTA),
                () -> assertEquals(50.0, twenties.getNormalRatio(), DELTA),
                () -> assertEquals(50.0, thirties.getOverweightRatio(), DELTA),
                () -> assertEquals(50.0, thirties.getObesityRatio(), DELTA)
        );
    }

    @Test
    @DisplayName("Given 대상 연령대가 있을 때 When 분포를 조회하면 Then 해당 연령대만 집계한다")
    void should_calculate_specific_age_group_bmi_distribution() {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(24, 50.0, 170.0),
                new BmiRecord(26, 65.0, 170.0),
                new BmiRecord(34, 80.0, 170.0)
        );

        // When
        BmiStatistics statistics = calculator.calculateAgeGroupDistribution(records, 20);

        // Then
        assertAll(
                () -> assertEquals(50.0, statistics.getUnderweightRatio(), DELTA),
                () -> assertEquals(50.0, statistics.getNormalRatio(), DELTA),
                () -> assertEquals(0.0, statistics.getObesityRatio(), DELTA)
        );
    }

    @Test
    @DisplayName("Given 정상 BMI 범위 사용자가 있을 때 When 조회하면 Then 정상 범위 사용자만 반환한다")
    void should_find_users_in_normal_bmi_range() {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord("user1", 24, 50.0, 170.0),
                new BmiRecord("user2", 26, 65.0, 170.0),
                new BmiRecord("user3", 34, 80.0, 170.0)
        );

        // When
        List<BmiRecord> normalUsers = calculator.findNormalRangeUsers(records);

        // Then
        assertAll(
                () -> assertEquals(1, normalUsers.size()),
                () -> assertEquals("user2", normalUsers.get(0).getId())
        );
    }

    @Test
    @DisplayName("Given 전체 기록이 있을 때 When 전체 카테고리 비율을 계산하면 Then 네 카테고리 비율을 반환한다")
    void should_calculate_overall_category_ratio() {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(24, 50.0, 170.0),
                new BmiRecord(26, 65.0, 170.0),
                new BmiRecord(34, 70.0, 170.0),
                new BmiRecord(38, 80.0, 170.0)
        );

        // When
        BmiStatistics statistics = calculator.calculateOverallCategoryRatio(records);

        // Then
        assertAll(
                () -> assertEquals(25.0, statistics.getUnderweightRatio(), DELTA),
                () -> assertEquals(25.0, statistics.getNormalRatio(), DELTA),
                () -> assertEquals(25.0, statistics.getOverweightRatio(), DELTA),
                () -> assertEquals(25.0, statistics.getObesityRatio(), DELTA)
        );
    }

    @ParameterizedTest(name = "{0}대 {1} 통계는 {2}")
    @DisplayName("Given 20대/30대/40대별 5명 기록이 있을 때 When 통계를 계산하면 Then 저체중/정상/과체중/비만 비율과 총 인원을 계산한다")
    @CsvSource({
            "20, UNDERWEIGHT, 20.0",
            "20, NORMAL, 40.0",
            "20, OVERWEIGHT, 20.0",
            "20, OBESITY, 20.0",
            "20, TOTAL_COUNT, 5.0",
            "30, UNDERWEIGHT, 20.0",
            "30, NORMAL, 40.0",
            "30, OVERWEIGHT, 20.0",
            "30, OBESITY, 20.0",
            "30, TOTAL_COUNT, 5.0",
            "40, UNDERWEIGHT, 20.0",
            "40, NORMAL, 40.0",
            "40, OVERWEIGHT, 20.0",
            "40, OBESITY, 20.0",
            "40, TOTAL_COUNT, 5.0"
    })
    void should_calculate_each_age_group_bmi_statistics_with_at_least_five_cases(
            int ageGroup, String metric, double expectedValue
    ) {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = createTwentyThirtyFortyRecords();

        // When
        BmiStatistics statistics = calculator.calculateStatistics(records).get(ageGroup);

        // Then
        if ("TOTAL_COUNT".equals(metric)) {
            assertEquals((int) expectedValue, statistics.getTotalCount());
            return;
        }
        assertEquals(expectedValue, statistics.getRatio(BmiCategory.valueOf(metric)), DELTA);
    }

    @ParameterizedTest(name = "{0}대 누락 체중은 {1}kg으로 보정된다")
    @DisplayName("Given 20대/30대/40대별 weight=0 기록이 있을 때 When 보정하면 Then 동일 연령대 평균 체중으로 보정한다")
    @CsvSource({
            "20, 63.25",
            "30, 63.25",
            "40, 63.25"
    })
    void should_correct_missing_weight_by_same_age_group_average(int ageGroup, double expectedWeight) {
        // Given
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = createTwentyThirtyFortyRecords();

        // When
        List<BmiRecord> correctedRecords = calculator.correctByAgeGroupAverage(records);

        // Then
        BmiRecord missingWeightRecord = findRecordByAge(correctedRecords, ageGroup + 9);
        assertEquals(expectedWeight, missingWeightRecord.getWeight(), DELTA);
    }

    @ParameterizedTest(name = "{0}대 카테고리 코드 {1} 비율은 {2}%")
    @DisplayName("Given CSV 파일에 20대/30대/40대 데이터가 있을 때 When SHealth API로 조회하면 Then 나이대별 BMI 비율을 반환한다")
    @CsvSource({
            "20, 100, 20.0",
            "20, 200, 40.0",
            "20, 300, 20.0",
            "20, 400, 20.0",
            "30, 100, 20.0",
            "30, 200, 40.0",
            "30, 300, 20.0",
            "30, 400, 20.0",
            "40, 100, 20.0",
            "40, 200, 40.0",
            "40, 300, 20.0",
            "40, 400, 20.0"
    })
    void should_calculate_bmi_ratio_by_age_group_through_shealth_api(
            int ageGroup, int categoryCode, double expectedRatio
    ) throws IOException {
        // Given
        Path dataFile = Files.createTempFile("shealth-bmi-", ".dat");
        try {
            Files.write(dataFile, createCsvLines(), StandardCharsets.UTF_8);
            SHealth sHealth = new SHealth();

            // When
            int recordCount = sHealth.calculateBmi(dataFile.toString());

            // Then
            assertAll(
                    () -> assertEquals(15, recordCount),
                    () -> assertEquals(expectedRatio, sHealth.getBmiRatio(ageGroup, categoryCode), DELTA)
            );
        } finally {
            Files.deleteIfExists(dataFile);
        }
    }

    private List<BmiRecord> createTwentyThirtyFortyRecords() {
        return Arrays.asList(
                new BmiRecord("twenty-underweight", 20, 50.0, 170.0),
                new BmiRecord("twenty-normal", 22, 60.0, 170.0),
                new BmiRecord("twenty-overweight", 25, 68.0, 170.0),
                new BmiRecord("twenty-obesity", 27, 75.0, 170.0),
                new BmiRecord("twenty-missing-weight", 29, 0.0, 170.0),
                new BmiRecord("thirty-underweight", 30, 50.0, 170.0),
                new BmiRecord("thirty-normal", 32, 60.0, 170.0),
                new BmiRecord("thirty-overweight", 35, 68.0, 170.0),
                new BmiRecord("thirty-obesity", 37, 75.0, 170.0),
                new BmiRecord("thirty-missing-weight", 39, 0.0, 170.0),
                new BmiRecord("forty-underweight", 40, 50.0, 170.0),
                new BmiRecord("forty-normal", 42, 60.0, 170.0),
                new BmiRecord("forty-overweight", 45, 68.0, 170.0),
                new BmiRecord("forty-obesity", 47, 75.0, 170.0),
                new BmiRecord("forty-missing-weight", 49, 0.0, 170.0)
        );
    }

    private List<String> createCsvLines() {
        return Arrays.asList(
                "id,age,weight,height",
                "twenty-underweight,20,50.0,170.0",
                "twenty-normal,22,60.0,170.0",
                "twenty-overweight,25,68.0,170.0",
                "twenty-obesity,27,75.0,170.0",
                "twenty-missing-weight,29,0.0,170.0",
                "thirty-underweight,30,50.0,170.0",
                "thirty-normal,32,60.0,170.0",
                "thirty-overweight,35,68.0,170.0",
                "thirty-obesity,37,75.0,170.0",
                "thirty-missing-weight,39,0.0,170.0",
                "forty-underweight,40,50.0,170.0",
                "forty-normal,42,60.0,170.0",
                "forty-overweight,45,68.0,170.0",
                "forty-obesity,47,75.0,170.0",
                "forty-missing-weight,49,0.0,170.0"
        );
    }

    private BmiRecord findRecordByAge(List<BmiRecord> records, int age) {
        for (BmiRecord record : records) {
            if (record.getAge() == age) {
                return record;
            }
        }
        fail("Expected record age was not found: " + age);
        return null;
    }
}
