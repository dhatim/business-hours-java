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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CronExpressionTest {

    @Test
    public void cronFromTemporal() {
        assertEquals(
                new CronExpression(BusinessTemporal.of(Collections.singletonMap(ChronoField.HOUR_OF_DAY, 5))).toString(),
                "0 5 * * *");
        assertEquals(new CronExpression(LocalDate.of(2015, 1, 12)).toString(), "0 0 12 1 1");
        assertEquals(new CronExpression(LocalTime.of(18, 2)).toString(), "2 18 * * *");
        assertEquals(new CronExpression(LocalDateTime.of(2015, 1, 12, 18, 2)).toString(), "2 18 12 1 1");
    }

    @Test
    public void mergeCrons() {
        CronExpression cron1 = new CronExpression(LocalTime.of(18, 02));
        CronExpression cron2 = new CronExpression(LocalTime.of(18, 03));
        CronExpression cron3 = new CronExpression(LocalTime.of(18, 04));
        CronExpression cron4 = new CronExpression(LocalTime.of(18, 06));
        CronExpression cron5 = new CronExpression(LocalTime.of(19, 03));
        CronExpression cron6 = new CronExpression(BusinessTemporal.of(Collections.singletonMap(ChronoField.MINUTE_OF_HOUR, 2)));

        Set<CronExpression> merged = CronExpression.merge(Arrays.asList(cron1, cron2));
        assertEquals(merged.size(), 1);
        assertEquals(merged.iterator().next().toString(), "2-3 18 * * *");

        merged = CronExpression.merge(Arrays.asList(cron1, cron2, cron3));
        assertEquals(merged.size(), 1);
        assertEquals(merged.iterator().next().toString(), "2-4 18 * * *");

        merged = CronExpression.merge(Arrays.asList(cron1, cron2, cron3, cron4));
        assertEquals(merged.size(), 1);
        assertEquals(merged.iterator().next().toString(), "2-4,6 18 * * *");

        assertEquals(CronExpression.merge(Arrays.asList(cron1, cron5)), new HashSet<>(Arrays.asList(cron1, cron5)));

        assertEquals(CronExpression.merge(Arrays.asList(cron1, cron6)), new HashSet<>(Arrays.asList(cron6)));
    }

}
