package com.bestreviewer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SHealth {
    private final BmiDataReader dataReader;
    private final BMICalculator calculator;
    private List<BmiRecord> records;
    private Map<Integer, BmiStatistics> statisticsByAgeGroup;

    public SHealth() {
        this(new BmiDataReader(), new BMICalculator());
    }

    public SHealth(BMICalculator calculator) {
        this(new BmiDataReader(), calculator);
    }

    SHealth(BmiDataReader dataReader, BMICalculator calculator) {
        this.dataReader = dataReader;
        this.calculator = calculator;
        this.records = Collections.emptyList();
    }

    public int calculateBmi(String filename) {
        records = dataReader.read(filename);
        statisticsByAgeGroup = calculator.calculateStatistics(records);
        return records.size();
    }

    public double getBmiRatio(int ageClass, int type) {
        if (statisticsByAgeGroup == null || !statisticsByAgeGroup.containsKey(ageClass)) {
            return 0.0;
        }
        try {
            return statisticsByAgeGroup.get(ageClass).getRatio(BmiCategory.fromCode(type));
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }

    public BmiStatistics getAgeGroupDistribution(int ageClass) {
        return calculator.calculateAgeGroupDistribution(records, ageClass);
    }

    public Map<BmiCategory, Double> getBmiDistributionRatio(int ageClass) {
        return getAgeGroupDistribution(ageClass).toRatioMap();
    }

    public List<BmiRecord> getNormalRangeUsers() {
        return calculator.findNormalRangeUsers(records);
    }

    public List<String> getNormalBmiUserIds() {
        return calculator.findNormalRangeUserIds(records);
    }

    public BmiStatistics getOverallCategoryRatio() {
        return calculator.calculateOverallCategoryRatio(records);
    }

    public Map<BmiCategory, Double> getOverallBmiCategoryRatios() {
        return getOverallCategoryRatio().toRatioMap();
    }
}