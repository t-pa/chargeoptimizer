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

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Communicates with a Wallbe charger via Modbus.
 */
public class WallbeCharger implements Charger {
    
    final Logger logger = LoggerFactory.getLogger(WallbeCharger.class);
    
    private final ModbusMaster master;
    private static final int REGISTER_STATUS = 100;
    private static final int COIL_ENABLED = 400;
    
    public WallbeCharger(String host, int port) {
        TcpParameters tcpParams = new TcpParameters(host, port, true);
        master = ModbusMasterFactory.createModbusMasterTCP(tcpParams);
    }
    
    public void close() {
        try {
            master.disconnect();
        } catch (ModbusIOException ex) {
            logger.error("Error while disconnecting.", ex);
        }
    }

    @Override
    public State getState() throws IOException {
        try {
            if (!master.isConnected()) master.connect();
            int[] state = master.readInputRegisters(Modbus.TCP_DEFAULT_ID, REGISTER_STATUS, 1);
        
            switch (state[0]) {
                case 65: return State.NO_CAR;
                case 66: return State.CAR_CONNECTED;
                case 67:
                case 68: return State.CHARGING;
                default: return State.ERROR;
            }
        } catch (ModbusIOException | ModbusNumberException | ModbusProtocolException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) throws IOException {
        try {
            if (!master.isConnected()) master.connect();
            master.writeSingleCoil(Modbus.TCP_DEFAULT_ID, COIL_ENABLED, enabled);
        } catch (ModbusIOException | ModbusNumberException | ModbusProtocolException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean getEnabled() throws IOException {
        try {
            if (!master.isConnected()) master.connect();
            boolean[] active = master.readCoils(Modbus.TCP_DEFAULT_ID, COIL_ENABLED, 1);
            return active[0];
        } catch (ModbusIOException | ModbusNumberException | ModbusProtocolException ex) {
            throw new IOException(ex);
        }
    }

}