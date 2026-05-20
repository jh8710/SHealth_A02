package com.bestreviewer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TexttestFixtureGoldenMasterTest {
    private static final String UPDATE_GOLDEN_MASTER_PROPERTY = "updateGoldenMaster";
    private static final String INPUT_RESOURCE = "golden-master/shealth_golden_master_input.csv";
    private static final String EXPECTED_RESOURCE = "golden-master/golden_master_expected.txt";
    private static final Path SOURCE_GOLDEN_MASTER_PATH = Paths.get(
            "src", "test", "resources", "golden-master", "golden_master_expected.txt");

    @Test
    @DisplayName("Given 고정 CSV 입력이 있을 때 When TexttestFixture를 실행하면 Then Golden Master 출력과 일치한다")
    void should_match_golden_master_output() throws Exception {
        Path inputPath = resourcePath(INPUT_RESOURCE);
        String actual = TexttestFixture.run(inputPath.toString());

        if (Boolean.getBoolean(UPDATE_GOLDEN_MASTER_PROPERTY)) {
            Files.createDirectories(SOURCE_GOLDEN_MASTER_PATH.getParent());
            Files.writeString(SOURCE_GOLDEN_MASTER_PATH, actual, StandardCharsets.UTF_8);
        }

        Path expectedPath = Boolean.getBoolean(UPDATE_GOLDEN_MASTER_PROPERTY)
                ? SOURCE_GOLDEN_MASTER_PATH
                : resourcePath(EXPECTED_RESOURCE);
        String expected = Files.readString(expectedPath, StandardCharsets.UTF_8);
        assertEquals(normalizeLineEndings(expected), normalizeLineEndings(actual),
                "Golden Master output changed. Review the diff and run 'mvn test -DupdateGoldenMaster=true' only when the new behavior is intentional.");
    }

    private Path resourcePath(String resourceName) throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Test resource was not found: " + resourceName);
        }
        return Paths.get(resource.toURI());
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n")
                .replace("\r", "\n");
    }
}
