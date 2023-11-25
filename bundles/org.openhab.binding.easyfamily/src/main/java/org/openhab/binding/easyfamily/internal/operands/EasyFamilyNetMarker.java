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
 * The {@link EasyFamilyNetMarker} is an abstract class for representing the available function blocks on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyNetMarker extends EasyFamilyNetOperand {

    EasyFamilyNetMarker(int number, int netID, ChannelUID uid) {
        super(CHANNEL_ID_NET_MARKERS, number, netID, uid, 16);
    }

    @Override
    public String setPath() {
        return "/api/set/op";
    }

    @Override
    public String setQuery() {
        return "?op=N&index=" + number + "&netid=" + (netID - 1) + "&val=";
    }

    @Override
    public String getAcceptedItemType() {
        return "Switch";
    }

    @Override
    public int getMaximumInstance() {
        return MAX_MARKER_INSTANCE;
    }
}
