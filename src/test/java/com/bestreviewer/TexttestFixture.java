package com.bestreviewer;

import java.util.Locale;

public final class TexttestFixture {
    private static final int[] AGE_GROUPS = {20, 30, 40, 50, 60, 70};

    private TexttestFixture() {
    }

    public static String run(String filename) {
        SHealth sHealth = new SHealth();
        sHealth.calculateBmi(filename);

        StringBuilder output = new StringBuilder();
        for (int ageGroup : AGE_GROUPS) {
            output.append(String.format(Locale.ROOT,
                    "%d - underweight = %.6f, normal = %.6f, overweight = %.6f, obesity = %.6f\n",
                    ageGroup,
                    sHealth.getBmiRatio(ageGroup, 100),
                    sHealth.getBmiRatio(ageGroup, 200),
                    sHealth.getBmiRatio(ageGroup, 300),
                    sHealth.getBmiRatio(ageGroup, 400)));
        }
        return output.toString();
    }
}
