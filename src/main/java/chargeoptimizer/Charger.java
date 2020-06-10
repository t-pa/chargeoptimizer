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

import java.io.IOException;

/**
 * Represents the charging device, for example a wallbox charger.
 */
public interface Charger {
    
    public enum State {
        NO_CAR,
        CAR_CONNECTED,
        CHARGING,
        ERROR
    }
    
    /**
     * Get the state of the charger and its connection to the car.
     * @return the current state
     * @throws IOException 
     */
    public State getState() throws IOException;
    
    /** 
     * Set the enabled bit, which controls whether charging is allowed.
     * @param enabled
     * @throws IOException 
     */
    public void setEnabled(boolean enabled) throws IOException;
    
    /** 
     * Get the enabled bit, which controls whether charging is allowed.
     * @return
     * @throws IOException 
     */
    public boolean getEnabled() throws IOException;
}
