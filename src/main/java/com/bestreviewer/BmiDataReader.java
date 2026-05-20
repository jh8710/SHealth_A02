package com.bestreviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class BmiDataReader {
    private static final char CSV_DELIMITER = ',';
    private static final int ID_INDEX = 0;
    private static final int AGE_INDEX = 1;
    private static final int WEIGHT_INDEX = 2;
    private static final int HEIGHT_INDEX = 3;

    List<BmiRecord> read(String filename) {
        List<BmiRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                records.add(createRecord(split(line, CSV_DELIMITER)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read BMI data file: " + filename, e);
        }
        return records;
    }

    private BmiRecord createRecord(List<String> tokens) {
        if (tokens.size() != 4) {
            throw new IllegalArgumentException("Invalid BMI data line.");
        }
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
            tokens.add(line.substring(start, end).trim());
            start = end + 1;
            end = line.indexOf(delimiter, start);
        }
        tokens.add(line.substring(start).trim());
        return tokens;
    }
}
