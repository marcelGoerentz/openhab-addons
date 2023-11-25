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
package org.openhab.binding.easyfamily.internal.dto.data;

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_WORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_WORDS;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.EasyFamilyHandler;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class MarkerWord extends Marker {
    public final List<MarkerByte> markerBytes = new ArrayList<>();

    public MarkerWord(int number, int value, int netIndex) {
        super(CHANNEL_MARKER_WORDS, number, value, netIndex);
        if (netIndex > 0) {
            this.type = CHANNEL_NET_MARKER_WORDS;
        }
        markerBytes.add(new MarkerByte(number * 2 - 1, value & 0x00FF, netIndex));
        markerBytes.add(new MarkerByte(number * 2, value >> 8, netIndex));
    }

    @Override
    public void updateOperandValue(EasyFamilyHandler handler) {
        super.updateOperandValue(handler);
        for (MarkerByte markerByte : markerBytes) {
            markerByte.updateOperandValue(handler);
        }
    }
}
