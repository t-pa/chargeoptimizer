/*
 * Copyright (C) 2020 t-pa <t-pa@posteo.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package chargeoptimizer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Frequently used time-related functions.
 */
public class TimeUtils {
    /**
     * UTC time zone constant
     */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * Get current UTC time.
     * @return the time
     */
    public static LocalDateTime now() {
        return Instant.now().atZone(UTC).toLocalDateTime();
    }
    
    /**
     * Round time to a given granularity; the time is always rounded towards the start of the day
     * and the rounded time is an integer multiple of granularity after the start of the day.
     * @param time
     * @param granularity
     * @return the rounded time
     */
    public static LocalDateTime roundTimeTo(LocalDateTime time, Duration granularity) {
        LocalDateTime startOfDay = time.truncatedTo(ChronoUnit.DAYS);
        long l = Duration.between(startOfDay, time).dividedBy(granularity);
        return startOfDay.plus(granularity.multipliedBy(l));
    }
    
    private TimeUtils() {
    }
}
