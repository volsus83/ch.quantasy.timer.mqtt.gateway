/*
 *   "TimerMqWay"
 *
 *    TimerMqWay(tm): A gateway to provide a universal timer ability.
 *
 *    Copyright (c) 2016 Bern University of Applied Sciences (BFH),
 *    Research Institute for Security in the Information Society (RISIS), Wireless Communications & Secure Internet of Things (WiCom & SIoT),
 *    Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *    Licensed under Dual License consisting of:
 *    1. GNU Affero General Public License (AGPL) v3
 *    and
 *    2. Commercial license
 *
 *
 *    1. This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *    2. Licensees holding valid commercial licenses for TiMqWay may use this file in
 *     accordance with the commercial license agreement provided with the
 *     Software or, alternatively, in accordance with the terms contained in
 *     a written agreement between you and Bern University of Applied Sciences (BFH),
 *     Research Institute for Security in the Information Society (RISIS), Wireless Communications & Secure Internet of Things (WiCom & SIoT),
 *     Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *     For further information contact <e-mail: reto.koenig@bfh.ch>
 *
 *
 */
package ch.quantasy.gateway.service.timer;

import ch.quantasy.mqtt.gateway.client.GatewayClient;
import ch.quantasy.timer.DeviceTickerConfiguration;
import ch.quantasy.timer.TimerDevice;
import ch.quantasy.timer.TimerDeviceCallback;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author reto
 */
public class TimerService extends GatewayClient<TimerServiceContract> implements TimerDeviceCallback {

    private final TimerDevice device;

    public TimerService(URI mqttURI, String instance) throws MqttException {
        super(mqttURI, instance + "TimerService.q334oi34-q34", new TimerServiceContract(instance));
        addDescription(getContract().INTENT_CONFIGURATION, "id: <String>\n first: [null|0.." + Long.MAX_VALUE + "]\n repeat: [null|1.." + Long.MAX_VALUE + "]\n last: [null|0.." + Long.MAX_VALUE + "]\n");
        addDescription(getContract().STATUS_CONFIGURATION + "/<id>", "id: <String>\n first: [null|0.." + Long.MAX_VALUE + "]\n repeat: [null|1.." + Long.MAX_VALUE + "]\n last: [null|0.." + Long.MAX_VALUE + "]\n");
        addDescription(getContract().EVENT_TICK + "/<id>", "timestamp: [0.." + Long.MAX_VALUE + "]\n value: true\n");
        addDescription(getContract().STATUS_UNIX_EPOCH, "milliseconds: [0.." + Long.MAX_VALUE + "]\n");

        configurations = new TreeSet<>();
        device = new TimerDevice(this);
        subscribe(getContract().INTENT_CONFIGURATION + "/#", (topic, payload) -> {
            try {
                DeviceTickerConfiguration configuration = super.getMapper().readValue(payload, DeviceTickerConfiguration.class);
                device.setTimerConfiguration(configuration);
            } catch (Exception ex) {
                Logger.getLogger(TimerService.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        super.connect();
        device.setTimerConfiguration(new DeviceTickerConfiguration(super.getParameters().getClientID(), null, 1000L, null));
    }

    private SortedSet<DeviceTickerConfiguration> configurations;

    @Override
    public void tickerConfigurationUpdated(DeviceTickerConfiguration configuration) {
        if (configuration.getId().equals(super.getParameters().getClientID())) {
            return;
        }
        configurations.add(configuration);
        addStatus(getContract().STATUS_CONFIGURATION + "/" + configuration.getId(), configuration);
    }

    @Override
    public void onTick(String id) {
        if (id.equals(super.getParameters().getClientID())) {
            addStatus(getContract().STATUS_UNIX_EPOCH, new UnixEpochStatus());
        } else {
            addEvent(getContract().EVENT_TICK + "/" + id, true);
        }
    }

    @Override
    public void tickerConfigurationRemoved(DeviceTickerConfiguration configuration) {
        configurations.remove(configuration);
        addStatus(getContract().STATUS_CONFIGURATION + "/" + configuration.getId(), null);
    }
    
    final class UnixEpochStatus{
        private long millisceconds;
        public UnixEpochStatus(){
            millisceconds=System.currentTimeMillis();
        }

        public long getMillisceconds() {
            return millisceconds;
        }
        
    }

}
