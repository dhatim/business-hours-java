/**
 * Copyright 2015 Dhatim
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dhatim.businesshours;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BusinessTemporalTest {

    @Test(expected = DateTimeException.class)
    public void fromNonContiguousFields() {
        BusinessTemporal.of(new HashMap<ChronoField, Integer>() {
            {
                put(ChronoField.MILLI_OF_SECOND, 0);
                put(ChronoField.MINUTE_OF_HOUR, 0);
            }
        });
    }

    @Test(expected = DateTimeException.class)
    public void fromVariableLengthFields() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_MONTH, 1));
    }

    @Test
    public void temporalQueries() {
        assertEquals(
                BusinessTemporal.of(toMap(ChronoField.HOUR_OF_DAY, 0, ChronoField.MINUTE_OF_HOUR, 0))
                .query(TemporalQueries.precision()),
                ChronoUnit.MINUTES);
        assertEquals(
                BusinessTemporal.of(toMap(ChronoField.HOUR_OF_DAY, 0, ChronoField.MINUTE_OF_HOUR, 0, ChronoField.SECOND_OF_MINUTE, 0))
                .query(TemporalQueries.precision()),
                ChronoUnit.SECONDS);
        assertNull(
                BusinessTemporal.of(Collections.singletonMap(ChronoField.HOUR_OF_DAY, 0))
                .query(TemporalQueries.zoneId()));
    }

    @Test
    public void supportedFields() {
        BusinessTemporal bt = BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1));
        assertEquals(bt.isSupported(ChronoField.DAY_OF_WEEK), true);
        assertEquals(bt.isSupported(ChronoField.HOUR_OF_DAY), false);
        assertEquals(bt.isSupported(WeekFields.SUNDAY_START.dayOfWeek()), true);
        assertEquals(bt.isSupported(IsoFields.QUARTER_OF_YEAR), false);
    }

    @Test
    public void getSupportedField() {
        BusinessTemporal bt = BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1));
        assertEquals(bt.getLong(ChronoField.DAY_OF_WEEK), 1);
        assertEquals(bt.getLong(WeekFields.SUNDAY_START.dayOfWeek()), 2);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void getUnsupportedChronoField() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1)).getLong(ChronoField.HOUR_OF_DAY);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void getUnsupportedField() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1)).getLong(IsoFields.QUARTER_OF_YEAR);
    }

    @Test
    public void withSupportedField() {
        BusinessTemporal bt = BusinessTemporal.of(toMap(ChronoField.DAY_OF_WEEK, 1, ChronoField.HOUR_OF_DAY, 0));
        Temporal temporal = bt.with(ChronoField.DAY_OF_WEEK, 2);
        assertEquals(temporal.getLong(ChronoField.DAY_OF_WEEK), 2);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);

        temporal = bt.with(WeekFields.SUNDAY_START.dayOfWeek(), 4);
        assertEquals(temporal.getLong(ChronoField.DAY_OF_WEEK), 3);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);
    }

    @Test(expected = DateTimeException.class)
    public void withInvalidFieldValue() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.SECOND_OF_MINUTE, 0)).with(ChronoField.SECOND_OF_MINUTE, 61);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void withUnsupportedChronoField() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1)).with(ChronoField.HOUR_OF_DAY, 0);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void withUnsupportedField() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1)).with(IsoFields.QUARTER_OF_YEAR, 0);
    }

    @Test
    public void supportedUnits() {
        BusinessTemporal bt = BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1));
        assertEquals(bt.isSupported(ChronoUnit.DAYS), true);
        assertEquals(bt.isSupported(ChronoUnit.WEEKS), false);
        assertEquals(bt.isSupported(IsoFields.QUARTER_YEARS), false);
    }

    @Test
    public void plusSupportedUnit() {
        BusinessTemporal bt = BusinessTemporal.of(toMap(ChronoField.HOUR_OF_DAY, 0, ChronoField.MINUTE_OF_HOUR, 0));
        Temporal temporal = bt.plus(1, ChronoUnit.MINUTES);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 1);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);

        temporal = bt.plus(60, ChronoUnit.MINUTES);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 1);

        temporal = bt.plus(1440, ChronoUnit.MINUTES);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);

        temporal = bt.plus(1, ChronoUnit.HOURS);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 1);

        temporal = bt.plus(24, ChronoUnit.HOURS);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);

        temporal = bt.minus(1, ChronoUnit.MINUTES);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 59);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 23);

        temporal = bt.minus(60, ChronoUnit.MINUTES);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 23);

        temporal = bt.minus(1440, ChronoUnit.MINUTES);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);

        temporal = bt.minus(1, ChronoUnit.HOURS);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 23);

        temporal = bt.minus(24, ChronoUnit.HOURS);
        assertEquals(temporal.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(temporal.getLong(ChronoField.HOUR_OF_DAY), 0);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void plusUnsupportedChronoUnit() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 0)).plus(1, ChronoUnit.HOURS);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void plusUnsupportedUnit() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 0)).plus(1, IsoFields.QUARTER_YEARS);
    }

    @Test
    public void increment() {
        BusinessTemporal bt = BusinessTemporal.of(toMap(ChronoField.HOUR_OF_DAY, 0, ChronoField.MINUTE_OF_HOUR, 0)).increment();
        assertEquals(bt.getLong(ChronoField.MINUTE_OF_HOUR), 1);
        assertEquals(bt.getLong(ChronoField.HOUR_OF_DAY), 0);

        bt = BusinessTemporal.of(toMap(ChronoField.HOUR_OF_DAY, 0, ChronoField.MINUTE_OF_HOUR, 59)).increment();
        assertEquals(bt.getLong(ChronoField.MINUTE_OF_HOUR), 0);
        assertEquals(bt.getLong(ChronoField.HOUR_OF_DAY), 1);
    }

    @Test
    public void untilChronoUnit() {
        BusinessTemporal bt = BusinessTemporal.of(toMap(ChronoField.HOUR_OF_DAY, 11, ChronoField.MINUTE_OF_HOUR, 30));
        assertEquals(bt.until(LocalTime.of(13, 29), ChronoUnit.MINUTES), 119);
        assertEquals(bt.until(LocalTime.of(13, 29), ChronoUnit.HOURS), 1);
        assertEquals(bt.until(LocalTime.of(13, 29), ChronoUnit.DAYS), 0);
        assertEquals(bt.until(LocalTime.of(13, 29), IsoFields.QUARTER_YEARS), 0);
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void sinceInvalidTemporal() {
        BusinessTemporal.of(Collections.singletonMap(ChronoField.DAY_OF_WEEK, 1)).until(LocalTime.of(0, 0), ChronoUnit.DAYS);
    }

    @Test
    public void since() {
        assertEquals(BusinessTemporal
                .of(toMap(ChronoField.HOUR_OF_DAY, 13, ChronoField.MINUTE_OF_HOUR, 29))
                .since(LocalTime.of(11, 30, 1), ChronoUnit.HOURS), 1);

        assertEquals(BusinessTemporal
                .of(toMap(ChronoField.HOUR_OF_DAY, 13, ChronoField.MINUTE_OF_HOUR, 29))
                .since(LocalTime.of(11, 30, 1), ChronoUnit.MINUTES), 118);

        assertEquals(BusinessTemporal
                .of(toMap(ChronoField.HOUR_OF_DAY, 13, ChronoField.MINUTE_OF_HOUR, 29))
                .since(LocalTime.of(11, 30, 1), ChronoUnit.SECONDS), 7139);

        assertEquals(BusinessTemporal
                .of(toMap(ChronoField.HOUR_OF_DAY, 13, ChronoField.MINUTE_OF_HOUR, 29))
                .since(LocalTime.of(11, 30, 1), ChronoUnit.MILLIS), 7139000);

        assertEquals(BusinessTemporal
                .of(toMap(ChronoField.HOUR_OF_DAY, 11, ChronoField.MINUTE_OF_HOUR, 30))
                .since(LocalTime.of(13, 29, 1), ChronoUnit.MILLIS), 79259000);
    }

    @Test
    public void compare() {
        assertEquals(BusinessTemporal.of(Collections.singletonMap(ChronoField.HOUR_OF_DAY, 1)).compareTo(LocalTime.of(1, 0)), 0);
        assertTrue(BusinessTemporal.of(Collections.singletonMap(ChronoField.HOUR_OF_DAY, 2)).compareTo(LocalTime.of(1, 0)) > 0);
        assertTrue(BusinessTemporal.of(Collections.singletonMap(ChronoField.HOUR_OF_DAY, 0)).compareTo(LocalTime.of(1, 0)) < 0);
    }

    private static Map<ChronoField, Integer> toMap(
            ChronoField field1, Integer value1,
            ChronoField field2, Integer value2) {
        return new HashMap<ChronoField, Integer>() {
            {
                put(field1, value1);
                put(field2, value2);
            }
        };
    }

    private static Map<ChronoField, Integer> toMap(
            ChronoField field1, Integer value1,
            ChronoField field2, Integer value2,
            ChronoField field3, Integer value3) {
        return new HashMap<ChronoField, Integer>() {
            {
                put(field1, value1);
                put(field2, value2);
                put(field3, value3);
            }
        };
    }

}
