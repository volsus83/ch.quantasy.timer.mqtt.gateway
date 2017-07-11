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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author reto
 */
public class TimerDevice {

    private final SortedMap<String, Ticker> tickerMap;
    private final TimerDeviceCallback callback;
    private final ScheduledExecutorService timerService;

    public TimerDevice(TimerDeviceCallback callback) {
        this.callback = callback;
        tickerMap = new TreeMap<>();
        this.timerService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), (Runnable r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public void cancel(DeviceTickerCancel cancel) {
        if (cancel == null || cancel.getId() == null) {
            return;
        }
        Ticker ticker = tickerMap.get(cancel.getId());
        if (ticker != null) {
            ticker.cancel();
        }
    }

    public void setTickerConfiguration(DeviceTickerConfiguration configuration) {
        if (configuration == null) {
            return;
        }
        Logger.getLogger(TimerDevice.class.getName()).log(Level.INFO, null, configuration);
        Ticker ticker = null;
        synchronized (this) {
            ticker = tickerMap.get(configuration.getId());
            if (ticker == null) {
                ticker = new Ticker(configuration.getId());
                tickerMap.put(configuration.getId(), ticker);
            }
        }
        ticker.updateConfig(configuration);

    }

    public SortedMap<String, Ticker> getTickerMap() {
        return new TreeMap(tickerMap);
    }

    public synchronized void removeTicker(Ticker ticker) {
        tickerMap.remove(ticker.id);
        callback.tickerConfigurationRemoved(ticker.configuration);
    }

    class Ticker {

        private DeviceTickerConfiguration configuration;
        private String id;
        private Task task;

        public Ticker(String id) {
            this.id = id;
        }

        public synchronized void cancel() {
            this.task.taskFuture.cancel(true);
            removeTicker(Ticker.this);
        }

        public synchronized void updateConfig(DeviceTickerConfiguration configuration) {
            if (configuration == null) {
                return;
            }
            if (!this.id.equals(configuration.getId())) {
                return;
            }
            if (this.configuration == null) {
                this.configuration = new DeviceTickerConfiguration(configuration);
                if (this.configuration.getEpoch() == null) {
                    this.configuration.setEpoch(System.currentTimeMillis());
                }
                if (this.configuration.getFirst() == null) {
                    this.configuration.setFirst(0);
                }
            } else if (this.configuration.equals(configuration)) {
                return;
            } else {
                if (configuration.getEpoch() != null) {
                    this.configuration.setEpoch(configuration.getEpoch());
                }
                if (configuration.getFirst() != null) {
                    this.configuration.setFirst(configuration.getFirst());
                }
                if (configuration.getLast() != null) {
                    this.configuration.setLast(configuration.getLast());
                }
                if (configuration.getInterval() != null) {
                    this.configuration.setInterval(configuration.getInterval());
                }
            }
            task = new Task(task);
        }

        class Task implements Runnable {

            private Future taskFuture;
            private boolean isFirstReached = false;
            private long latestTick = 0;

            public Task(Task oldTask) {
                if (oldTask != null && oldTask.taskFuture != null) {
                    oldTask.taskFuture.cancel(false);
                    this.latestTick = oldTask.latestTick;
                }
                callback.tickerConfigurationUpdated(configuration);                
                Integer interval = configuration.getInterval();
                Long start = configuration.getFirstInMillisFromNow();
                System.out.println("Start in: "+start);
                if (start == null || start <= 0) {                    
                    if (interval != null && interval > 0) {
                        start = Math.max(0, interval - (System.currentTimeMillis() - latestTick));
                        isFirstReached = configuration.isFirstReached();
                        taskFuture = timerService.scheduleAtFixedRate(this, start, interval, TimeUnit.MILLISECONDS);
                    } else {
                        isFirstReached = configuration.isFirstReached();
                        taskFuture = timerService.schedule(this, 0, TimeUnit.MILLISECONDS);
                    }
                } else {
                    if (interval != null && interval > 0) {
                        isFirstReached = configuration.isFirstReached();
                        //Logger.getLogger(TimerDevice.class.getName()).log(Level.INFO, System.currentTimeMillis() + ": let us see: start:" + start + " interval: " + interval);
                        taskFuture = timerService.scheduleAtFixedRate(this, start, interval, TimeUnit.MILLISECONDS);
                    } else {
                        //Logger.getLogger(TimerDevice.class.getName()).log(Level.INFO, System.currentTimeMillis() + ": a one timer: " + configuration);
                        isFirstReached = configuration.isFirstReached();
                        taskFuture = timerService.schedule(this, start, TimeUnit.MILLISECONDS);
                    }
                }
            }

            @Override
            public void run() {
                if (taskFuture.isCancelled()) {
                    return;
                }
                if (configuration == null) {
                    return;
                }
                //Logger.getLogger(TimerDevice.class.getName()).log(Level.INFO, System.currentTimeMillis() + ":" + configuration);

                if (isFirstReached == false) {
                    isFirstReached = true;
                    callback.tickerConfigurationUpdated(configuration);
                }
                latestTick = System.currentTimeMillis();
                callback.onTick(configuration.getId(), 0 - configuration.getEpochDelta());
                //Logger.getLogger(TimerDevice.class.getName()).log(Level.INFO, System.currentTimeMillis() + ": " + latestTick);
                if (configuration.isFinished()) {
                    taskFuture.cancel(false);
                    callback.tickerConfigurationUpdated(configuration);
                    removeTicker(Ticker.this);
                }
            }

        }

    }

}
