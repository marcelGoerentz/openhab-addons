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
package org.openhab.binding.easyfamily.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EasyFamilyDiscoveryService} is responsible for parsing the udp response
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyScanResponse {
    public String requestIp = "";
    public String deviceIp = "";
    public String deviceMask = "";
    public String deviceGw = "";
    public String deviceDNS = "";
    // public int deviceType;
    // public int deviceDevState;
    public String deviceMAC = "";
    // public int deviceNetID;
    // public int deviceNetIDUse;
    // public int deviceNetGroup;
    // public int deviceBusDelay;
    // public int deviceIPMode;
    // public boolean deviceConfigProtection;
    // public boolean deviceEncryptionNET;
    // public boolean deviceEncryptionTCP;
    public boolean deviceHTTPActive;
    // public boolean deviceRemoteRun;
    // public boolean deviceUDPRDEnable;
    public boolean deviceSSLEnable;
    // public int deviceEasyComPort;
    // public int deviceTimeSync;
    public int deviceHTTPPort;
    public String deviceName = "";
    public String deviceDomainName = "";
    public final boolean deviceValid;

    private final byte[] rawData;

    /**
     * This method parse the responded udp packet
     * 
     * @param b byte array of the response
     * @throws UnknownHostException
     */
    public EasyFamilyScanResponse(byte[] b) {
        this.rawData = b;
        this.deviceValid = checkForValidInput();
        if (this.deviceValid) {
            setAddresses();
            setBitValues();
            setPorts();
            this.deviceName = setDeviceName();
            this.deviceDomainName = setDeviceDomainName();
        }
    }

    /**
     * 
     */
    private void setAddresses() {
        this.requestIp = buildAddressString(new byte[] { rawData[8], rawData[9], rawData[10], rawData[11] });
        this.deviceMAC = setMacAddress();
        this.deviceIp = buildAddressString(new byte[] { rawData[22], rawData[23], rawData[24], rawData[25] });
        this.deviceMask = buildAddressString(new byte[] { rawData[29], rawData[28], rawData[27], rawData[26] });
        this.deviceMask = buildAddressString(new byte[] { rawData[33], rawData[32], rawData[31], rawData[30] });
        this.deviceDNS = buildAddressString(new byte[] { rawData[37], rawData[36], rawData[35], rawData[34] });
    }

    /**
     * 
     */
    private void setBitValues() {
        // this.deviceConfigProtection = ((b[42] >> 2) & 0x01) == 1;
        // this.deviceEncryptionNET = ((b[42] >> 3) & 0x01) == 1;
        this.deviceHTTPActive = ((rawData[42] >> 4) & 0x01) == 1;
        // this.deviceRemoteRun = ((b[42] >> 5) & 0x01) == 1;
        // this.deviceEncryptionTCP = ((b[42] >> 6) & 0x01) == 1;
        // this.deviceUDPRDEnable = ((b[42] >> 7) & 0x01) == 1;
        this.deviceSSLEnable = (rawData[43] & 0x01) == 1;
    }

    /**
     * 
     */
    /*
     * private void setByteValues() {
     * // this.deviceDevState = b[20];
     * // this.deviceNetGroup = b[38] & 0xff;
     * // this.deviceNetID = b[39] & 0xff;
     * // this.deviceNetIDUse = b[40] & 0xff;
     * // this.deviceBusDelay = b[41] & 0xff;
     * // this.deviceIPMode = b[42] & 0x03;
     * }
     */

    /**
     * 
     */
    private void setPorts() {
        // this.deviceEasyComPort = (b[44] & 0xff) + (b[45] & 0xff) * 256;
        this.deviceHTTPPort = (rawData[46] & 0xff) + (rawData[47] & 0xff) * 256;
    }

    /**
     * This Method checks whether the response is valid or not
     * 
     * @return true if this is a valid response from a device, false when not
     */
    private boolean checkForValidInput() {
        if (rawData.length > 5) {
            int t = (rawData[1] & 0xff) << 8 | (rawData[0] & 0xff);
            if (t == 0xEA54) {
                return rawData[4] == 1;
            }
        }
        return false;
    }

    /**
     * This method parses the device name from the response
     * 
     * @return Device name as a string
     */
    private String setDeviceName() {
        StringBuilder stringBuilder = new StringBuilder();
        char c = '\0';
        for (int i = 0; i < 24; i++) {
            if ((i == 0) || (c != '\0')) {
                c = (char) rawData[48 + i];
            }
            if (c != '\0') {
                stringBuilder.append(c);
            } else {
                i = 24;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 
     * @return Domain name as a string
     */
    private String setDeviceDomainName() {
        StringBuilder stringBuilder = new StringBuilder();
        char c = '\0';
        for (int i = 0; i < 64; i++) {
            if ((i == 0) || (c != '\0')) {
                c = (char) rawData[48 + 24 + i];
            }
            if (c != '\0') {
                stringBuilder.append(c);
            } else {
                i = 64;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 
     * @return MAC address as a string
     */
    private String setMacAddress() {
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            mac.append(String.format("%02x", rawData[i + 14]));
            if (i < 5) {
                mac.append("-");
            }
        }
        return mac.toString().toUpperCase();
    }

    /**
     * 
     * @param b Byte array that will be transformed into a IP address
     * @return IP address as a string
     */
    private String buildAddressString(byte[] b) {
        StringBuilder address = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            address.append(b[i] & 0xff);
            if (i < 3) {
                address.append(".");
            }
        }
        return address.toString();
    }
}
