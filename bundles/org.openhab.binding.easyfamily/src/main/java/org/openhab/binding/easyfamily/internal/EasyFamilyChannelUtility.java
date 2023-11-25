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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_BYTES;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_DWORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_WORDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EasyFamilyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyChannelUtility {

    private final Logger logger = LoggerFactory.getLogger(EasyFamilyChannelUtility.class);

    public String channelType = "";
    public int channelNumber = 0;
    public int netID = 0;

    public void parseChannelInfo(String channelID) {
        this.channelType = channelID.replaceAll("[0-9]", "");
        // check if String cotains at least one Digit
        if (!(channelID.matches(".*\\d+.*"))) {
            return;
        }
        // check if channel type is part of the NET markers
        if (channelType.equals(CHANNEL_NET_MARKERS) || channelType.equals(CHANNEL_NET_MARKER_BYTES)
                || channelType.equals(CHANNEL_NET_MARKER_WORDS) || channelType.equals(CHANNEL_NET_MARKER_DWORDS)) {
            // check if there is a netID
            // if there is a netID then retrieve it
            String tmp = channelID.replaceAll("[A-Z][0-9]*", "");
            if (tmp.isEmpty()) {
                tmp = "0";
            }
            logger.debug("From: {} extracted: {}", channelID, tmp);
            this.netID = Integer.parseInt(tmp);
            // get all digits from the String
            tmp = channelID.replaceAll("[^0-9?!.]", "");
            // get the number by taking the substring of the digits
            this.channelNumber = Integer.parseInt(tmp);
        } else {
            // if not, obtain the number from the String
            this.channelNumber = Integer.parseInt(channelID.replaceAll("[^0-9?!.]", ""));
        }
    }
}
