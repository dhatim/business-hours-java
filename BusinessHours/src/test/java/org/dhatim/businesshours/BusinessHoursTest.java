package org.dhatim.businesshours;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BusinessHoursTest {

    @Test
    public void parseAnyPeriod() {
        BusinessHours bh = new BusinessHours("");
        Temporal temporal = LocalDateTime.now();
        for (int i = 0; i < 86400; i++) {
            temporal.plus(1, ChronoUnit.SECONDS);
            assertEquals(bh.isOpen(temporal), true);
            assertEquals(bh.timeBeforeOpening(temporal, ChronoUnit.NANOS), Long.MAX_VALUE);
        }
    }

    @Test
    public void hourBoundaries() {
        BusinessHours bh = new BusinessHours("hr {09-5pm}");
        Temporal t = LocalDateTime.parse("2014-04-22T08:59:59");
        assertEquals(bh.isOpen(t), false);
        t = LocalDateTime.parse("2014-04-22T09:00:00");
        assertEquals(bh.isOpen(t), true);
        t = LocalDateTime.parse("2014-04-22T17:59:59");
        assertEquals(bh.isOpen(t), true);
        t = LocalDateTime.parse("2014-04-22T18:00:00");
        assertEquals(bh.isOpen(t), false);
    }

    @Test
    public void mergeHours() {
        assertEquals(BusinessHoursParser.parse("hr {09-17}, hr {11am-12am}"), BusinessHoursParser.parse("hr {9-0}"));
    }

    @Test
    public void timeBeforeOpening() {
        BusinessHours bh = new BusinessHours("wday {tu-thu} hr {08-5pm}");
        assertEquals(bh.timeBeforeOpening(LocalDateTime.parse("2014-04-22T07:08:09"), ChronoUnit.SECONDS), 51 * 60 + 51);
        assertEquals(bh.timeBeforeOpening(LocalDateTime.parse("2014-04-21T07:08:09"), ChronoUnit.SECONDS), 86400 + 51 * 60 + 51);
        assertEquals(bh.timeBeforeOpening(LocalDateTime.parse("2014-04-22T18:00:00"), ChronoUnit.SECONDS), 14 * 3600);

        bh = new BusinessHours("wday {we-mon} hr {21-03}");
        assertEquals(bh.timeBeforeOpening(LocalDateTime.parse("2014-12-23T23:59:00"), ChronoUnit.MINUTES), 1L);
    }

    @Test
    public void numericWeekDay() {
        assertEquals(new BusinessHours("wday {mo-we}"), new BusinessHours("wday {1-3}"));
        assertEquals(new BusinessHours("wday {mo-we}").hashCode(), new BusinessHours("wday {1-3}").hashCode());
    }

    @Test
    public void letterHours() {
        assertEquals(new BusinessHours("hr{12am}"), new BusinessHours("hr{0}"));
        assertEquals(new BusinessHours("hr{1am}"), new BusinessHours("hr{1}"));
        assertEquals(new BusinessHours("hr{12pm}"), new BusinessHours("hr{12}"));
        assertEquals(new BusinessHours("hr{12noon}"), new BusinessHours("hr{12}"));
        assertEquals(new BusinessHours("hr{1pm}"), new BusinessHours("hr{13}"));
    }

    @Test
    public void reverseBoundaries() {
        BusinessHours bh = new BusinessHours("wday {sa-mo} hr {10pm-8am} min{50-10}");
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-25T22:00:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T22:49:59")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T22:50:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T22:10:59")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T22:11:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T23:49:59")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T23:55:12")), true);
    }

    @Test
    public void BusinessHourstoString() {
        assertEquals(new BusinessHours("wday {mon-Fri} hr {9-18}").toString(), "wday {mon-Fri} hr {9-18}");
    }

    @Test
    public void openingCrons() {
        assertEquals(new BusinessHours("wday{Mon-Fri} hr{9-18}").getOpeningCrons(), Collections.singleton("0 9 * * 1-5"));
        assertEquals(new BusinessHours("wday{Sa} hr{12-23}, wday{Su}").getOpeningCrons(), Collections.singleton("0 12 * * 6"));
        //every Wednesday and Thursday, from 20h30 to 3am
        //It opens on Wednesday at midnight, and on Wednesdays and Thursday at 20h30
        assertEquals(
                new BusinessHours("wday{We-Th} hr{21-3}, wday{We-Th} hr{20} min{30-59}").getOpeningCrons(),
                new HashSet<String>() {
                    {
                        add("0 0 * * 3");
                        add("30 20 * * 3-4");
                    }
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWeekDay() throws ParseException {
        BusinessHoursParser.parse("wday {su-wtf}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHourRange() throws ParseException {
        BusinessHoursParser.parse("hr {10pm-8am-12}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHourFormat() throws ParseException {
        BusinessHoursParser.parse("hr {9am-8wtf}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMinuteRange() throws ParseException {
        BusinessHoursParser.parse("min {10-11-12}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMinuteFormat() throws ParseException {
        BusinessHoursParser.parse("minute {5-10.1}");
    }

    @Test(expected = NullPointerException.class)
    public void parseErrorWhenNullPeriod() throws ParseException {
        BusinessHoursParser.parse(null);
    }

}
