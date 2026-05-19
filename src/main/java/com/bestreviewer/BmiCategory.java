package com.bestreviewer;

public enum BmiCategory {
    UNDERWEIGHT(100),
    NORMAL(200),
    OVERWEIGHT(300),
    OBESITY(400);

    private final int code;

    BmiCategory(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BmiCategory fromCode(int code) {
        for (BmiCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown BMI category code: " + code);
    }
}
