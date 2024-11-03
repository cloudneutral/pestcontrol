package io.cockroachdb.pestcontrol.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import io.cockroachdb.pestcontrol.util.PatternUtils;

@Tags(value = {
        @Tag("unit-test")
})
public class PatternUtilsTest {
    @Test
    public void whenMatchingLocalityString_expectKeyValueTuples() {
        final String regex = "([^=,]+)=([^\0]+?)(?=,[^,]+=|$)";
        final String string = "region=abc,zone=ooo,dns=sd";
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            System.out.println("Full match: " + matcher.group(0));
            for (int i = 1; i <= matcher.groupCount(); i++) {
                System.out.println("Group " + i + ": " + matcher.group(i));
            }
        }
    }

    @Test
    public void whenParsingDateTime_expectSuccess() {
        String updatedAt = "2024-09-17 19:15:09.958892 +0100 UTC";
        LocalDateTime dateTime = LocalDateTime.parse(updatedAt,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS Z z"));
        Assertions.assertEquals(2024, dateTime.getYear());
        Assertions.assertEquals(9, dateTime.getMonthValue());
        Assertions.assertEquals(17, dateTime.getDayOfMonth());
        Assertions.assertEquals(19, dateTime.getHour());
        Assertions.assertEquals(15, dateTime.getMinute());
        Assertions.assertEquals(9, dateTime.getSecond());
        Assertions.assertEquals(958892000, dateTime.getNano());
    }

    @Test
    public void whenParsingDateTime_expectSuccess2() {
        String updatedAt = "2024-09-17 19:15:09.958892 +0000 UTC";
        ZonedDateTime dt = LocalDateTime.parse(updatedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS Z z"))
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime dt2 = ZonedDateTime.now()
                .withZoneSameInstant(ZoneOffset.UTC);
        System.out.println(dt);
        System.out.println(dt2);
    }

    @Test
    public void whenMatchingLocality_expectKeyValueTuples() {
        Map<String, String> map = PatternUtils.parseLocality("region=abc,zone=def,dns=gh");
        Assertions.assertEquals(3, map.size());

        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();

        Map.Entry<String, String> e1 = it.next();
        Assertions.assertEquals("region", e1.getKey());
        Assertions.assertEquals("abc", e1.getValue());

        Map.Entry<String, String> e2 = it.next();
        Assertions.assertEquals("zone", e2.getKey());
        Assertions.assertEquals("def", e2.getValue());

        Map.Entry<String, String> e3 = it.next();
        Assertions.assertEquals("dns", e3.getKey());
        Assertions.assertEquals("gh", e3.getValue());
    }
}
