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

/**
 *
 * @author reto
 */
public class DeviceTickerConfiguration implements Comparable<DeviceTickerConfiguration> {

    private String id;
    private Long first;
    private Long repeat;
    private Long last;

    /**
     *
     * @return First absolute time in ms (epoc) when the ticker begins ticking
     */
    public Long getFirst() {
        return first;
    }

    /**
     *
     * @param first First absolute time in ms (epoc) when the ticker begins
     * ticking. null means immediate start of ticking If first is smaller than
     * @code{System.currentTimeInMillis()} ticking starts immediate If first is
     * negative, the value will not be accepted (old value persists)
     */
    public void setFirst(Long first) {
        if (first < 0) {
            return;
        }
        this.first = first;
    }

    /**
     *
     * @return Id of the ticker to be configured.
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public Long getLast() {
        return last;
    }

    /**
     *
     * @param last absolute time in ms (epoc) when the ticker terminates. null
     * means immediate termination after one ticking If last is smaller than
     * {@code System.currentTimeInMillis()} ticking terminates immediate If last
     * is negative, the value will not be accepted (old value persists)
     */
    public void setLast(Long last) {
        if (last < 0) {
            return;
        }
        this.last = last;
    }

    public Long getRepeat() {
        return repeat;
    }

    /**
     *
     * @param repeat delay in ms (epoc) between two ticks. null means no
     * repetition If repeat is negative, the value will not be accepted (old
     * value persists)
     */
    public void setRepeat(Long repeat) {
        if (repeat < 0) {
            return;
        }
        this.repeat = repeat;
    }

    private DeviceTickerConfiguration() {
    }

    /**
     *
     * @param id Identifier of the ticker to be configured
     * @param first {@link #setFirst(java.lang.Long) }
     * @param repeat {@link #setRepeat(java.lang.Long) }
     * @param last  {@link #setLast(java.lang.Long) }
     */
    public DeviceTickerConfiguration(String id, Long first, Long repeat, Long last) {
        this.id = id;
        this.first = first;
        this.repeat = repeat;
        this.last = last;
    }

    @Override
    public int compareTo(DeviceTickerConfiguration o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "DeviceTickerConfiguration{" + "id=" + id + ", first=" + first + ", repeat=" + repeat + ", last=" + last + '}';
    }

}
