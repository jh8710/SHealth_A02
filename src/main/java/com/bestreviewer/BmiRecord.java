package com.bestreviewer;

public class BmiRecord {
    private final String id;
    private final int age;
    private final double weight;
    private final double height;

    public BmiRecord(int age, double weight, double height) {
        this("", age, weight, height);
    }

    public BmiRecord(String id, int age, double weight, double height) {
        this.id = id;
        this.age = age;
        this.weight = weight;
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeight() {
        return height;
    }

    public BmiRecord withWeightAndHeight(double weight, double height) {
        return new BmiRecord(id, age, weight, height);
    }
}
