package tn.esprit.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class LocalSecrets {
    private static final String LOCAL_FILE_NAME = "local.secrets.properties";
    private static final String EXAMPLE_FILE_NAME = "local.secrets.properties.example";
    private static volatile String lastResolvedPath = "not-resolved";
    private static final Properties PROPS = loadProperties();

    private LocalSecrets() {
    }

    public static String get(String key) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env.trim();

        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();

        String fileValue = getPropertyNormalized(PROPS, key);
        if (fileValue != null && !fileValue.isBlank()) return fileValue.trim();

        return null;
    }

    public static String debugSource() {
        return "user.dir=" + System.getProperty("user.dir") + ", secretsPath=" + lastResolvedPath;
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        Path file = resolveSecretsFile();
        if (file == null || !Files.exists(file)) {
            lastResolvedPath = "not-found";
            return p;
        }
        lastResolvedPath = file.toAbsolutePath().normalize().toString();

        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (IOException ignored) {
            // Keep empty properties; missing local secrets should not break app startup.
        }
        return p;
    }

    private static Path resolveSecretsFile() {
        Path direct = resolveCandidate(Path.of("").toAbsolutePath().normalize());
        if (direct != null) return direct;

        String userDir = System.getProperty("user.dir");
        if (userDir != null && !userDir.isBlank()) {
            Path current = Paths.get(userDir).toAbsolutePath().normalize();
            for (int i = 0; i < 12 && current != null; i++) {
                Path candidate = resolveCandidate(current);
                if (candidate != null) return candidate;
                current = current.getParent();
            }
        }

        try {
            Path codeSource = Paths.get(LocalSecrets.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .toAbsolutePath()
                    .normalize();
            Path current = Files.isDirectory(codeSource) ? codeSource : codeSource.getParent();
            for (int i = 0; i < 12 && current != null; i++) {
                Path candidate = resolveCandidate(current);
                if (candidate != null) return candidate;
                current = current.getParent();
            }
        } catch (URISyntaxException | NullPointerException ignored) {
        }

        return null;
    }

    private static Path resolveCandidate(Path directory) {
        Path local = directory.resolve(LOCAL_FILE_NAME);
        if (Files.exists(local)) return local;

        Path example = directory.resolve(EXAMPLE_FILE_NAME);
        if (Files.exists(example)) return example;

        return null;
    }

    private static String getPropertyNormalized(Properties props, String key) {
        String direct = props.getProperty(key);
        if (direct != null && !direct.isBlank()) return direct;

        String bom = "\uFEFF" + key;
        String withBom = props.getProperty(bom);
        if (withBom != null && !withBom.isBlank()) return withBom;

        for (String k : props.stringPropertyNames()) {
            String normalized = k == null ? "" : k.replace("\uFEFF", "").trim();
            if (normalized.equals(key)) {
                return props.getProperty(k);
            }
        }
        return null;
    }
}
