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

    // Maximum instances of operands
    public static final int MAX_DEFAULT_INSTANCE = 1;
    public static final int MAX_NET_DWORD_MARKER_INSTANCE = 16;
    public static final int MAX_NET_WORD_MARKER_INSTANCE = 32;
    public static final int MAX_ANALOG_IO_INSTANCE = 48;
    public static final int MAX_NET_BYTE_MARKER_INSTANCE = 64;
    public static final int MAX_DIGITAL_IO_INSTANCE = 128;
    public static final int MAX_DWORD_INSTANCE = 256;
    public static final int MAX_MARKER_INSTANCE = 512;

    // Accepted item types
    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_NUMBER = "Number";

    // List of all mode types
    public static final String MODE_RUN = "RUN";
    public static final String MODE_STOP = "STOP";

    // List of all Properties
    public static final String PROPERTY_MAC = "MAC address";
    public static final String PROPERTY_FW = "Firmware version";
    public static final String PROPERTY_BTL_VERSION = "Bootloader version";
    public static final String PROPERTY_TYPE = "Device type";
    public static final String PROPERTY_SERIAL = "Serialnumber";
    public static final String PROPERTY_IP = "IP address";
    public static final String PROPERTY_DEVICE_NAME = "Device name";
    public static final String PROPERTY_DOMAIN = "Domain name";
    public static final String PROPERTY_PROGRAM = "Program name";
    public static final String PROPERTY_VENDOR = "Vendor";
    public static final String PROPERTY_BUILD = "Build";
    public static final String PROPERTY_DATE_OF_MANUFACTURE = "Date of manufacture";
    public static final String PROPERTY_NET_ID = "NET ID";

    public static final Map<String, String> PROPERTIES = new HashMap<>();

    static {
        PROPERTIES.put("macAddress", PROPERTY_MAC);
        PROPERTIES.put("firmware", PROPERTY_FW);
        PROPERTIES.put("type", PROPERTY_TYPE);
        PROPERTIES.put("serialNumber", PROPERTY_SERIAL);
        PROPERTIES.put("ipAddressProp", PROPERTY_IP);
        PROPERTIES.put("deviceName", PROPERTY_DEVICE_NAME);
        PROPERTIES.put("domainName", PROPERTY_DOMAIN);
        PROPERTIES.put("programName", PROPERTY_PROGRAM);
        PROPERTIES.put("vendor", PROPERTY_VENDOR);
        PROPERTIES.put("build", PROPERTY_BUILD);
        PROPERTIES.put("dateOfManufacture", PROPERTY_DATE_OF_MANUFACTURE);
        PROPERTIES.put("btlVersion", PROPERTY_BTL_VERSION);
        PROPERTIES.put("netID", PROPERTY_NET_ID);
    }

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
