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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_BYTES;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_BYTES;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.EasyFamilyHandler;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public final class MarkerByte extends Marker {
    public final List<MarkerBit> markerBits = new ArrayList<>();

    public MarkerByte(int number, int value, int netIndex) {
        super(CHANNEL_MARKER_BYTES, number, value, netIndex);
        if (netIndex > 0) {
            this.type = CHANNEL_NET_MARKER_BYTES;
        }
        for (byte i = 0; i < 8; i++) {
            int markerValue = value % 2;
            this.markerBits.add(new MarkerBit((i + 1) + ((number - 1) * 8), markerValue, netIndex));
            value = value / 2;
        }
    }

    @Override
    public void updateOperandValue(EasyFamilyHandler handler) {
        super.updateOperandValue(handler);
        for (MarkerBit bit : markerBits) {
            bit.updateOperandValue(handler);
        }
    }
}
