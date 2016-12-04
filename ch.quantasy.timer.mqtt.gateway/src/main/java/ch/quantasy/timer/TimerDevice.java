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
            try {
                ticker = new Ticker(configuration);
                tickerMap.put(configuration.getId(), ticker);
            } catch (IllegalArgumentException ex) {
                //Nope. That was something in the past...
            }
        } else {
            ticker.updateConfiguration(configuration);
        }
    }

    public SortedMap<String, Ticker> getTickerMap() {
        return new TreeMap(tickerMap);
    }

    class Ticker {

        private DeviceTickerConfiguration configuration;
        private Task timerTask;

        public Ticker(DeviceTickerConfiguration configuration) throws IllegalArgumentException {
            if (configuration.getEpoch() == null) {
                configuration.setEpoch(System.currentTimeMillis());
            }
            this.configuration = configuration;

            if (isLastReached()) {
                throw new IllegalArgumentException("Timer set to finish in the past.");
            }

            updateConfiguration();
        }

        public boolean isLastReached() {
            if (configuration.getLast() == null) {
                return false;
            }
            return getEpochDelta() >= configuration.getLast();
        }

        public boolean isFirstReached() {
            if (configuration.getFirst() == null) {
                return true;
            }
            return getEpochDelta() >= configuration.getFirst();
        }

        private void updateConfiguration() {
            callback.tickerConfigurationUpdated(this.configuration);
            this.timerTask = new Task();
            this.timerTask.updateTimer();
        }

        public long getEpochDelta() {
            return System.currentTimeMillis() - configuration.getEpoch();
        }

        public void updateConfiguration(DeviceTickerConfiguration configuration) {
            if (!this.configuration.getId().equals(configuration.getId())) {
                return;
            }
            boolean changed = false;
            if (configuration.getFirst() != null && (!configuration.getFirst().equals(this.configuration.getFirst()))) {
                this.configuration.setFirst(configuration.getFirst());
                if (!isFirstReached()) {
                    timerTask.cancel();
                }
                changed = true;
            }
            if (configuration.getInterval() != null && (!configuration.getInterval().equals(this.configuration.getInterval()))) {
                this.configuration.setInterval(configuration.getInterval());
                changed = true;
            }
            if (changed) {
                timerTask.updateTimer();
            }

            if (configuration.getLast() != null && (!configuration.getLast().equals(this.configuration.getLast()))) {
                this.configuration.setLast(configuration.getLast());
                callback.tickerConfigurationUpdated(this.configuration);
                if (isLastReached()) {
                    timerTask.cancel();
                    tickerMap.remove(configuration.getId());
                    callback.tickerConfigurationRemoved(this.configuration);
                    return;
                }
                changed = true;
            }
            if (changed) {
                callback.tickerConfigurationUpdated(this.configuration);
            }
        }

        class Task extends TimerTask {

            public Task() {
            }

            @Override
            public void run() {

                if (!isFirstReached()) {
                    this.cancel();
                    updateTimer();
                    return;
                }
                if (isLastReached()) {
                    this.cancel();
                    tickerMap.remove(configuration.getId());
                    callback.tickerConfigurationRemoved(configuration);
                }
                callback.onTick(configuration.getId(), getEpochDelta());
            }

            public final void updateTimer() {
                timerTask.cancel();
                timerTask = new Task();
                long start = 0;
                if (configuration.getFirst() != null) {
                    start = (Math.max(start, getEpochDelta() + configuration.getFirst()));
                }
                if (configuration.getInterval() == null || configuration.getInterval() == 0) {
                    if (configuration.getLast() == null) {
                        //so it is a one timer right now
                        timer.schedule(timerTask, start);
                        tickerMap.remove(configuration.getId());
                        callback.tickerConfigurationRemoved(configuration);
                    } else {
                        //one to start and one to end.
                        timer.scheduleAtFixedRate(timerTask, start, Math.max(0,getEpochDelta() + configuration.getLast() - start));
                    }
                } else {
                    timer.scheduleAtFixedRate(timerTask, start, configuration.getInterval());
                }
            }

        }

    }
}
