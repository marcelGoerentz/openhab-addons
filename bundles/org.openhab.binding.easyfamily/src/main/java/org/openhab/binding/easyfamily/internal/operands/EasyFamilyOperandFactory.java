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
package org.openhab.binding.easyfamily.internal.operands;

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.EasyFamilyChannelUtility;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyFunctionBlocks} is an abstract class for representing the available function blocks on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyOperandFactory {

    public EasyFamilyOperand getOperand(ChannelUID uid) {
        EasyFamilyChannelUtility channelUtility = new EasyFamilyChannelUtility();
        channelUtility.parseChannelInfo(uid.getId());
        if (!channelUtility.channelType.equals("")) {
            int number = channelUtility.channelNumber;
            int netID = channelUtility.netID;
            switch (channelUtility.channelType) {
                case CHANNEL_STATE:
                    return new EasyFamilyProgrameState(uid);
                case CHANNEL_IOX:
                    return new EasyFamilyBusState(uid);
                case CHANNEL_INPUTS:
                    return new EasyFamilyInput(number, uid);
                case CHANNEL_OUTPUTS:
                    return new EasyFamilyOutput(number, uid);
                case CHANNEL_ANALOG_INPUTS:
                    return new EasyFamilyAnalogInput(number, uid);
                case CHANNEL_ANALOG_OUTPUTS:
                    return new EasyFamilyAnalogOutput(number, uid);
                case CHANNEL_MARKERS:
                    return new EasyFamilyMarker(number, uid);
                case CHANNEL_MARKER_BYTES:
                    return new EasyFamilyMarkerBytes(number, uid);
                case CHANNEL_MARKER_WORDS:
                    return new EasyFamilyMarkerWords(number, uid);
                case CHANNEL_MARKER_DWORDS:
                    return new EasyFamilyMarkerDWords(number, uid);
                case CHANNEL_NET_MARKERS:
                    return new EasyFamilyMarkerWords(number, uid);
                case CHANNEL_NET_MARKER_BYTES:
                    return new EasyFamilyNetMarkerByte(number, netID, uid);
                case CHANNEL_NET_MARKER_WORDS:
                    return new EasyFamilyNetMarkerWord(number, netID, uid);
                case CHANNEL_NET_MARKER_DWORDS:
                    return new EasyFamilyNetMarkerDWord(number, netID, uid);
                default:
                    String id = uid.getId();
                    ChannelUID nUid = new ChannelUID(uid.toString().replace(id, "null"));
                    return new EasyFamilyNullOperand("", 0, nUid);
            }
        }
        ChannelUID nUid = new ChannelUID("");
        return new EasyFamilyNullOperand("", 0, nUid);
    }
}
