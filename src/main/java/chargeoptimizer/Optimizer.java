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

/**
 * Optimizes the times at which the car should be charged.
 */
public interface Optimizer {
    
    /**
     * Optimize the times at which the car should be charged.
     * @param costs this time series specifies the costs and the time interval for optimization
     * @return 
     */
    public TimeSeries<Boolean> optimize(TimeSeries<Double> costs);
    
 }
