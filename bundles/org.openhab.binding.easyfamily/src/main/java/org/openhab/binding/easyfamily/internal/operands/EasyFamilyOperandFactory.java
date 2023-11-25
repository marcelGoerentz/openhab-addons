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
 * The {@link EasyFamilyOperandFactory} helps creating the correct operand classes.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyOperandFactory {

    public EasyFamilyOperand getOperand(ChannelUID uid) {
        EasyFamilyChannelUtility channelUtility = new EasyFamilyChannelUtility();
        channelUtility.parseChannelInfo(uid.getId());
        int indexOfLastSeparator = uid.getAsString().lastIndexOf(":");
        String id = uid.getAsString().substring(0, indexOfLastSeparator) + ":null";
        ChannelUID nUid = new ChannelUID(id);
        EasyFamilyOperand operand = new EasyFamilyNullOperand("", 0, nUid);
        if (!channelUtility.channelType.isEmpty()) {
            int number = channelUtility.channelNumber;
            int netID = channelUtility.netID;
            switch (channelUtility.channelType) {
                case CHANNEL_STATE -> {
                    operand = new EasyFamilyProgrameState(uid);
                }
                case CHANNEL_IOX -> {
                    operand = new EasyFamilyBusState(uid);
                }
                case CHANNEL_INPUTS -> {
                    operand = new EasyFamilyInput(number, uid);
                }
                case CHANNEL_OUTPUTS -> {
                    operand = new EasyFamilyOutput(number, uid);
                }
                case CHANNEL_ANALOG_INPUTS -> {
                    operand = new EasyFamilyAnalogInput(number, uid);
                }
                case CHANNEL_ANALOG_OUTPUTS -> {
                    operand = new EasyFamilyAnalogOutput(number, uid);
                }
                case CHANNEL_MARKERS -> {
                    operand = new EasyFamilyMarker(number, uid);
                }
                case CHANNEL_MARKER_BYTES -> {
                    operand = new EasyFamilyMarkerBytes(number, uid);
                }
                case CHANNEL_MARKER_WORDS -> {
                    operand = new EasyFamilyMarkerWord(number, uid);
                }
                case CHANNEL_MARKER_DWORDS -> {
                    operand = new EasyFamilyMarkerDWord(number, uid);
                }
                case CHANNEL_NET_MARKERS -> {
                    operand = new EasyFamilyNetMarker(number, netID, uid);
                }
                case CHANNEL_NET_MARKER_BYTES -> {
                    operand = new EasyFamilyNetMarkerByte(number, netID, uid);
                }
                case CHANNEL_NET_MARKER_WORDS -> {
                    operand = new EasyFamilyNetMarkerWord(number, netID, uid);
                }
                case CHANNEL_NET_MARKER_DWORDS -> {
                    operand = new EasyFamilyNetMarkerDWord(number, netID, uid);
                }
            }
        }
        return operand;
    }
}
