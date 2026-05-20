package com.bestreviewer;

import java.util.EnumMap;
import java.util.Map;

public class BmiStatistics {
    private final int totalCount;
    private final Map<BmiCategory, Integer> categoryCounts;

    public BmiStatistics(int totalCount, Map<BmiCategory, Integer> categoryCounts) {
        this.totalCount = totalCount;
        this.categoryCounts = new EnumMap<>(BmiCategory.class);
        this.categoryCounts.putAll(categoryCounts);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCount(BmiCategory category) {
        Integer count = categoryCounts.get(category);
        return count == null ? 0 : count;
    }

    public double getRatio(BmiCategory category) {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) getCount(category) * 100 / totalCount;
    }

    public double getUnderweightRatio() {
        return getRatio(BmiCategory.UNDERWEIGHT);
    }

    public double getNormalRatio() {
        return getRatio(BmiCategory.NORMAL);
    }

    public double getOverweightRatio() {
        return getRatio(BmiCategory.OVERWEIGHT);
    }

    public double getObesityRatio() {
        return getRatio(BmiCategory.OBESITY);
    }

    public Map<BmiCategory, Double> toRatioMap() {
        Map<BmiCategory, Double> ratios = new EnumMap<>(BmiCategory.class);
        for (BmiCategory category : BmiCategory.values()) {
            ratios.put(category, getRatio(category));
        }
        return ratios;
    }
}
