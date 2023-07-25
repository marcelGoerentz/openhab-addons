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
 * The {@link EasyFamilyDatagram} is responsible for analyzing the received datagramms
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyDatagram {

    public byte[] telegramm = new byte[14];
    public boolean success = false;

    /**
     * 
     * @param ip
     * @param port
     */
    public EasyFamilyDatagram(byte[] ip, int port) {
        if (ip.length < 4 || ip.length > 5 || port > 65535) {
            return;
        }
        setData(ip, port);
        success = true;
    }

    /**
     * Methods sets all the data
     * 
     * @param ip as byte array
     * @param port as int
     */
    private void setData(byte[] ip, int port) {
        setIdentifier();
        setType();
        setIP(ip);
        setPort(port);
        setGroup((byte) 255); // all groups
        setNetID((byte) 255); // all NET-IDs
    }

    /**
     * Methods sets the identifier
     */
    private void setIdentifier() {
        telegramm[0] = 0x54;
        telegramm[1] = (byte) 0xEA;
        telegramm[2] = 14 - 4;
    }

    /**
     * Methods sets the telegram type
     */
    private void setType() {
        telegramm[3] = 0;
        telegramm[4] = 0; // Telegram type
        telegramm[5] = 0;
    }

    /**
     * Methods sets the ip address
     * 
     * @param ip as byte array
     */
    private void setIP(byte[] ip) {
        telegramm[6] = ip[0];// own IP
        telegramm[7] = ip[1];// own IP
        telegramm[8] = ip[2];// own IP
        telegramm[9] = ip[3];// own IP
    }

    /**
     * Method sets the port
     * 
     * @param port
     */
    private void setPort(int port) {
        telegramm[10] = (byte) (port & 0xff);
        telegramm[11] = (byte) ((port >> 8) & 0xff);
    }

    /**
     * Method sets the group
     * 
     * @param id
     */
    private void setGroup(byte id) {
        telegramm[12] = id;
    }

    /**
     * Methods sets the net ID
     * 
     * @param id
     */
    private void setNetID(byte id) {
        telegramm[13] = id;
    }
}
