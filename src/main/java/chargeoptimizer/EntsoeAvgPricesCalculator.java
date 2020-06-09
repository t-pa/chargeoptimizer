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
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.DoubleSummaryStatistics;

/**
 * Calculate average ENTSOE day-ahead prices for the EntsoeAvgPrices class.
 */
public class EntsoeAvgPricesCalculator {

    static final Period AVGEXTENT = Period.of(1, 0, 0);
    static final int HOURS = 24;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please supply security token as the command line parameter.");
            System.exit(1);
        }
        
        boolean firstRow = true;
        for (EICCodes.AreaInfo areaInfo : EICCodes.areas.values()) {
            ZoneId timezone = ZoneId.of(areaInfo.timezone);
            EntsoeDayAhead prices = new EntsoeDayAhead(areaInfo.eic, timezone, args[0]);
            
            // get price data for the previous AVGEXTENT time until and including today
            ZonedDateTime start = Instant.now().atZone(timezone).truncatedTo(ChronoUnit.DAYS)
                    .minus(AVGEXTENT).plusDays(1);
            ZonedDateTime end = start.plus(AVGEXTENT);
            prices.fetchCosts(start.withZoneSameInstant(TimeUtils.UTC).toLocalDateTime(),
                    end.withZoneSameInstant(TimeUtils.UTC).toLocalDateTime());
            
            // calculate average price per hour
            DoubleSummaryStatistics[] hourlyStatistics = new DoubleSummaryStatistics[HOURS];
            for (int hour = 0; hour < HOURS; hour++)
                hourlyStatistics[hour] = new DoubleSummaryStatistics();
            
            prices.getPrices().forEach((time, price) -> {
                int hour = time.atZone(TimeUtils.UTC).withZoneSameInstant(timezone).getHour();
                hourlyStatistics[hour].accept(price);                
            });
            
            // print for use in EntsoeAvgPrices
            if (firstRow) {
                System.out.println("    // average hourly prices from " +
                        start.toLocalDate().format(DateTimeFormatter.ISO_DATE) + " to " + 
                        end.minusDays(1).toLocalDate().format(DateTimeFormatter.ISO_DATE));
                System.out.println("    static final HashMap<String, double[]> areaPrices = new HashMap<>();");
                System.out.println("    static {");
                firstRow = false;
            }
            
            StringBuilder s = new StringBuilder("        areaPrices.put(\""+ areaInfo.eic +
                    "\", new double[]{");
            for (int hour = 0; hour < HOURS; hour++) {
                s.append(String.format("%.1f", hourlyStatistics[hour].getAverage()));
                if (hour < HOURS - 1)
                    s.append(", ");
            }
            s.append("});");
            System.out.println(s);
        }
        
        System.out.println("    }");
    }
    
}
