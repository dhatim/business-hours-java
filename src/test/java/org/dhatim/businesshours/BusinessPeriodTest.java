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

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Maxime
 */
public class BusinessPeriodTest {

    @Test
    public void alwaysOpen() {
        assertTrue(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 0)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 59)))
                .alwaysOpen());
        assertFalse(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 1)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 59)))
                .alwaysOpen());
    }

    @Test
    public void isInPeriod() {

        assertTrue(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .isInPeriod(LocalTime.of(0, 10, 00)));

        assertTrue(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .isInPeriod(LocalTime.of(0, 15, 59)));

        assertFalse(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .isInPeriod(LocalTime.of(0, 16, 0)));

        assertFalse(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .isInPeriod(LocalTime.of(0, 9, 59)));
    }

    @Test
    public void timeBeforeOpening() {
        assertEquals(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .timeBeforeOpening(LocalTime.of(0, 9, 1), ChronoUnit.SECONDS), 59);

        assertEquals(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .timeBeforeOpening(LocalTime.of(0, 10, 0), ChronoUnit.SECONDS), 0);

        assertEquals(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .timeBeforeOpening(LocalTime.of(0, 10, 1), ChronoUnit.SECONDS), 3599);

        assertEquals(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 0)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 59)))
                .timeBeforeOpening(LocalTime.of(0, 0), ChronoUnit.MINUTES), Long.MAX_VALUE);
    }

    @Test
    public void getStartCron() {
        assertEquals(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .getStartCron().toString(), "10 * * * *");

        assertNull(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 0)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 59)))
                .getStartCron());
    }

    @Test
    public void getEndCron() {
        assertEquals(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15)))
                .getEndCron().toString(), "16 * * * *");

        assertNull(
                new BusinessPeriod(
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 0)),
                        BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 59)))
                .getEndCron());
    }

    @Test
    public void mergePeriods() {
        BusinessTemporal start1 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 10));
        BusinessTemporal end1 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 15));

        //the start of this period is included in the previous period
        BusinessTemporal start2 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 12));
        BusinessTemporal end2 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 20));

        //the start of this period is contiguous to the end of the previous period
        BusinessTemporal start3 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 21));
        BusinessTemporal end3 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 22));

        //this period is disjoint from the others
        BusinessTemporal start4 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 24));
        BusinessTemporal end4 = BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 27));

        assertEquals(
                BusinessPeriod.merge(
                        Arrays.asList(
                                new BusinessPeriod(start1, end1),
                                new BusinessPeriod(start2, end2),
                                new BusinessPeriod(start3, end3),
                                new BusinessPeriod(start4, end4))),
                new HashSet<BusinessPeriod>() {
                    {
                        add(new BusinessPeriod(start1, end3));
                        add(new BusinessPeriod(start4, end4));
                    }
                }
        );
    }

}
