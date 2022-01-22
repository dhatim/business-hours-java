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
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A representation of business hours.
 * <p>
 * The business hours are specified as a string which adheres to the format:
 *
 * <pre>sub-period[, sub-period...]</pre>
 *
 * If the period is blank, then the business supposed to be always open.
 * <br>
 * A sub-period is of the form:
 * <pre>  scale {range [range ...]} [scale {range [range ...]}]</pre> Scale must
 * be one of three different scales (or their equivalent codes):
 * <table>
 * <caption>valid period scales</caption>
 * <tr>
 * <th>Scale</th>
 * <th>Scale code</th>
 * <th>Valid range values</th>
 * </tr>
 * <tr>
 * <td>wday</td>
 * <td>wd</td>
 * <td>1 (Monday) to 7 (Sunday) or mo, tu, we, th, fr, sa, su</td>
 * </tr>
 * <tr>
 * <td>hour</td>
 * <td>hr</td>
 * <td>0-23 or 12am 1am-11am 12noon 12pm 1pm-11pm</td>
 * </tr>
 * <tr>
 * <td>minute</td>
 * <td>min</td>
 * <td>0-59</td>
 * </tr>
 * </table>
 *
 * The same scale type may be specified multiple times. Additional scales simply
 * extend the range defined by previous scales of the same type.
 * <br>
 * The range for a given scale must be a valid value in the form of
 * <code>v</code> or <code>v-v</code>.
 * <br>
 * For the range specification <code>v-v</code>, if the second value is larger
 * than the first value, the range wraps around.
 * <br>
 * <code>v</code> isn't a point in time. In the context of the hour scale, 9
 * specifies the time period from 9:00 am to 9:59. This is what most people
 * would call 9-10.
 * <p>
 * Note that whitespaces can be anywhere. Furthermore, when using letters to
 * specify week days, only the first two are significant and the case is not
 * important: <code>Sunday</code> or <code>Sun</code> are valid specifications
 * for <code>su</code>.
 *
 * <h2>Examples:</h2>
 *
 * To specify business hours that go from Monday through Friday, 9am to 5pm:
 *
 * <pre>wd {Mon-Fri} hr {9am-4pm}</pre>
 *
 * To specify business hours that go from Monday through Friday, 9am to 5pm on
 * Monday, Wednesday, and Friday, and 9am to 3pm on Tuesday and Thursday, use a
 * period such as:
 *
 * <pre>wd {Mon Wed Fri} hr {9am-4pm}, wd{Tue Thu} hr {9am-2pm}</pre>
 *
 * To specify business hours open every other half-hour, use something like:
 * <pre>minute { 0-29 }</pre>
 *
 * To specify the morning, use:
 *
 * <pre>hour { 12am-11am }</pre>
 *
 * Remember, 11am is not 11:00am, but rather 11:00am - 11:59am.
 *
 * @author Maxime Suret
 */
public class BusinessHours {

    private final String stringValue;
    private final Set<BusinessPeriod> periods;

    /**
     * Build a new instance of BusinessHours from its string representation.
     *
     * @param stringValue the string representation of the business hours. See
     * the class level Javadoc for more info on valid formats.
     */
    public BusinessHours(String stringValue) {
        this.stringValue = stringValue;
        this.periods = new HashSet<>(BusinessHoursParser.parse(stringValue));
    }

    /**
     * Tells if the business is open at the given time.
     *
     * @param temporal the time when we want to know if the business is open or
     * closed.
     * @return true if the business is open at the given time, false otherwise
     */
    public boolean isOpen(Temporal temporal) {
        return periods
                .stream()
                .anyMatch(period -> period.isInPeriod(temporal));
    }

    /**
     * Get the time between the given temporal and the next business opening.
     *
     * @param temporal the temporal from which to compute the time before next
     * opening
     * @param unit the unit in which the result must be stated
     * @return the duration,, or LONG.MAX_VALUE if the business is always open
     */
    public long timeBeforeOpening(Temporal temporal, ChronoUnit unit) {
        return periods
                .stream()
                .mapToLong(period -> period.timeBeforeOpening(temporal, unit))
                .min()
                .getAsLong();
    }

    /**
     * Get a set of crons corresponding to each opening. The set is empty if the
     * business is always open.
     *
     * @return the cron set
     */
    public Set<String> getOpeningCrons() {
        //get the start crons of all periods and merge them
        return CronExpression
                .merge(
                        periods
                        .stream()
                        .map(BusinessPeriod::getStartCron)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .map(CronExpression::toString)
                .collect(Collectors.toSet());
    }

    /**
     * Get a set of crons corresponding to each closing. The set is empty if the
     * business is always open.
     *
     * @return the cron set
     */
    public Set<String> getClosingCrons() {
        //get the end crons of all periods and merge them
        return CronExpression
                .merge(
                        periods
                        .stream()
                        .map(BusinessPeriod::getEndCron)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .map(CronExpression::toString)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a hash code for this BusinessHours.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return periods.hashCode();
    }

    /**
     * Tells if these business hours are equals to the given ones.
     * <p>
     * Business hours are equals if they are open at exactly the
     * same instants (regardless of the string representation used to build
     * them).
     *
     * @param obj the other BusinessHours, null returns false
     * @return true if the other BusinessHours is equals to this one
     */
    @Override
    public boolean equals(Object obj) {
        return Optional
                .ofNullable(obj)
                .filter(BusinessHours.class::isInstance)
                .filter(other -> periods.equals(((BusinessHours) other).periods))
                .isPresent();
    }

    /**
     * Return a string representation of this BusinessHours instance.
     * <p>
     * It is the same String as the one used to build this instance.
     *
     * @return the string representation of this BusinessHours instance.
     */
    @Override
    public String toString() {
        return stringValue;
    }

}
