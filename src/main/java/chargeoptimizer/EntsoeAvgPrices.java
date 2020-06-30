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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies average price data; useful when no internet connection is available.
 */
public class EntsoeAvgPrices implements CostSource {
    
    final Logger logger = LoggerFactory.getLogger(EntsoeAvgPrices.class);
    
    private final String areaEIC;
    private final ZoneId timezone;

    // average hourly prices from 2019-06-10 to 2020-06-09
    static final HashMap<String, double[]> areaPrices = new HashMap<>();
    static {
        areaPrices.put("10YAT-APG------L", new double[]{28.2, 26.5, 25.5, 24.4, 24.8, 26.8, 32.6, 37.8, 39.2, 37.3, 35.2, 34.6, 33.1, 31.5, 30.4, 31.1, 32.8, 37.2, 40.9, 43.2, 41.2, 36.9, 34.8, 30.6});
        areaPrices.put("10YCS-SERBIATSOV", new double[]{37.1, 32.4, 29.1, 27.6, 28.0, 31.8, 40.0, 47.2, 49.3, 48.6, 47.0, 46.5, 46.1, 45.1, 45.0, 46.2, 48.8, 52.3, 54.6, 56.6, 55.5, 49.7, 44.8, 39.1});
        areaPrices.put("10YLV-1001A00074", new double[]{26.1, 26.0, 23.7, 22.5, 22.3, 23.2, 28.1, 37.8, 44.0, 47.7, 47.1, 46.1, 44.9, 46.0, 45.3, 43.3, 42.6, 43.6, 44.6, 46.7, 44.2, 39.3, 36.3, 31.0});
        areaPrices.put("10YGB----------A", new double[]{29.3, 27.4, 25.2, 23.7, 23.9, 27.5, 33.5, 35.4, 38.9, 39.8, 37.6, 36.2, 35.6, 33.1, 31.6, 31.8, 39.0, 46.0, 48.3, 45.5, 40.3, 37.1, 31.9, 33.0});
        areaPrices.put("10YES-REE------0", new double[]{37.0, 34.5, 32.6, 31.5, 31.1, 32.0, 34.2, 37.3, 38.9, 39.8, 39.3, 38.8, 38.6, 38.4, 37.2, 35.9, 35.9, 37.4, 39.5, 41.6, 42.5, 42.6, 40.5, 37.7});
        areaPrices.put("10Y1001A1001A44P", new double[]{22.8, 21.9, 21.4, 21.0, 21.4, 23.0, 24.8, 26.4, 27.5, 27.4, 27.2, 26.9, 26.5, 26.2, 26.0, 26.0, 26.1, 26.9, 27.1, 26.8, 26.3, 25.8, 25.0, 23.4});
        areaPrices.put("10Y1001A1001A45N", new double[]{22.8, 21.9, 21.4, 21.0, 21.4, 23.0, 24.8, 26.4, 27.5, 27.4, 27.2, 26.9, 26.5, 26.2, 26.0, 26.0, 26.1, 26.9, 27.1, 26.8, 26.3, 25.8, 25.0, 23.4});
        areaPrices.put("10Y1001A1001A46L", new double[]{22.8, 21.9, 21.4, 21.0, 21.4, 23.2, 26.4, 30.8, 33.1, 31.8, 30.7, 29.7, 28.6, 27.8, 27.4, 27.4, 28.0, 30.1, 31.2, 30.5, 28.7, 27.5, 25.7, 23.6});
        areaPrices.put("10Y1001A1001A47J", new double[]{23.4, 22.4, 21.7, 21.4, 21.9, 23.9, 28.7, 34.5, 36.5, 34.6, 32.8, 31.7, 30.2, 29.3, 28.8, 28.9, 30.1, 33.2, 35.7, 35.5, 33.1, 30.4, 27.2, 24.4});
        areaPrices.put("10YHU-MAVIR----U", new double[]{35.7, 31.4, 28.4, 27.1, 27.8, 31.8, 40.1, 47.9, 49.5, 48.5, 46.5, 46.2, 46.0, 44.3, 44.0, 45.9, 49.8, 53.3, 55.8, 58.3, 57.0, 49.5, 44.3, 37.0});
        areaPrices.put("10YFR-RTE------C", new double[]{28.4, 25.8, 24.0, 21.5, 20.5, 22.7, 27.6, 33.3, 36.4, 36.2, 34.4, 33.5, 32.7, 30.4, 28.5, 28.0, 29.3, 33.8, 38.4, 40.5, 37.3, 33.9, 34.6, 31.7});
        areaPrices.put("10Y1001A1001A71M", new double[]{40.2, 38.2, 35.9, 34.5, 34.1, 36.3, 41.0, 45.0, 46.9, 45.3, 42.3, 40.7, 38.3, 37.0, 38.6, 41.1, 44.5, 48.9, 52.5, 56.4, 55.1, 50.1, 43.9, 38.9});
        areaPrices.put("10YCA-BULGARIA-R", new double[]{72.7, 69.4, 61.8, 55.8, 54.8, 57.7, 67.7, 80.6, 93.9, 94.5, 88.5, 86.2, 82.1, 84.7, 85.2, 88.7, 91.8, 95.4, 103.0, 106.5, 107.3, 104.9, 92.2, 84.6});
        areaPrices.put("10YDK-1--------W", new double[]{24.1, 22.9, 21.8, 21.4, 21.7, 23.3, 28.8, 34.3, 35.9, 33.8, 31.5, 30.2, 28.5, 27.3, 26.8, 27.0, 28.6, 33.2, 36.3, 37.6, 35.3, 32.1, 29.7, 26.4});
        areaPrices.put("10YDK-2--------M", new double[]{25.3, 23.8, 22.9, 22.6, 23.1, 24.8, 30.3, 36.1, 37.9, 35.7, 33.6, 32.5, 30.9, 29.7, 29.1, 29.4, 31.1, 35.3, 38.4, 39.7, 37.3, 33.8, 30.8, 27.3});
        areaPrices.put("10YPT-REN------W", new double[]{34.6, 32.7, 31.6, 31.2, 32.1, 34.2, 37.2, 38.7, 39.7, 39.3, 38.8, 38.6, 38.5, 37.2, 36.1, 36.1, 37.5, 39.5, 41.6, 42.5, 42.6, 40.5, 37.7, 37.0});
        areaPrices.put("10Y1001A1001A70O", new double[]{38.4, 35.8, 33.7, 32.7, 32.6, 34.7, 39.9, 44.0, 46.4, 45.5, 42.9, 41.6, 38.8, 37.8, 39.5, 41.6, 44.5, 47.9, 51.0, 54.4, 52.5, 48.4, 43.1, 38.5});
        areaPrices.put("10YPL-AREA-----S", new double[]{113.3, 110.6, 109.4, 108.7, 109.3, 111.0, 125.2, 131.4, 141.0, 144.7, 143.4, 145.0, 145.7, 145.1, 140.0, 138.7, 138.2, 138.3, 139.4, 141.7, 138.3, 129.4, 126.6, 117.5});
        areaPrices.put("10YNO-1--------2", new double[]{23.8, 23.2, 22.9, 22.7, 22.8, 23.4, 24.3, 25.3, 26.1, 26.0, 25.7, 25.5, 25.2, 24.9, 24.8, 24.9, 25.2, 25.7, 25.8, 25.5, 25.2, 24.9, 24.6, 23.9});
        areaPrices.put("10YNO-2--------T", new double[]{23.8, 23.2, 22.9, 22.7, 22.8, 23.4, 24.3, 25.3, 26.1, 25.9, 25.7, 25.4, 25.1, 24.9, 24.8, 24.8, 25.2, 25.7, 25.8, 25.6, 25.2, 24.9, 24.6, 23.9});
        areaPrices.put("10YNO-3--------J", new double[]{22.9, 22.2, 21.8, 21.6, 21.8, 23.0, 24.5, 25.8, 26.6, 26.6, 26.5, 26.3, 26.1, 25.8, 25.6, 25.6, 25.8, 26.3, 26.4, 26.2, 25.8, 25.4, 24.6, 23.4});
        areaPrices.put("10YNO-4--------9", new double[]{22.9, 22.3, 21.9, 21.6, 21.9, 23.0, 24.4, 25.5, 26.2, 26.4, 26.3, 26.1, 25.8, 25.6, 25.5, 25.5, 25.7, 26.2, 26.3, 26.1, 25.7, 25.3, 24.6, 23.5});
        areaPrices.put("10YFI-1--------U", new double[]{24.8, 23.8, 22.6, 21.9, 21.6, 22.3, 27.5, 36.4, 41.6, 44.0, 42.9, 42.2, 40.9, 40.5, 39.8, 37.9, 37.7, 39.2, 40.6, 43.1, 40.4, 33.7, 32.2, 28.9});
        areaPrices.put("10Y1001A1001A48H", new double[]{23.8, 23.3, 23.0, 22.7, 22.8, 23.4, 24.3, 25.3, 26.1, 26.0, 25.7, 25.5, 25.2, 24.9, 24.8, 24.9, 25.2, 25.7, 25.8, 25.5, 25.2, 24.9, 24.6, 23.9});
        areaPrices.put("10YNL----------L", new double[]{28.3, 26.8, 25.4, 24.2, 23.9, 25.3, 30.7, 36.8, 38.8, 37.7, 35.3, 33.8, 32.0, 30.7, 29.9, 30.3, 32.0, 37.6, 41.0, 43.1, 40.7, 36.7, 34.4, 30.5});
        areaPrices.put("10YSI-ELES-----O", new double[]{34.9, 31.7, 29.6, 28.4, 28.8, 31.7, 39.4, 45.3, 47.7, 46.7, 45.1, 45.7, 44.8, 42.7, 42.2, 43.3, 45.8, 48.5, 50.6, 53.5, 52.1, 46.5, 41.8, 36.1});
        areaPrices.put("10YHR-HEP------M", new double[]{35.0, 31.7, 29.5, 28.4, 28.7, 31.6, 39.3, 45.6, 48.0, 47.5, 46.1, 46.8, 46.1, 43.8, 43.1, 43.8, 46.7, 49.8, 51.6, 54.4, 53.8, 48.1, 42.6, 36.2});
        areaPrices.put("10YCZ-CEPS-----N", new double[]{27.3, 25.6, 24.4, 23.6, 24.0, 26.1, 32.5, 38.0, 39.6, 38.6, 36.6, 36.1, 35.3, 34.0, 32.6, 33.7, 35.9, 38.9, 42.3, 44.7, 42.3, 37.3, 34.5, 29.7});
        areaPrices.put("10YSK-SEPS-----K", new double[]{27.9, 26.0, 24.8, 24.0, 24.5, 26.8, 33.4, 38.8, 40.7, 40.1, 38.1, 38.1, 37.1, 35.6, 34.3, 35.1, 37.6, 40.3, 43.6, 46.0, 43.9, 38.4, 35.2, 30.5});
        areaPrices.put("10YBE----------2", new double[]{28.2, 26.1, 24.4, 22.3, 21.6, 23.4, 28.5, 34.2, 36.8, 36.6, 34.3, 32.6, 30.5, 28.8, 27.4, 27.9, 29.6, 34.9, 39.6, 41.4, 38.7, 35.0, 34.4, 30.9});
        areaPrices.put("10Y1001A1001A82H", new double[]{25.8, 23.8, 22.6, 21.9, 22.3, 24.3, 30.1, 36.1, 37.5, 35.1, 32.3, 31.1, 29.0, 27.0, 26.2, 27.8, 30.3, 35.9, 39.8, 42.2, 39.7, 35.3, 33.0, 28.5});
        areaPrices.put("10YGR-HTSO-----Y", new double[]{51.5, 50.5, 48.1, 44.8, 43.1, 44.9, 48.7, 53.0, 55.8, 55.9, 54.4, 53.2, 52.8, 51.3, 48.9, 50.0, 52.7, 55.5, 59.4, 60.9, 62.4, 61.9, 58.7, 57.0});
        areaPrices.put("10YCH-SWISSGRIDZ", new double[]{30.7, 28.8, 27.5, 26.6, 26.5, 28.4, 33.0, 36.4, 37.9, 36.9, 35.5, 34.7, 33.2, 31.8, 31.1, 31.5, 33.2, 36.7, 39.5, 40.2, 38.6, 36.5, 35.7, 32.9});
        areaPrices.put("10Y1001A1001A59C", new double[]{30.4, 27.5, 25.2, 23.7, 23.6, 25.8, 30.8, 37.8, 46.6, 48.2, 47.1, 45.7, 46.1, 41.9, 39.2, 39.4, 47.2, 58.6, 59.7, 53.3, 46.9, 42.6, 35.5, 34.8});
        areaPrices.put("10Y1001A1001A39I", new double[]{25.8, 25.8, 23.5, 22.4, 22.2, 23.2, 28.2, 38.2, 44.5, 48.3, 47.5, 46.5, 45.1, 45.9, 45.2, 43.2, 42.7, 43.6, 44.8, 46.9, 44.7, 39.0, 36.1, 30.8});
        areaPrices.put("10YLT-1001A0008Q", new double[]{26.0, 25.9, 23.6, 22.5, 22.3, 23.2, 27.5, 37.2, 43.6, 47.6, 47.0, 46.0, 44.9, 45.9, 45.2, 43.1, 42.5, 43.4, 44.5, 46.5, 44.1, 39.3, 36.2, 31.0});
        areaPrices.put("10YRO-TEL------P", new double[]{172.5, 168.7, 146.9, 132.1, 126.7, 131.2, 155.8, 195.3, 237.8, 246.3, 236.9, 224.1, 220.4, 220.3, 212.2, 211.5, 221.4, 242.4, 259.0, 272.7, 286.2, 276.9, 235.1, 209.9});
    }

    /**
     * Average day-ahead prices for a certain area. For the area EIC codes see {@link EICCodes}.
     * The time zone is used to calculate the local start and end of day.
     * @param areaEIC 
     * @param timezone
     */
    public EntsoeAvgPrices(String areaEIC, ZoneId timezone) {
        if (!areaPrices.containsKey(areaEIC))
            throw new IllegalArgumentException("Prices for area EIC " + areaEIC + " not available.");
        this.areaEIC = areaEIC;
        this.timezone = timezone;
        
        logger.info("areaEIC = " + areaEIC + ", timezone = " + timezone);
    }
    
    @Override
    public Double getCostAt(LocalDateTime time) {
        int hour = time.atZone(TimeUtils.UTC).withZoneSameInstant(timezone).getHour();
        return areaPrices.get(areaEIC)[hour];
    }

}
