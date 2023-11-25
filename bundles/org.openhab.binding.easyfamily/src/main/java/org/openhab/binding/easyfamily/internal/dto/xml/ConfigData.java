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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The {@link ConfigData} class manages .
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class ConfigData {

    @XStreamAlias("n")
    @XStreamAsAttribute
    private final String name;

    @XStreamAlias("v")
    @XStreamAsAttribute
    private final String version;

    public ConfigData(String n, String v) {
        name = n;
        version = v;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
