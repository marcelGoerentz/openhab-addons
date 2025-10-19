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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The {@link Ich} class manages the httpClient connection.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@XStreamAlias("i")
public class Ich {
    @XStreamAsAttribute
    private String ich;

    public String getIch() {
        return ich;
    }

    public void setIch(String ich) {
        this.ich = ich;
    }
}
