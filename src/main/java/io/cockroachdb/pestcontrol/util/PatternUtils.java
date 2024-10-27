package io.cockroachdb.pestcontrol.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PatternUtils {
    private static final Pattern LOCALITY_PATTERN
            = Pattern.compile("([^=,]+)=([^\0]+?)(?=,[^,]+=|$)", Pattern.CASE_INSENSITIVE);

    private PatternUtils() {
    }

    public static Map<String, String> parseLocality(String locality) {
        final Matcher matcher = LOCALITY_PATTERN.matcher(locality);

        Map<String, String> map = new LinkedHashMap<>();

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                map.put(matcher.group(1), matcher.group(2));
            }
        }

        return map;
    }

    private static Map<String, String> parseTuples(Path path) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        Pattern tuples = Pattern.compile("(\\S+?)\\s*=\\s*\"([^\\t]+)\"\\s*");

        if (Files.exists(path)) {
            Files.readAllLines(path).forEach(line -> {
                Matcher m = tuples.matcher(line);
                if (m.matches()) {
                    map.put(m.group(1), m.group(2));
                }
            });
        }

        return Collections.unmodifiableMap(map);
    }
}
