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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_IOX;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.ITEM_TYPE_SWITCH;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.MAX_DEFAULT_INSTANCE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.EasyDevice;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyBusState} is responsible representating the actual Bus State of the device as a read only
 * operand it will only indicate the state
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyBusState extends EasyFamilyReadOnlyOperand {

    EasyFamilyBusState(ChannelUID uid) {
        super(CHANNEL_ID_IOX, 0, uid);
        this.value = 0;
    }

    @Override
    public String getAcceptedItemType() {
        return ITEM_TYPE_SWITCH;
    }

    @Override
    public int getMaximumInstance() {
        return MAX_DEFAULT_INSTANCE;
    }

    @Override
    public void setQueries(EasyDevice device) {
        return;
    }
}
