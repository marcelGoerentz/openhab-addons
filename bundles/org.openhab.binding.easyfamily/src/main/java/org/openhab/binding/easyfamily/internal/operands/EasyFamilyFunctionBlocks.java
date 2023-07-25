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
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyFunctionBlocks} is an abstract class for representing the available function blocks on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyFunctionBlocks extends EasyFamilyWriteableOperand {

    public final int numberOfWordPins;
    public final int numberOfBoolPins;

    EasyFamilyFunctionBlocks(String type, int number, ChannelUID uid) {
        super(type, number, uid);
        this.numberOfWordPins = setNumberOfWordPins();
        this.numberOfBoolPins = setNumberOfBoolPins();
    }

    protected abstract int setNumberOfWordPins();

    public int getNumberOfWordPins() {
        return this.numberOfWordPins;
    }

    protected abstract int setNumberOfBoolPins();

    public int getNumberOfBoolPins() {
        return this.numberOfWordPins;
    }
}
