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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This charger object communicates via a file. It can be used for testing or as a generic
 * interface to a charger.
 */
public class FileCharger implements Charger {

    final Logger logger = LoggerFactory.getLogger(FileCharger.class);

    private final File file;
    
    private Properties properties;
    private static final String CAR_CONNECTED = "carConnected";
    private static final String CHARGING = "charging";
    private static final String ENABLED = "enabled";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    
    public FileCharger(String filename) {
        file = new File(filename);
        
        properties = new Properties();
        
        try {
            readProperties();
            logger.info("Connection file " + filename + " loaded.");
        } catch (IOException ex) {
            properties.setProperty(CAR_CONNECTED, "false");
            properties.setProperty(CHARGING, "false");
            properties.setProperty(ENABLED, "false");
            try {
                writeProperties();
            } catch (IOException ex1) {
                logger.error("Could not write charger file.", ex);
            }
            logger.info("Created new connection file " + filename + ".");
        }
    }
            
    @Override
    public State getState() throws IOException {
        readProperties();
        if (TRUE.equals(properties.get(CHARGING))) {
            return State.CHARGING;
        } else if (TRUE.equals(properties.get(CAR_CONNECTED))) {
            return State.CAR_CONNECTED;
        } else {
            return State.NO_CAR;
        }
    }

    @Override
    public void setEnabled(boolean enabled) throws IOException {
        properties.setProperty(ENABLED, enabled ? TRUE : FALSE);
        writeProperties();
    }

    @Override
    public boolean getEnabled() throws IOException {
        readProperties();
        return TRUE.equals(properties.get(ENABLED));
    }
    
    private void readProperties() throws IOException {
        properties.load(new FileReader(file));
    }
    
    private void writeProperties() throws IOException {
        properties.store(new FileWriter(file), "FileCharger properties");
    }
    
}
