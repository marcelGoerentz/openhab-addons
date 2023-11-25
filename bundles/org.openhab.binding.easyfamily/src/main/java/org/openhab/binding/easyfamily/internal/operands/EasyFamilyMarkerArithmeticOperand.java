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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.MAX_MARKER_INSTANCE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyMarkerArithmeticOperand} is an abstract class for representing the available operands on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyMarkerArithmeticOperand extends EasyFamilyMarkerOperand {

    EasyFamilyMarkerArithmeticOperand(String channelUID, int number, ChannelUID uid, int divisor) {
        super(channelUID, number, uid, divisor);
    }

    @Override
    public int getMaximumInstance() {
        return MAX_MARKER_INSTANCE;
    }
}
