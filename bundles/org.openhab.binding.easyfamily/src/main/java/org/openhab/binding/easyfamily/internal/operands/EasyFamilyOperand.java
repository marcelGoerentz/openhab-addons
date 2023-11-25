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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.BINDING_ID;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.ITEM_TYPE_SWITCH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easyfamily.internal.EasyDevice;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * The {@link EasyFamilyOperand} is an abstract class for representing the available operands on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyOperand implements OperandInterface {

    private final ChannelUID uid;
    private final String channelID;
    public int value;
    protected final int number;
    private final String channelType;

    EasyFamilyOperand(String channelType, int number, ChannelUID uid) {
        this.number = number;
        this.channelID = uid.getId();
        this.uid = uid;
        this.channelType = channelType;
        this.value = 0;
    }

    public int getNumber() {
        return number;
    }

    public abstract void setQueries(EasyDevice device);

    /**
     * Determine the State for the corresponding channel
     *
     * @return return the State with the correct Type
     */
    public State getState() {
        if (ITEM_TYPE_SWITCH.equals(getAcceptedItemType())) {
            return value == 0 ? OnOffType.OFF : OnOffType.ON;
        } else {
            return new DecimalType(this.value);
        }
    }

    public String getChannelID() {
        return this.channelID;
    }

    public ChannelUID getUid() {
        return uid;
    }

    @Nullable
    public Channel createChannelFromOperand(String name) {
        if (!(this instanceof EasyFamilyNullOperand)) {
            ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, channelType);
            String acceptedItemType = getAcceptedItemType();
            if (!acceptedItemType.isEmpty()) {
                return ChannelBuilder.create(uid, getAcceptedItemType()).withLabel(name).withType(typeUID).build();
            }
        }
        return null;
    }

    public boolean updateValue(int value) {
        if (this.value != value) {
            this.value = value;
            return true;
        }
        return false;
    }
}
