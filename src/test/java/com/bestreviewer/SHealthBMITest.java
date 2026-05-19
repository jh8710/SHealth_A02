package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SHealthBMITest {
    private static final double DELTA = 0.001;

    @Test
    void should_calculate_bmi_when_weight_and_height_are_valid() {
        BMICalculator calculator = new BMICalculator();

        double bmi = calculator.calculateBmi(72.0, 180.0);

        assertEquals(22.222, bmi, DELTA);
    }

    @Test
    void should_apply_average_correction_when_weight_and_height_are_zero() {
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(25, 60.0, 170.0),
                new BmiRecord(27, 80.0, 180.0),
                new BmiRecord(29, 0.0, 0.0)
        );

        List<BmiRecord> correctedRecords = calculator.correctByAgeGroupAverage(records);

        assertAll(
                () -> assertEquals(70.0, correctedRecords.get(2).getWeight(), DELTA),
                () -> assertEquals(175.0, correctedRecords.get(2).getHeight(), DELTA)
        );
    }

    @Test
    void should_classify_bmi_category_when_bmi_boundary_values_are_given() {
        BMICalculator calculator = new BMICalculator();

        assertAll(
                () -> assertEquals(BmiCategory.UNDERWEIGHT, calculator.classify(18.5)),
                () -> assertEquals(BmiCategory.NORMAL, calculator.classify(22.9)),
                () -> assertEquals(BmiCategory.OVERWEIGHT, calculator.classify(24.9)),
                () -> assertEquals(BmiCategory.OBESITY, calculator.classify(25.0))
        );
    }

    @Test
    void should_calculate_age_group_statistics_when_records_have_different_age_groups() {
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(21, 50.0, 170.0),
                new BmiRecord(25, 65.0, 170.0),
                new BmiRecord(31, 70.0, 170.0),
                new BmiRecord(35, 80.0, 170.0)
        );

        Map<Integer, BmiStatistics> statistics = calculator.calculateStatistics(records);

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
    void should_calculate_specific_age_group_bmi_distribution() {
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(24, 50.0, 170.0),
                new BmiRecord(26, 65.0, 170.0),
                new BmiRecord(34, 80.0, 170.0)
        );

        BmiStatistics statistics = calculator.calculateAgeGroupDistribution(records, 20);

        assertAll(
                () -> assertEquals(50.0, statistics.getUnderweightRatio(), DELTA),
                () -> assertEquals(50.0, statistics.getNormalRatio(), DELTA),
                () -> assertEquals(0.0, statistics.getObesityRatio(), DELTA)
        );
    }

    @Test
    void should_find_users_in_normal_bmi_range() {
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord("user1", 24, 50.0, 170.0),
                new BmiRecord("user2", 26, 65.0, 170.0),
                new BmiRecord("user3", 34, 80.0, 170.0)
        );

        List<BmiRecord> normalUsers = calculator.findNormalRangeUsers(records);

        assertAll(
                () -> assertEquals(1, normalUsers.size()),
                () -> assertEquals("user2", normalUsers.get(0).getId())
        );
    }

    @Test
    void should_calculate_overall_category_ratio() {
        BMICalculator calculator = new BMICalculator();
        List<BmiRecord> records = Arrays.asList(
                new BmiRecord(24, 50.0, 170.0),
                new BmiRecord(26, 65.0, 170.0),
                new BmiRecord(34, 70.0, 170.0),
                new BmiRecord(38, 80.0, 170.0)
        );

        BmiStatistics statistics = calculator.calculateOverallCategoryRatio(records);

        assertAll(
                () -> assertEquals(25.0, statistics.getUnderweightRatio(), DELTA),
                () -> assertEquals(25.0, statistics.getNormalRatio(), DELTA),
                () -> assertEquals(25.0, statistics.getOverweightRatio(), DELTA),
                () -> assertEquals(25.0, statistics.getObesityRatio(), DELTA)
        );
    }
}
