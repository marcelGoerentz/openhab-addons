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
 * The {@link EasyFamilyInput} is a class for representing the available digital input operands on a
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyInput extends EasyFamilyDigitalIOOperand {

    EasyFamilyInput(int number, ChannelUID uid) {
        super(CHANNEL_ID_INPUTS, number, uid);
        this.value = 0;
    }

    public String getAcceptedItemType() {
        return ITEM_TYPE_SWITCH;
    }

    @Override
    public int getMaximumInstance() {
        return MAX_DIGITAL_IO_INSTANCE;
    }
}
