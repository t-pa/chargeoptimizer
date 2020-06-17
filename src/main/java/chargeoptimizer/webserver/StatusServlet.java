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
package chargeoptimizer.webserver;

import chargeoptimizer.ChargeOptimizer;
import chargeoptimizer.Charger;
import chargeoptimizer.TimeUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Simple REST interface to see the status of the ChargeOptimizer and to control it; can be used
 * by, e.g., OpenHAB.
 */
public class StatusServlet extends HttpServlet {
    
    // all LocalDateTimes are converted to the current time zone because OpenHAB currently does
    // not honor the time zone information, and then put into this format:
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LocalDateTime now = TimeUtils.now();
        
        ChargeOptimizer chargeOptimizer = (ChargeOptimizer) getServletContext().
                getAttribute("ChargeOptimizer");
        ExecutorService executor = chargeOptimizer.getExecutorService();

        String override = request.getParameter("override");
        if (override != null) {
            override = override.toLowerCase();
            
            if ("on".equals(override) || "true".equals(override)) {
                executor.submit(() -> chargeOptimizer.setOverride(true));
            } else if ("off".equals(override) || "false".equals(override)) {
                executor.submit(() -> chargeOptimizer.setOverride(false));
            }
        }
        
        String chargingAllowed = request.getParameter("chargingAllowed");
        if (chargingAllowed != null) {
            chargingAllowed = chargingAllowed.toLowerCase();
            
            if ("on".equals(chargingAllowed) || "true".equals(chargingAllowed)) {
                executor.submit(() -> chargeOptimizer.setChargerEnabled(true));
            } else if ("off".equals(chargingAllowed) || "false".equals(chargingAllowed)) {
                executor.submit(() -> chargeOptimizer.setChargerEnabled(false));
            }            
        }

        Charger.State chargerState = chargeOptimizer.getChargerState();
        LocalDateTime lastStateChange = chargeOptimizer.getLastStateChange();
        boolean isChargingAllowed = chargeOptimizer.getChargerEnabled();
        LocalDateTime lastEnabledChange = chargeOptimizer.getLastEnabledChange();
        boolean overrideActive = chargeOptimizer.getOverride();
        
        Double costNow = Double.NaN;
        LocalDateTime chargingAllowedSinceOrWhen = null;
        try {
            costNow = executor.submit(() -> chargeOptimizer.costAt(now)).get();
            chargingAllowedSinceOrWhen = (isChargingAllowed ? lastEnabledChange : 
                executor.submit(() -> chargeOptimizer.nextEnabledStateChange(now)).get());
        } catch (InterruptedException | ExecutionException ex) {
        }

        JSONObject jo = new JSONObject();
        jo.put("carConnected", chargerState.isConnected());
        jo.put("charging", (chargerState == Charger.State.CHARGING));
        jo.put("lastStateChange", lastStateChange.atZone(TimeUtils.UTC)
                .withZoneSameInstant(ZoneId.systemDefault()).format(DATE_FORMATTER));
        jo.put("chargingAllowed", isChargingAllowed);
        if (chargingAllowedSinceOrWhen != null)
            jo.put("chargingAllowedSinceOrWhen", chargingAllowedSinceOrWhen.atZone(TimeUtils.UTC)
                    .withZoneSameInstant(ZoneId.systemDefault()).format(DATE_FORMATTER));
        jo.put("override", overrideActive);
        jo.put("costs", costNow);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(jo.toString());
    }    
}
