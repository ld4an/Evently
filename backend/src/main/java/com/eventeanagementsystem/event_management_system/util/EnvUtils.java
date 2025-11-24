package com.eventeanagementsystem.event_management_system.util;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Small helper to fetch configuration values from environment variables or a local .env.
 * It looks in the current directory and in /backend to support running from project root.
 */
public final class EnvUtils {
    private EnvUtils() {}

    private static final Dotenv DOTENV_CURRENT = Dotenv.configure().ignoreIfMissing().load();
    private static final Dotenv DOTENV_BACKEND = Dotenv.configure().directory("backend").ignoreIfMissing().load();

    public static String getFromDotenv(String key) {
        String v = DOTENV_CURRENT.get(key);
        if (v == null || v.isBlank()) {
            v = DOTENV_BACKEND.get(key);
        }
        return v;
    }

    public static String getFromSystemOrDotenv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            v = getFromDotenv(key);
        }
        return v;
    }

    public static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    public static int firstNonEmptyInt(String value, int defaultVal) {
        try {
            return value != null && !value.isBlank() ? Integer.parseInt(value) : defaultVal;
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }
}
