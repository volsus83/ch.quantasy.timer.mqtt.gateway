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
package ch.quantasy.timer;

import java.io.IOException;

/**
 *
 * @author reto
 */
public class TimerDeviceTestMain implements TimerDeviceCallback {

    public TimerDeviceTestMain() throws InterruptedException {
        TimerDevice td = new TimerDevice(this);
        td.setTimerConfiguration(new DeviceTickerConfiguration("yyy", System.currentTimeMillis(), 2000L, System.currentTimeMillis() + (10 * 1000)));

        td.setTimerConfiguration(new DeviceTickerConfiguration("xxx", System.currentTimeMillis(), 1000L, System.currentTimeMillis() + (10 * 1000)));
        Thread.sleep(5000);
        td.setTimerConfiguration(new DeviceTickerConfiguration("xxx", null, 100L, null));
        Thread.sleep(2000);
        td.setTimerConfiguration(new DeviceTickerConfiguration("xxx", System.currentTimeMillis() + (5 * 1000), 500L, System.currentTimeMillis() + (10 * 1000)));
        Thread.sleep(8000);
        td.setTimerConfiguration(new DeviceTickerConfiguration("xxx", null, null, System.currentTimeMillis() + (10 * 1000)));

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new TimerDeviceTestMain();
        System.in.read();
    }

    @Override
    public void tickerConfigurationUpdated(DeviceTickerConfiguration configuration) {
        System.out.println("Config: " + configuration);
    }

    @Override
    public void onTick(String id) {
        System.out.printf("%d ID: %s% d%n", count++, id, System.currentTimeMillis());
    }
    private int count;

    @Override
    public void tickerConfigurationRemoved(DeviceTickerConfiguration configuration) {
        System.out.println("Config removed: " + configuration);
    }
}
