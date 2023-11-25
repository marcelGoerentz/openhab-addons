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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_DWORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_DWORDS;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.internal.NonnullByDefault;
import org.openhab.binding.easyfamily.internal.EasyFamilyHandler;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonnullByDefault
public class MarkerDoubleWord extends Marker {
    public final List<MarkerWord> markerWords = new ArrayList<>();

    public MarkerDoubleWord(int number, int value, int netIndex) {
        super(CHANNEL_MARKER_DWORDS, number, value, netIndex);
        if (netIndex > 0) {
            this.type = CHANNEL_NET_MARKER_DWORDS;
        }
        markerWords.add(new MarkerWord(number * 2 - 1, value & 0xFFFF, netIndex));
        markerWords.add(new MarkerWord(number * 2, value >> 16, netIndex));
    }

    @Override
    public void updateOperandValue(EasyFamilyHandler handler) {
        super.updateOperandValue(handler);
        for (MarkerWord word : markerWords) {
            word.updateOperandValue(handler);
        }
    }
}
