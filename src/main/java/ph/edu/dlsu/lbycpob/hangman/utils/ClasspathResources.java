package ph.edu.dlsu.lbycpob.hangman.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ClasspathResources {

    private ClasspathResources() {
    }

    public static List<String> readLines(String resourcePath) throws IOException {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");

        try (InputStream input = ClasspathResources.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Resource not found on the classpath: " + resourcePath);
            }
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines;
        }
    }
}