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

import chargeoptimizer.webserver.Webserver;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        logger.info("Loading configuration...");
        
        Properties config = new Properties();
        if (args.length < 1) {
            logger.info("Running with default configuration. To change this, start this "+
                    "program with a configuration file as the command line parameter.");
        } else {
            try {
                config.load(new FileReader(args[0]));
            } catch (IOException ex) {
                logger.error("Could not read configuration file " + args[0] + ".", ex);
                System.exit(1);
            }
        }
        
        ChargeOptimizer chargeOptimizer = new ChargeOptimizer();
        
        // Charger
        switch (config.getProperty("charger", "File")) {
            case "Wallbe":
                String host = config.getProperty("wallbeCharger.host", "192.168.0.8");
                int port = Integer.parseInt(config.getProperty("wallbeCharger.port", "502"));
                chargeOptimizer.setCharger(new WallbeCharger(host, port));
                break;
                
            case "File":
                String filename = config.getProperty("fileCharger.filename", "/tmp/filecharger.txt");
                chargeOptimizer.setCharger(new FileCharger(filename));
                break;
                
            default:
                logger.error("Unknown charger type " + config.getProperty("charger"));
                System.exit(1);
        }
        
        // CostSource
        switch (config.getProperty("costSource", "EntsoeAvgPrices")) {
            case "EntsoeDayAhead":
                String areaCode = config.getProperty("entsoe.areaCode", "10Y1001A1001A82H");
                ZoneId timezone = ZoneId.of(config.getProperty("entsoe.timezone", "Europe/Berlin"));
                String securityToken =  config.getProperty("entsoe.securityToken");
                chargeOptimizer.setCostSource(new EntsoeDayAhead(areaCode, timezone, securityToken));
                break;
                
            case "EntsoeAvgPrices":
                areaCode = config.getProperty("entsoe.areaCode", "10Y1001A1001A82H");
                timezone = ZoneId.of(config.getProperty("entsoe.timezone", "Europe/Berlin"));
                chargeOptimizer.setCostSource(new EntsoeAvgPrices(areaCode, timezone));
                break;
                
            default:
                logger.error("Unknown cost source " + config.getProperty("costSource"));
                System.exit(1);
        }
        
        // Optimizer
        switch (config.getProperty("optimizer", "CheapestTimesOptimizer")) {
            case "CheapestTimesOptimizer":
                int minimumChargingTime = Integer.parseInt(
                        config.getProperty("minimumChargingTime", "180"));
                chargeOptimizer.setOptimizer(
                        new CheapestTimesOptimizer(Duration.ofMinutes(minimumChargingTime)));
                break;
                
            default:
                logger.error("Unknown optimizer " + config.getProperty("optimizer"));
                System.exit(1);
        }
        int optimizationTime = Integer.parseInt(config.getProperty("optimizationTime", "480"));
        chargeOptimizer.setOptimizationTime(Duration.ofMinutes(optimizationTime));
        
        // StatisticsDatabase
        String dbUrl = config.getProperty("statisticsDatabase.url", "jdbc:h2:mem:chargeoptim");
        String dbUser = config.getProperty("statisticsDatabase.user", "");
        String dbPassword = config.getProperty("statisticsDatabase.password", "");
        chargeOptimizer.setStatisticsDatabase(new StatisticsDatabase(dbUrl, dbUser, dbPassword));
        
        // Webserver
        Webserver webserver = null;
        int port = Integer.parseInt(config.getProperty("webserver.port", "0"));
        if (port != 0)
            webserver = new Webserver(chargeOptimizer, port);
        
        chargeOptimizer.start();
        if (webserver != null)  webserver.start();
        logger.info("Press enter to stop.");
        try {
            System.in.read();
        } catch (IOException ex) { }
        if (webserver != null)  webserver.stop();
        chargeOptimizer.stop();
    }
    
}
