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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.*;

import java.lang.reflect.Field;
import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easyfamily.internal.dto.json.login.Sysinfo;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyNullOperand;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyOperand;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyOperandFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EasyDevice} is responsible sett,
 * which are sent to one of the channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyDevice {

    private final Logger logger = LoggerFactory.getLogger(EasyDevice.class);

    protected final BundleContext bundleContext = FrameworkUtil.getBundle(EasyFamilyHandler.class).getBundleContext();
    public final Queries queries = new Queries();

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    public int webServerPort;
    protected Boolean isInNETGroup = false;
    protected Boolean enableIORead = false;
    protected Boolean accessMode = false;
    protected short deviceMarkerRangeLower = 0;
    protected short deviceMarkerRangeUpper = 0;
    protected short deviceNetMarkerRangeLower = 0;
    protected short deviceNetMarkerRangeUpper = 0;

    protected int statusCounter = 0;
    protected float statusRef = 0;

    protected final HashMap<String, @Nullable EasyFamilyOperand> operands = new HashMap<>();

    /**
     *
     */
    public EasyDevice(TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    /**
     * This method creates a Hash Map where the requested Operands where saved
     * and determines the query range for markers and NET-Markers
     */
    public List<Channel> createMap(Thing thing) {
        List<Channel> wrongIDList = new ArrayList<>();
        // get a list of all the channels bound to the thing
        List<Channel> channelList = thing.getChannels();
        // iterate through the list and add them to them map
        addOperandsFromChannelList(channelList, wrongIDList);
        setQueries();
        return wrongIDList;
    }

    public void addOperandsFromChannelList(List<Channel> channels) {
        EasyFamilyOperandFactory operandFactory = new EasyFamilyOperandFactory();
        for (Channel channel : channels) {
            EasyFamilyOperand operand = operandFactory.getOperand(channel.getUID());
            if (!(operand instanceof EasyFamilyNullOperand) && !operands.containsValue(operand)) {
                operands.put(operand.getChannelID(), operand);
            }
        }
    }

    public void addOperandsFromChannelList(List<Channel> channels, List<Channel> wrongIDList) {
        var operandFactory = new EasyFamilyOperandFactory();
        for (Channel channel : channels) {
            var operand = operandFactory.getOperand(channel.getUID());
            if (!(operand instanceof EasyFamilyNullOperand)) {
                if (!operands.containsValue(operand)) {
                    operands.put(operand.getChannelID(), operand);
                } else {
                    wrongIDList.add(channel);
                }
            } else {
                wrongIDList.add(channel);
            }
        }
    }

    /**
     * 
     */
    public void setQueries() {
        for (EasyFamilyOperand operand : operands.values()) {
            if (operand != null) {
                operand.setQueries(this);
            }
        }
    }

    /**
     * Method to set device properties
     */
    public Map<String, String> setDeviceProperties(Sysinfo sysinfo) {
        // Create HashMap for the properties
        Map<String, String> propertyMap = new HashMap<>();
        // Load translated strings
        PropertyStrings locals = new PropertyStrings(this.i18nProvider, this.bundleContext);
        propertyMap.put(locals.ipAddressProp, sysinfo.ipSet.actIP);
        propertyMap.put(locals.macAddress, sysinfo.mac.toUpperCase().replace("-", ":"));
        propertyMap.put(locals.netID, Integer.toString(sysinfo.netID));
        propertyMap.put(locals.vendor, "Eaton");
        propertyMap.put(locals.firmware, sysinfo.vers);
        propertyMap.put(locals.build, sysinfo.build);
        propertyMap.put(locals.btlVersion, sysinfo.btlVersion);
        propertyMap.put(locals.type, sysinfo.type);
        propertyMap.put(locals.serialNumber, sysinfo.serialNumber);
        propertyMap.put(locals.dateOfManufacture, sysinfo.dateOfManufacture);
        propertyMap.put(locals.deviceName, sysinfo.devName);
        if (!sysinfo.ethernetDomain.isEmpty()) {
            propertyMap.put(locals.domainName, sysinfo.ethernetDomain);
        }
        if (!sysinfo.progname.isEmpty()) {
            propertyMap.put(locals.programName, sysinfo.progname);
        }
        return propertyMap;
    }

    /**
     * Class to organize the localized property strings
     */
    public class PropertyStrings {

        public String macAddress = "";
        public String firmware = "";
        public String build = "";
        public String btlVersion = "";
        public String type = "";
        public String serialNumber = "";
        public String dateOfManufacture = "";
        public String ipAddressProp = "";
        public String deviceName = "";
        public String domainName = "";
        public String programName = "";
        public String vendor = "";
        public String netID = "";

        /**
         * Get the localized property strings
         */
        public PropertyStrings(TranslationProvider i18nProvider, BundleContext bundleContext) {
            Field[] fields = PropertyStrings.class.getFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String tmp = i18nProvider.getText(bundleContext.getBundle(), fieldName, PROPERTIES.get(fieldName),
                        localeProvider.getLocale());
                if (tmp != null) {
                    try {
                        field.set(this, tmp);
                    } catch (IllegalAccessException e) {
                        logger.debug("This went wrong when setting the properties: {}", e.getMessage());
                    }
                }
            }
        }
    }

    public static class Queries {
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
            }
            if (value == 0) {
                queryNetMarkerRangeLower = 1;
            } else if (value < queryNetMarkerRangeLower) {
                queryNetMarkerRangeLower = value;
            }
        }
    }
}
