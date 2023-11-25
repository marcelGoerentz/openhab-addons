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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.ITEM_TYPE_NUMBER;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.MAX_ANALOG_IO_INSTANCE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyAnalogIOOperand}
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyAnalogIOOperand extends EasyFamilyIOOperand {

    EasyFamilyAnalogIOOperand(String channelUID, int number, ChannelUID uid) {
        super(channelUID, number, uid);
    }

    @Override
    public String getAcceptedItemType() {
        return ITEM_TYPE_NUMBER;
    }

    @Override
    public int getMaximumInstance() {
        return MAX_ANALOG_IO_INSTANCE;
    }
}
