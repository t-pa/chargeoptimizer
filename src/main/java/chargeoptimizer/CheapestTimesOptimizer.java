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
 *)
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package chargeoptimizer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * An optimizer that guarantees a minimum charging time and distributes this time to the cheapest
 * time slots. After the minimum charging time has been reached, charging is also enabled.
 */
public class CheapestTimesOptimizer implements Optimizer {
    
    private Duration minimumChargingTime;

    @Override
    public TimeSeries<Boolean> optimize(TimeSeries<Double> costs) {
        // sort times by cost
        ArrayList<TimeSeries.Entry<Double>> times = costs.getEntries();
        times.sort((o1, o2) -> Double.compare(o1.item, o2.item));
        
        // find maximum cost needed to get the minimum charging time
        int timesNeeded = (int) minimumChargingTime.dividedBy(costs.getGranularity());
        if (costs.getGranularity().multipliedBy(timesNeeded).compareTo(minimumChargingTime) < 0)
            timesNeeded++;
        Double maxCost = times.get(timesNeeded-1).item;
        
        // enable charging at all times where the cost is smaller than maxCost and also after the
        // minimum charging time has been reached
        ArrayList<Boolean> enabled = new ArrayList<>(costs.size());
        int count = 0;
        for (LocalDateTime time : costs.getTimes()) {
            if (costs.getValueAt(time) <= maxCost || count >= timesNeeded) {
                count++;
                enabled.add(true);
            } else {
                enabled.add(false);
            }
        }
        
        return new TimeSeries<>(costs.getStart(), costs.getGranularity(), enabled, false, true);
    }
    
    public CheapestTimesOptimizer(Duration minimumChargingTime) {
        this.minimumChargingTime = minimumChargingTime;
    }

    public Duration getMinimumChargingTime() {
        return minimumChargingTime;
    }

    public void setMinimumChargingTime(Duration minimumChargingTime) {
        this.minimumChargingTime = minimumChargingTime;
    }

}