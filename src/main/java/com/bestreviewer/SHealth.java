package com.bestreviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SHealth {
    private static final char CSV_DELIMITER = ',';
    private static final int ID_INDEX = 0;
    private static final int AGE_INDEX = 1;
    private static final int WEIGHT_INDEX = 2;
    private static final int HEIGHT_INDEX = 3;

    private final BMICalculator calculator;
    private List<BmiRecord> records;
    private Map<Integer, BmiStatistics> statisticsByAgeGroup;

    public SHealth() {
        this(new BMICalculator());
    }

    public SHealth(BMICalculator calculator) {
        this.calculator = calculator;
        this.records = new ArrayList<>();
    }

    public int calculateBmi(String filename) {
        records = loadRecords(filename);
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

    public List<BmiRecord> getNormalRangeUsers() {
        return calculator.findNormalRangeUsers(records);
    }

    public BmiStatistics getOverallCategoryRatio() {
        return calculator.calculateOverallCategoryRatio(records);
    }

    private List<BmiRecord> loadRecords(String filename) {
        List<BmiRecord> loadedRecords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                List<String> tokens = split(line, CSV_DELIMITER);
                if (tokens.isEmpty()) {
                    break;
                }
                loadedRecords.add(createRecord(tokens));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedRecords;
    }

    private BmiRecord createRecord(List<String> tokens) {
        return new BmiRecord(
                tokens.get(ID_INDEX),
                Integer.parseInt(tokens.get(AGE_INDEX)),
                Double.parseDouble(tokens.get(WEIGHT_INDEX)),
                Double.parseDouble(tokens.get(HEIGHT_INDEX))
        );
    }

    private List<String> split(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        int start = 0;
        int end = line.indexOf(delimiter);
        while (end != -1) {
            tokens.add(line.substring(start, end));
            start = end + 1;
            end = line.indexOf(delimiter, start);
        }
        tokens.add(line.substring(start));
        return tokens;
    }
}