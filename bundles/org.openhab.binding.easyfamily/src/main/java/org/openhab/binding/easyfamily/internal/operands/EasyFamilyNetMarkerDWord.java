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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyNetMarkerDWord} is an abstract class for representing the available NET Marker Double Word
 * operands on a
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyNetMarkerDWord extends EasyFamilyNetOperand {

    EasyFamilyNetMarkerDWord(int number, int netID, ChannelUID uid) {
        super(CHANNEL_ID_NET_MARKER_DWORDS, number, netID, uid, 2);
    }

    @Override
    public String setPath() {
        return "/api/set/op";
    }

    @Override
    public String setQuery() {
        return "?op=ND&index=" + number + "&netid=" + (netID - 1) + "&val=";
    }

    @Override
    public int getMaximumInstance() {
        return MAX_NET_DWORD_MARKER_INSTANCE;
    }

    @Override
    public String getAcceptedItemType() {
        return ITEM_TYPE_NUMBER;
    }
}
