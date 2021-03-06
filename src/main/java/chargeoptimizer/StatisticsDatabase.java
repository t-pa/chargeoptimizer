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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage connection to a database where the current state can be logged for statistics.
 */
public class StatisticsDatabase {
    
    private final Logger logger = LoggerFactory.getLogger(StatisticsDatabase.class);

    private final JdbcConnectionPool connPool;
    
    public StatisticsDatabase(String databaseUrl, String user, String password) {
        logger.info("databaseUrl = " + databaseUrl + ", user = " + user);
        
        try {
            Flyway.configure().dataSource(databaseUrl, user, password).load().migrate();
        } catch (FlywayException ex) {
            logger.error("Error accessing database.", ex);
        }
        
        connPool = JdbcConnectionPool.create(databaseUrl, user, password);
    }
    
    public void logState(LocalDateTime time, Charger.State state, boolean chargingAllowed, double price) {
        logger.debug("Logging at " + time + ", state=" + state +
                    ", chargingAllowed=" + chargingAllowed +
                    ", price=" + price);
        
        try (
            Connection conn = connPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO chargelog" +
                    "  (logtime, carconnected, charging, chargingAllowed, price) " +
                    "VALUES" +
                    "  (?, ?, ?, ?, ?)");
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(time));
            stmt.setBoolean(2, state.isConnected());
            stmt.setBoolean(3, state == Charger.State.CHARGING);
            stmt.setBoolean(4, chargingAllowed);
            stmt.setDouble(5, price);
            stmt.execute();
        } catch (SQLException ex) {
            logger.error("Error accessing database.", ex);
        }
    }
    
}
