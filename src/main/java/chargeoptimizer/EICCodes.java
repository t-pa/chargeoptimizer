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

import java.util.Map;

/**
 * Stores information about area EIC codes and timezones.
 */
public class EICCodes {
    
    public static class AreaInfo {
        public final String eic;
        public final String timezone;
        
        public AreaInfo(String eic, String timezone) {
            this.eic = eic;
            this.timezone = timezone;
        }
    }
    
    public static final Map<String, AreaInfo> areas = Map.ofEntries(
            Map.entry("Austria", new AreaInfo("10YAT-APG------L", "Europe/Vienna")),
            Map.entry("Belgium", new AreaInfo("10YBE----------2", "Europe/Brussels")),
            Map.entry("Bulgaria", new AreaInfo("10YCA-BULGARIA-R", "Europe/Sofia")),
            Map.entry("Croatia", new AreaInfo("10YHR-HEP------M", "Europe/Zagreb")),
            Map.entry("Czech Republic", new AreaInfo("10YCZ-CEPS-----N", "Europe/Prague")),
            Map.entry("Denmark DK1", new AreaInfo("10YDK-1--------W", "Europe/Copenhagen")),
            Map.entry("Denmark DK2", new AreaInfo("10YDK-2--------M", "Europe/Copenhagen")),
            Map.entry("Estonia", new AreaInfo("10Y1001A1001A39I", "Europe/Tallinn")),
            Map.entry("Finland", new AreaInfo("10YFI-1--------U", "Europe/Helsinki")),
            Map.entry("France", new AreaInfo("10YFR-RTE------C", "Europe/Paris")),
            Map.entry("Germany", new AreaInfo("10Y1001A1001A82H", "Europe/Berlin")),
            Map.entry("Great Britain", new AreaInfo("10YGB----------A", "Europe/London")),
            Map.entry("Greece", new AreaInfo("10YGR-HTSO-----Y", "Europe/Athens")),
            Map.entry("Hungary", new AreaInfo("10YHU-MAVIR----U", "Europe/Budapest")),
            Map.entry("Ireland and Northern Ireland", new AreaInfo("10Y1001A1001A59C", "Europe/Dublin")),
            Map.entry("Italy Centre-North", new AreaInfo("10Y1001A1001A70O", "Europe/Rome")),
            Map.entry("Italy Centre-South", new AreaInfo("10Y1001A1001A71M", "Europe/Rome")),
            Map.entry("Latvia", new AreaInfo("10YLV-1001A00074", "Europe/Riga")),
            Map.entry("Lithuania", new AreaInfo("10YLT-1001A0008Q", "Europe/Vilnius")),
            Map.entry("Netherlands", new AreaInfo("10YNL----------L", "Europe/Amsterdam")),
            Map.entry("Norway NO1", new AreaInfo("10YNO-1--------2", "Europe/Oslo")),
            Map.entry("Norway NO2", new AreaInfo("10YNO-2--------T", "Europe/Oslo")),
            Map.entry("Norway NO3", new AreaInfo("10YNO-3--------J", "Europe/Oslo")),
            Map.entry("Norway NO4", new AreaInfo("10YNO-4--------9", "Europe/Oslo")),
            Map.entry("Norway NO5", new AreaInfo("10Y1001A1001A48H", "Europe/Oslo")),
            Map.entry("Poland", new AreaInfo("10YPL-AREA-----S", "Europe/Warsaw")),
            Map.entry("Portugal", new AreaInfo("10YPT-REN------W", "Europe/Lisbon")),
            Map.entry("Romania", new AreaInfo("10YRO-TEL------P", "Europe/Bucharest")),
            Map.entry("Serbia", new AreaInfo("10YCS-SERBIATSOV", "Europe/Belgrade")),
            Map.entry("Slovakia", new AreaInfo("10YSK-SEPS-----K", "Europe/Bratislava")),
            Map.entry("Slovenia", new AreaInfo("10YSI-ELES-----O", "Europe/Ljubljana")),
            Map.entry("Spain", new AreaInfo("10YES-REE------0", "Europe/Madrid")),
            Map.entry("Sweden SE1", new AreaInfo("10Y1001A1001A44P", "Europe/Stockholm")),
            Map.entry("Sweden SE2", new AreaInfo("10Y1001A1001A45N", "Europe/Stockholm")),
            Map.entry("Sweden SE3", new AreaInfo("10Y1001A1001A46L", "Europe/Stockholm")),
            Map.entry("Sweden SE4", new AreaInfo("10Y1001A1001A47J", "Europe/Stockholm")),
            Map.entry("Switzerland", new AreaInfo("10YCH-SWISSGRIDZ", "Europe/Zurich"))
    );
}
