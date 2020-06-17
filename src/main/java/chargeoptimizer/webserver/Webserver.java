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
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.LoggerFactory;

/**
 * Local web server for letting the user control the ChargeOptimizer.
 */
public class Webserver {
    
    final org.slf4j.Logger logger = LoggerFactory.getLogger(Webserver.class);
    
    private final Server server;
    
    public Webserver(ChargeOptimizer chargeOptimizer, int port) {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.setAttribute("ChargeOptimizer", chargeOptimizer);
        handler.addServlet(StatusServlet.class, "/status/");
        server.setHandler(handler);
    }
    
    public void start() {
        logger.info("Webserver is starting.");
        try {
            server.start();
        } catch (Exception ex) {
            logger.error("Could not start jetty web server.", ex);
        }
    }
    
    public void stop() {
        logger.info("Webserver is stopping.");
        try {
            server.stop();
        } catch (Exception ex) {
            logger.error("Could not start jetty web server.", ex);
        }
    }
}
