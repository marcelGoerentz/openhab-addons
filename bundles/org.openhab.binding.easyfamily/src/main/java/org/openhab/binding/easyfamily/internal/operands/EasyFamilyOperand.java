/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * The {@link EasyFamilyFunctionBlocks} is an abstract class for representing the available function blocks on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyOperand {

    public final ChannelUID uid;
    public int value = 0;
    public final int number;
    public final String type;

    EasyFamilyOperand(String type, int number, ChannelUID uid) {
        this.type = type;
        this.number = number;
        this.uid = uid;
        this.value = 0;
    }

    /**
     * Determine the State for the corresponding channel
     * 
     * @param value = value of the operand
     * @param onOffType , when True then OnOffType else it will be a Decimal Type
     * @return return the State with the correct Type
     */
    public State getState(boolean onOffType) {
        if (onOffType) {
            if (this.value == 0) {
                return OnOffType.OFF;
            } else {
                return OnOffType.ON;
            }
        } else {
            return new DecimalType(this.value);
        }
    }

    public String getChannelID() {
        return this.uid.getId();
    }
}
