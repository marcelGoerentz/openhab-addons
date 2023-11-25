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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * The {@link Ich} class manages the httpClient connection.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "data" })
public class Ich {

    @XStreamAsAttribute
    private final String ich;

    private final String data;

    public Ich(String ich) {
        this.ich = ich;
        this.data = "";
    }

    public String getIch() {
        return ich;
    }

    public String getData() {
        return data;
    }
}
