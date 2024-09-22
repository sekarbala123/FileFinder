package org.bn;

import java.util.regex.Pattern;

public class WildcardUtils {
    public static boolean isMatch(String wildcard, String filename) {
        // Convert wildcard pattern to regex pattern
        String regex = wildcard
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        return Pattern.matches(regex, filename);
    }
}
