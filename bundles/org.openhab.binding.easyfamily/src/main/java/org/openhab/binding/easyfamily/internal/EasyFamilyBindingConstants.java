/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.easyfamily.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EasyFamilyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyBindingConstants {

    public static final String BINDING_ID = "easyfamily";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_EASYE4 = new ThingTypeUID(BINDING_ID, "easyE4");

    // List of all config parameters
    public static final String CONFIG_IP_ADDRESS = "IP Address";
    public static final String CONFIG_PORT = "Port";
    public static final String CONFIG_USER = "User";
    public static final String CONFIG_PASSWORD = "Password";
    public static final String CONFIG_ENCRYPTION = "SSL/TLS Encryption";
    public static final String CONFIG_APIKEY = "API Key";

    // List of all config ids
    public static final String CONFIG_ID_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_ID_PORT = "port";
    public static final String CONFIG_ID_USER = "user";
    public static final String CONFIG_ID_PASSWORD = "password";
    public static final String CONFIG_ID_ENCRYPTION = "encryption";
    public static final String CONFIG_ID_APIKEY = "apiKey";

    // List of all Channel ids
    public static final String CHANNEL_ID_STATE = "programState";
    public static final String CHANNEL_ID_IOX = "ioxState";
    public static final String CHANNEL_ID_INPUTS = "inputs";
    public static final String CHANNEL_ID_OUTPUTS = "outputs";
    public static final String CHANNEL_ID_MARKERS = "markers";
    public static final String CHANNEL_ID_MARKER_BYTES = "markerBytes";
    public static final String CHANNEL_ID_MARKER_WORDS = "markerWords";
    public static final String CHANNEL_ID_MARKER_DWORDS = "markerDoubleWords";
    public static final String CHANNEL_ID_NET_MARKERS = "netMarkers";
    public static final String CHANNEL_ID_NET_MARKER_BYTES = "netMarkerBytes";
    public static final String CHANNEL_ID_NET_MARKER_WORDS = "netMarkerWords";
    public static final String CHANNEL_ID_NET_MARKER_DWORDS = "netMarkerDoubleWords";
    public static final String CHANNEL_ID_ANALOG_INPUTS = "analogInputs";
    public static final String CHANNEL_ID_ANALOG_OUTPUTS = "analogOutputs";

    // List of all Operands
    public static final String CHANNEL_STATE = "programState";
    public static final String CHANNEL_IOX = "BUSState";
    public static final String CHANNEL_INPUTS = "I";
    public static final String CHANNEL_OUTPUTS = "Q";
    public static final String CHANNEL_MARKERS = "M";
    public static final String CHANNEL_MARKER_BYTES = "MB";
    public static final String CHANNEL_MARKER_WORDS = "MW";
    public static final String CHANNEL_MARKER_DWORDS = "MD";
    public static final String CHANNEL_NET_MARKERS = "N";
    public static final String CHANNEL_NET_MARKER_BYTES = "NB";
    public static final String CHANNEL_NET_MARKER_WORDS = "NW";
    public static final String CHANNEL_NET_MARKER_DWORDS = "ND";
    public static final String CHANNEL_ANALOG_INPUTS = "IA";
    public static final String CHANNEL_ANALOG_OUTPUTS = "IQ";

    // Marker ranges
    public static final short MARKER_RANGE_UPPER_LIMIT = 512;
    public static final short MARKER_RANGE_LOWER_LIMIT = 1;
    public static final short NET_MARKER_RANGE_UPPER_LIMIT = 32;
    public static final short NET_MARKER_RANGE_LOWER_LIMIT = 1;

    // List of all mode types
    public static final String MODE_RUN = "RUN";
    public static final String MODE_STOP = "STOP";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    public static final Map<String, String> CHANNEL_MAPPING = new HashMap<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_EASYE4);

        CHANNEL_MAPPING.put(CHANNEL_STATE, CHANNEL_ID_STATE);
        CHANNEL_MAPPING.put(CHANNEL_IOX, CHANNEL_ID_IOX);
        CHANNEL_MAPPING.put(CHANNEL_INPUTS, CHANNEL_ID_INPUTS);
        CHANNEL_MAPPING.put(CHANNEL_OUTPUTS, CHANNEL_ID_OUTPUTS);
        CHANNEL_MAPPING.put(CHANNEL_ANALOG_INPUTS, CHANNEL_ID_ANALOG_INPUTS);
        CHANNEL_MAPPING.put(CHANNEL_ANALOG_OUTPUTS, CHANNEL_ID_ANALOG_OUTPUTS);
        CHANNEL_MAPPING.put(CHANNEL_MARKERS, CHANNEL_ID_MARKERS);
        CHANNEL_MAPPING.put(CHANNEL_MARKER_BYTES, CHANNEL_ID_MARKER_BYTES);
        CHANNEL_MAPPING.put(CHANNEL_MARKER_WORDS, CHANNEL_ID_MARKER_WORDS);
        CHANNEL_MAPPING.put(CHANNEL_MARKER_DWORDS, CHANNEL_ID_MARKER_DWORDS);
        CHANNEL_MAPPING.put(CHANNEL_NET_MARKERS, CHANNEL_ID_NET_MARKERS);
        CHANNEL_MAPPING.put(CHANNEL_NET_MARKER_BYTES, CHANNEL_ID_NET_MARKER_BYTES);
        CHANNEL_MAPPING.put(CHANNEL_NET_MARKER_WORDS, CHANNEL_ID_NET_MARKER_WORDS);
        CHANNEL_MAPPING.put(CHANNEL_NET_MARKER_DWORDS, CHANNEL_ID_NET_MARKER_DWORDS);
    }
}
