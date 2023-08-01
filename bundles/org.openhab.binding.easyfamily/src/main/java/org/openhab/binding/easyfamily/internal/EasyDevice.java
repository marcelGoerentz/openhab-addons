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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ANALOG_INPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ANALOG_OUTPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_INPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_IOX;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_BYTES;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_DWORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_WORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_BYTES;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_DWORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_WORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_OUTPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_STATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easyfamily.internal.json.login.Sysinfo;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyNullOperand;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyOperand;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyOperandFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The {@link EasyDevice} is responsible sett,
 * which are sent to one of the channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyDevice {

    protected final BundleContext bundleContext = FrameworkUtil.getBundle(EasyFamilyHandler.class).getBundleContext();
    protected final Querries querries = new Querries();

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    protected Boolean isInNETGroup = false;
    protected Boolean enableIORead = false;
    protected Boolean accessMode = false;
    protected short deviceMarkerRangeLower = 0;
    protected short deviceMarkerRangeUpper = 0;
    protected short deviceNetMarkerRangeLower = 0;
    protected short deviceNetMarkerRangeUpper = 0;

    protected Boolean firstValues = true;

    protected int statusCounter = 0;
    protected float statusRef = 0;

    protected HashMap<String, EasyFamilyOperand> operands = new HashMap<>();

    /**
     *
     * @param i18nProvider
     * @param thing
     */
    public EasyDevice(TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    /**
     * This method creates a Hash Map where the requested Operands where saved
     * and determines the querry range for markers and NETmarkers
     */
    public List<Channel> createMap(Thing thing) {
        List<Channel> wrongIDList = new ArrayList<Channel>();

        EasyFamilyOperandFactory operandFactory = new EasyFamilyOperandFactory();
        // get a list of all the channels bound to the thing
        List<Channel> channelList = thing.getChannels();
        // iterate through the list and add them to them map
        for (Channel channel : channelList) {
            EasyFamilyOperand operand = operandFactory.getOperand(channel.getUID());
            if (!(operand instanceof EasyFamilyNullOperand)) {
                operands.put(operand.getChannelID(), operand);
            } else {
                wrongIDList.add(channel);
            }
        }
        setQuerries();
        return wrongIDList;
    }

    /**
     * 
     */
    private void setQuerries() {
        short v = 0;
        boolean netMarker;
        boolean state;
        for (EasyFamilyOperand operand : operands.values()) {
            netMarker = false;
            state = false;
            switch (operand.type) {
                case CHANNEL_STATE:
                case CHANNEL_IOX:
                    state = true;
                    break;
                case CHANNEL_ANALOG_INPUTS:
                case CHANNEL_ANALOG_OUTPUTS:
                case CHANNEL_INPUTS:
                case CHANNEL_OUTPUTS:
                    state = true;
                    this.querries.queryForIOs = true;
                    break;
                case CHANNEL_MARKERS:
                    this.querries.queryForMarkers = true;
                    v = (short) (operand.number / 16);
                    break;
                case CHANNEL_MARKER_BYTES:
                    this.querries.queryForMarkers = true;
                    v = (short) (operand.number / 2);
                    break;
                case CHANNEL_MARKER_WORDS:
                    this.querries.queryForMarkers = true;
                    v = (short) operand.number;
                    break;
                case CHANNEL_MARKER_DWORDS:
                    v = (short) (operand.number * 2);
                    break;
                case CHANNEL_NET_MARKERS:
                    this.querries.queryForNetMarkers = true;
                    netMarker = true;
                    v = (short) (operand.number / 16);
                    break;
                case CHANNEL_NET_MARKER_BYTES:
                    this.querries.queryForNetMarkers = true;
                    netMarker = true;
                    v = (short) (operand.number / 2);
                    break;
                case CHANNEL_NET_MARKER_WORDS:
                    this.querries.queryForNetMarkers = true;
                    netMarker = true;
                    v = (short) (operand.number);
                    break;
                case CHANNEL_NET_MARKER_DWORDS:
                    this.querries.queryForNetMarkers = true;
                    netMarker = true;
                    v = (short) (operand.number * 2);
            }
            if (!state) {
                setMarker(v, netMarker);
            }
        }
    }

    public void setMarker(short v, boolean net) {
        if (net) {
            querries.setNetMarkerRanger(v);
        } else {
            querries.setMarkerRanger(v);
        }
    }

    /**
     * Method to set device properties
     * 
     * @param jsonObject JSON from {@link #parseResponse parseResponse} method.
     */
    public Map<String, String> setDeviceProperties(Sysinfo sysinfo) {
        // Create HashMap for the properties
        Map<String, String> propertyMap = new HashMap<>();
        // Load translated strings
        PropertyStrings locals = new PropertyStrings(this.i18nProvider, this.bundleContext);
        propertyMap.put(locals.ip, sysinfo.ipSet.actIP);
        propertyMap.put(locals.mac, sysinfo.mac.toUpperCase());
        propertyMap.put(locals.vendor, "Eaton");
        propertyMap.put(locals.fw, sysinfo.vers);
        propertyMap.put(locals.type, sysinfo.type);
        propertyMap.put(locals.serialNumber, sysinfo.serialNumber);
        propertyMap.put(locals.device, sysinfo.devName);
        if (!"".equals(sysinfo.ethernetDomain)) {
            propertyMap.put(locals.domain, sysinfo.ethernetDomain);
        }
        if (!"".equals(sysinfo.progname)) {
            propertyMap.put(locals.program, sysinfo.progname);
        }
        return (propertyMap);
    }

    /**
     * Class to organize the localized property strings
     */
    public class PropertyStrings {

        String mac = "";
        String fw = "";
        String type = "";
        String serialNumber = "";
        String ip = "";
        String device = "";
        String domain = "";
        String program = "";
        String vendor = "";

        /**
         * Get the localized property strings
         */
        public PropertyStrings(TranslationProvider i18nProvider, BundleContext bundleContext) {
            @Nullable
            String tmp = i18nProvider.getText(bundleContext.getBundle(), "macAddress", "MAC address",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.mac = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "firmware", "Firmware version",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.fw = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "type", "device type", localeProvider.getLocale());
            if (tmp != null) {
                this.type = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "serialnumber", "Serialnumber",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.serialNumber = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "ipAddressProp", "IP address",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.ip = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "deviceName", "Device name",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.device = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "domainName", "Domain name",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.domain = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "programName", "Program name",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.program = tmp;
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "vendor", "Vendor", localeProvider.getLocale());
            if (tmp != null) {
                this.vendor = tmp;
            }
        }
    }

    public class Querries {

        // public int[] queryMarkerRange = { 512, 0 };
        // public int[] queryNetMarkerRange = { 32, 0 };
        public Boolean queryForIOs = false;
        public Boolean queryForMarkers = false;
        public Boolean queryForNetMarkers = false;

        public short queryMarkerRangeUpper = 0;
        public short queryMarkerRangeLower = 512;
        public short queryNetMarkerRangeUpper = 0;
        public short queryNetMarkerRangeLower = 32;

        public void setMarkerRanger(short value) {
            if (value > queryMarkerRangeUpper) {
                queryMarkerRangeUpper = value;
            } else if (value == 0) {
                queryMarkerRangeUpper = 1;
            }
            if (value == 0) {
                queryMarkerRangeLower = 1;
            } else if (value < queryMarkerRangeLower) {
                queryMarkerRangeLower = value;
            }
        }

        public void setNetMarkerRanger(short value) {
            if (value > queryNetMarkerRangeUpper) {
                queryNetMarkerRangeUpper = value;
            } else if (value == 0) {
                queryNetMarkerRangeUpper = 1;
            }
            if (value == 0) {
                queryNetMarkerRangeLower = 1;
            } else if (value < queryNetMarkerRangeLower) {
                queryNetMarkerRangeLower = value;
            }
        }
    }
}
