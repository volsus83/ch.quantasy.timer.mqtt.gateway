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

import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 *
 * @author reto
 */
public class TimerDevice {

    private final SortedMap<String, Ticker> tickerMap;
    private final TimerDeviceCallback callback;
    private final Timer timer;

    public TimerDevice(TimerDeviceCallback callback) {
        this.callback = callback;
        tickerMap = new TreeMap<>();
        this.timer = new Timer();

    }

    public void setTimerConfiguration(DeviceTickerConfiguration configuration) {
        Ticker ticker = tickerMap.get(configuration.getId());
        if (ticker == null) {
            ticker = new Ticker();
            tickerMap.put(configuration.getId(), ticker);
        }
        if (ticker.updateConfiguration(configuration)) {
            callback.tickerConfigurationUpdated(configuration);
        }
    }

    public SortedMap<String, Ticker> getTickerMap() {
        return new TreeMap(tickerMap);
    }

    
    class Ticker {

        private DeviceTickerConfiguration configuration;
        private Task timerTask;

        public boolean updateConfiguration(DeviceTickerConfiguration configuration) {
            if (this.configuration == null) {
                this.configuration = configuration;
                this.timerTask = new Task();
                this.timerTask.updateTimer();
                return true;
            } else {
                if (!this.configuration.getId().equals(configuration.getId())) {
                    return false;
                }
                boolean changed = false;
                if (configuration.getFirst() != null && (!configuration.getFirst().equals(this.configuration.getFirst())) && configuration.getFirst() > System.currentTimeMillis()) {
                    this.configuration.setFirst(configuration.getFirst());
                    changed = true;
                }
                if (configuration.getRepeat() != null && (!configuration.getRepeat().equals(this.configuration.getRepeat()))) {
                    this.configuration.setRepeat(configuration.getRepeat());
                    changed = true;
                }
                if (changed) {
                    timerTask.updateTimer();
                }

                if (configuration.getLast() != null && (!configuration.getLast().equals(this.configuration.getLast())) && configuration.getLast() > System.currentTimeMillis()) {
                    this.configuration.setLast(configuration.getLast());
                    changed = true;
                }

                return changed;
            }

        }

        class Task extends TimerTask {

            public Task() {
            }

            @Override
            public void run() {

                if (configuration.getFirst() != null && configuration.getFirst() > System.currentTimeMillis()) {
                    this.cancel();
                    updateTimer();
                    return;
                }
                if (configuration.getLast() != null && configuration.getLast() <= System.currentTimeMillis()) {
                    this.cancel();
                    tickerMap.remove(configuration.getId());
                    callback.tickerConfigurationRemoved(configuration);
                }
                callback.onTick(configuration.getId());
            }

            public final void updateTimer() {
                timerTask.cancel();
                timerTask = new Task();
                long start = 0;
                if (configuration.getFirst() != null) {
                    start = (Math.max(start, configuration.getFirst() - System.currentTimeMillis()));
                }
                if (configuration.getRepeat() == null) {
                    if (configuration.getLast() == null) {
                        //so it is a one timer right now
                        timer.schedule(timerTask, start);
                    } else {
                        //one to start and one to end.
                        timer.scheduleAtFixedRate(timerTask, start, configuration.getLast() - System.currentTimeMillis());
                    }
                } else {
                    timer.scheduleAtFixedRate(timerTask, start, configuration.getRepeat());
                }
            }

        }

    }
}
