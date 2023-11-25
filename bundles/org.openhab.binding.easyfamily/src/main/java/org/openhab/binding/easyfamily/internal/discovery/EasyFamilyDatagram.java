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
package org.openhab.binding.easyfamily.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EasyFamilyDatagram} is responsible for analyzing the received datagramms
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyDatagram {

    public final byte[] telegram = new byte[14];
    public boolean success = false;

    /**
     *
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
        setGroup(); // all groups
        setNetID(); // all NET-IDs
    }

    /**
     * Methods sets the identifier
     */
    private void setIdentifier() {
        telegram[0] = 0x54;
        telegram[1] = (byte) 0xEA;
        telegram[2] = 14 - 4;
    }

    /**
     * Methods sets the telegram type
     */
    private void setType() {
        telegram[3] = 0;
        telegram[4] = 0; // Telegram type
        telegram[5] = 0;
    }

    /**
     * Methods sets the ip address
     * 
     * @param ip as byte array
     */
    private void setIP(byte[] ip) {
        telegram[6] = ip[0];// own IP
        telegram[7] = ip[1];// own IP
        telegram[8] = ip[2];// own IP
        telegram[9] = ip[3];// own IP
    }

    /**
     * Method sets the port
     *
     */
    private void setPort(int port) {
        telegram[10] = (byte) (port & 0xff);
        telegram[11] = (byte) ((port >> 8) & 0xff);
    }

    /**
     * Method sets the group
     *
     */
    private void setGroup() {
        telegram[12] = (byte) -1;
    }

    /**
     * Methods sets the net ID
     *
     */
    private void setNetID() {
        telegram[13] = (byte) -1;
    }
}
