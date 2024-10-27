package io.cockroachdb.pestcontrol.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

@Tags(value = {
        @Tag("unit-test")
})
public class LogicalTimestampTest {
    @Test
    public void whenParsingHLC_thenSucceed() {
        LogicalTimestamp ts = LogicalTimestamp.parse("1546856630992375686.0000000000");
        Assertions.assertEquals(1546856630992375686L, ts.getPhysicalWallClockTimeNanos());
        Assertions.assertEquals(0, ts.getLogicalCounter());
    }

    @Test
    public void whenParsingHLC_thenSucceedAgain() {
        LogicalTimestamp ts = LogicalTimestamp.parse("1546856630992375686.0000000001");
        Assertions.assertEquals(1546856630992375686L, ts.getPhysicalWallClockTimeNanos());
        Assertions.assertEquals(1, ts.getLogicalCounter());
    }

    @Test
    public void whenParsingHLC_thenReturnLocalDateTime() {
        LogicalTimestamp ts = LogicalTimestamp.parse("1651906789519145742.0000000001");
        Assertions.assertEquals(1651906789519145742L, ts.getPhysicalWallClockTimeNanos());
        Assertions.assertEquals(1, ts.getLogicalCounter());
        Assertions.assertEquals(Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(1651906789519145742L)),
                ts.toInstant());
        Assertions.assertEquals(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(1651906789519145742L)),
                ZoneId.systemDefault()), ts.toLocalDateTime());

        System.out.println(ts.toLocalDateTime(ZoneId.of("UTC+0")));
    }

    @Test
    public void whenParsingBadFormat_thenFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            LogicalTimestamp.parse("15468566309923756860000000000");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            LogicalTimestamp.parse("15468566309923756860000000000");
        });
    }

    @Test
    public void whenComparingTimestamps_thenAssertOrder() {
        LogicalTimestamp high = LogicalTimestamp.parse("1546856630992375686.000000001");
        LogicalTimestamp higher = LogicalTimestamp.parse("1546856630992375686.000000001");
        LogicalTimestamp low = LogicalTimestamp.parse("1546856630992375686.0000000000");

        Assertions.assertTrue(high.compareTo(low) > 0);
        Assertions.assertFalse(high.compareTo(low) < 0);
        Assertions.assertEquals(0, high.compareTo(higher));
        Assertions.assertEquals(high, higher);
        Assertions.assertNotEquals(high, low);
    }
}
