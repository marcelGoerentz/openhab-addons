/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.persistence.influxdb.internal;

import java.text.DateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * Java bean used to return items queries results from InfluxDB.
 *
 * @author Theo Weiss - Initial Contribution
 * @author Joan Pujol Espinar - Addon rewrite refactoring code and adding support for InfluxDB 2.0.
 */
@NonNullByDefault
public class InfluxDBHistoricItem implements HistoricItem {

    private String name = "";
    private final State state;
    private final Instant instant;

    public InfluxDBHistoricItem(String name, State state, Instant instant) {
        this.name = name;
        this.state = state;
        this.instant = instant;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return instant.atZone(ZoneId.systemDefault());
    }

    @Override
    public Instant getInstant() {
        return instant;
    }

    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(getTimestamp()) + ": " + name + " -> " + state.toString();
    }
}
