/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.easyfamily.internal.dto.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The {@link Entry} class manages .
 *
 * @author Marcel Goerentz - Initial contribution
 */
@XStreamAlias("e")
public class Entry {
    @XStreamAlias("i")
    private Ich ich;

    @XStreamAlias("r")
    private ConfigData config;

    @XStreamAlias("p")
    private PData p;

    public PData getP() {
        return p;
    }

    public void setP(PData p) {
        this.p = p;
    }

    public Ich getIch() {
        return ich;
    }

    public void setIch(Ich ich) {
        this.ich = ich;
    }

    public ConfigData getConfig() {
        return config;
    }

    public void setConfig(ConfigData config) {
        this.config = config;
    }
}
