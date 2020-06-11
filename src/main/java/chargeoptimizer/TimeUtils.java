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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Frequently used time-related functions.
 */
public class TimeUtils {
    public static final ZoneId UTC = ZoneId.of("UTC");

    public static LocalDateTime now() {
        return Instant.now().atZone(UTC).toLocalDateTime();
    }
    
    private TimeUtils() {
    }
}
