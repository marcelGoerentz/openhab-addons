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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_MARKER_WORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.ITEM_TYPE_NUMBER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyMarkerWord} is an abstract class for representing the available Marker Word operands on a
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyMarkerWord extends EasyFamilyMarkerArithmeticOperand {

    EasyFamilyMarkerWord(int number, ChannelUID uid) {
        super(CHANNEL_ID_MARKER_WORDS, number, uid, 1);
    }

    @Override
    public String getAcceptedItemType() {
        return ITEM_TYPE_NUMBER;
    }

    @Override
    public String setPath() {
        return "/api/set/op";
    }

    @Override
    public String setQuery() {
        return "?op=MW&index=" + number + "&val=";
    }
}
