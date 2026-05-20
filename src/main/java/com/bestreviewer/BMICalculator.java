package com.bestreviewer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BMICalculator {
    private static final double CENTIMETERS_PER_METER = 100.0;
    private static final double MISSING_VALUE = 0.0;
    private static final int AGE_GROUP_SIZE = 10;
    private static final double UNDERWEIGHT_MAX_BMI = 18.5;
    private static final double NORMAL_MAX_BMI = 23.0;
    private static final double OVERWEIGHT_MAX_BMI = 25.0;
    private static final double BMI_BOUNDARY_EPSILON = 0.000000001;

    public double calculateBmi(double weight, double height) {
        if (height <= MISSING_VALUE) {
            throw new IllegalArgumentException("Height must be greater than 0.");
        }
        double heightInMeters = height / CENTIMETERS_PER_METER;
        return weight / (heightInMeters * heightInMeters);
    }

    public BmiCategory classify(double bmi) {
        if (bmi <= UNDERWEIGHT_MAX_BMI + BMI_BOUNDARY_EPSILON) {
            return BmiCategory.UNDERWEIGHT;
        }
        if (bmi < NORMAL_MAX_BMI - BMI_BOUNDARY_EPSILON) {
            return BmiCategory.NORMAL;
        }
        if (bmi < OVERWEIGHT_MAX_BMI - BMI_BOUNDARY_EPSILON) {
            return BmiCategory.OVERWEIGHT;
        }
        return BmiCategory.OBESITY;
    }

    public List<BmiRecord> correctByAgeGroupAverage(List<BmiRecord> records) {
        List<BmiRecord> correctedRecords = new ArrayList<>();
        for (BmiRecord record : records) {
            AgeGroupAverage average = calculateAgeGroupAverage(records, getAgeGroup(record.getAge()));
            correctedRecords.add(correctMissingValues(record, average));
        }
        return correctedRecords;
    }

    public Map<Integer, BmiStatistics> calculateStatistics(List<BmiRecord> records) {
        Map<Integer, List<BmiRecord>> recordsByAgeGroup = groupByAgeGroup(correctByAgeGroupAverage(records));
        Map<Integer, BmiStatistics> statistics = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<BmiRecord>> entry : recordsByAgeGroup.entrySet()) {
            statistics.put(entry.getKey(), calculateCategoryDistribution(entry.getValue()));
        }
        return statistics;
    }

    public BmiStatistics calculateAgeGroupDistribution(List<BmiRecord> records, int ageGroup) {
        List<BmiRecord> targetRecords = new ArrayList<>();
        for (BmiRecord record : correctByAgeGroupAverage(records)) {
            if (getAgeGroup(record.getAge()) == ageGroup) {
                targetRecords.add(record);
            }
        }
        return calculateCategoryDistribution(targetRecords);
    }

    public List<BmiRecord> findNormalRangeUsers(List<BmiRecord> records) {
        List<BmiRecord> normalUsers = new ArrayList<>();
        for (BmiRecord record : correctByAgeGroupAverage(records)) {
            if (classify(calculateBmi(record.getWeight(), record.getHeight())) == BmiCategory.NORMAL) {
                normalUsers.add(record);
            }
        }
        return normalUsers;
    }

    public List<String> findNormalRangeUserIds(List<BmiRecord> records) {
        List<String> normalUserIds = new ArrayList<>();
        for (BmiRecord record : findNormalRangeUsers(records)) {
            normalUserIds.add(record.getId());
        }
        return normalUserIds;
    }

    public BmiStatistics calculateOverallCategoryRatio(List<BmiRecord> records) {
        return calculateCategoryDistribution(correctByAgeGroupAverage(records));
    }

    private Map<Integer, List<BmiRecord>> groupByAgeGroup(List<BmiRecord> records) {
        Map<Integer, List<BmiRecord>> recordsByAgeGroup = new LinkedHashMap<>();
        for (BmiRecord record : records) {
            int ageGroup = getAgeGroup(record.getAge());
            if (!recordsByAgeGroup.containsKey(ageGroup)) {
                recordsByAgeGroup.put(ageGroup, new ArrayList<BmiRecord>());
            }
            recordsByAgeGroup.get(ageGroup).add(record);
        }
        return recordsByAgeGroup;
    }

    private BmiStatistics calculateCategoryDistribution(List<BmiRecord> records) {
        Map<BmiCategory, Integer> categoryCounts = createEmptyCategoryCounts();
        for (BmiRecord record : records) {
            BmiCategory category = classify(calculateBmi(record.getWeight(), record.getHeight()));
            categoryCounts.put(category, categoryCounts.get(category) + 1);
        }
        return new BmiStatistics(records.size(), categoryCounts);
    }

    private Map<BmiCategory, Integer> createEmptyCategoryCounts() {
        Map<BmiCategory, Integer> categoryCounts = new EnumMap<>(BmiCategory.class);
        for (BmiCategory category : BmiCategory.values()) {
            categoryCounts.put(category, 0);
        }
        return categoryCounts;
    }

    private BmiRecord correctMissingValues(BmiRecord record, AgeGroupAverage average) {
        double weight = isMissing(record.getWeight()) ? average.getWeight() : record.getWeight();
        double height = isMissing(record.getHeight()) ? average.getHeight() : record.getHeight();
        return record.withWeightAndHeight(weight, height);
    }

    private AgeGroupAverage calculateAgeGroupAverage(List<BmiRecord> records, int ageGroup) {
        double weightSum = 0.0;
        int weightCount = 0;
        double heightSum = 0.0;
        int heightCount = 0;

        for (BmiRecord record : records) {
            if (getAgeGroup(record.getAge()) != ageGroup) {
                continue;
            }
            if (!isMissing(record.getWeight())) {
                weightSum += record.getWeight();
                weightCount++;
            }
            if (!isMissing(record.getHeight())) {
                heightSum += record.getHeight();
                heightCount++;
            }
        }
        return new AgeGroupAverage(average(weightSum, weightCount), average(heightSum, heightCount));
    }

    private double average(double sum, int count) {
        if (count == 0) {
            return MISSING_VALUE;
        }
        return sum / count;
    }

    private boolean isMissing(double value) {
        return value == MISSING_VALUE;
    }

    private int getAgeGroup(int age) {
        return age / AGE_GROUP_SIZE * AGE_GROUP_SIZE;
    }

    private static class AgeGroupAverage {
        private final double weight;
        private final double height;

        private AgeGroupAverage(double weight, double height) {
            this.weight = weight;
            this.height = height;
        }

        private double getWeight() {
            return weight;
        }

        private double getHeight() {
            return height;
        }
    }
}
