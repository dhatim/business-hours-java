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

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;

class BusinessPeriod {

    private final BusinessTemporal start;
    private final BusinessTemporal end;

    public BusinessPeriod(BusinessTemporal start, BusinessTemporal end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    public boolean alwaysOpen() {
        return end.increment().equals(start);
    }

    public boolean isInPeriod(Temporal temporal) {
        return start.compareTo(temporal) <= 0 && end.compareTo(temporal) >= 0;
    }

    public long timeBeforeOpening(Temporal temporal, ChronoUnit unit) {
        return alwaysOpen() ? Long.MAX_VALUE : start.since(temporal, unit);
    }

    public CronExpression getStartCron() {
        return alwaysOpen() ? null : new CronExpression(start);
    }

    public BusinessTemporal getStart() {
        return start;
    }

    public BusinessTemporal getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.start);
        hash = 59 * hash + Objects.hashCode(this.end);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return Optional
                .ofNullable(obj)
                .filter(BusinessPeriod.class::isInstance)
                .filter(other -> start.equals(((BusinessPeriod) other).start))
                .filter(other -> end.equals(((BusinessPeriod) other).end))
                .isPresent();
    }

    public static Set<BusinessPeriod> merge(Collection<BusinessPeriod> periods) {
        //sort the periods by start date
        List<BusinessPeriod> sortedPeriods = new ArrayList<>(periods);
        Collections.sort(sortedPeriods, Comparator.comparing(BusinessPeriod::getStart));

        Set<BusinessPeriod> mergedPeriods = new HashSet<>(periods.size());
        BusinessPeriod currentPeriod = null;
        for (BusinessPeriod period : sortedPeriods) {
            if (currentPeriod == null) {
                currentPeriod = period;
            } else {
                //check if the start of the period is included in the previous period
                if (currentPeriod.isInPeriod(period.getStart())) {
                    currentPeriod = new BusinessPeriod(
                            currentPeriod.getStart(),
                            BinaryOperator.maxBy(Comparator.<BusinessTemporal>naturalOrder()).apply(currentPeriod.getEnd(), period.getEnd()));
                } else if (currentPeriod.getEnd().increment().equals(period.getStart())) {
                    //check if the two periods are contiguous
                    currentPeriod = new BusinessPeriod(currentPeriod.getStart(), period.getEnd());
                } else {
                    //the periods can not be merged
                    mergedPeriods.add(currentPeriod);
                    currentPeriod = period;
                }
            }
        }

        Optional
                .ofNullable(currentPeriod)
                .ifPresent(mergedPeriods::add);

        return mergedPeriods;
    }

}
