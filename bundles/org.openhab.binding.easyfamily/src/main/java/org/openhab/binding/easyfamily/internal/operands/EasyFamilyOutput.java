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
 * The {@link EasyFamilyOutput} is an abstract class for representing the outputs on the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyOutput extends EasyFamilyDigitalIOOperand {

    EasyFamilyOutput(int number, ChannelUID uid) {
        super(CHANNEL_ID_OUTPUTS, number, uid);
    }

    public String getAcceptedItemType() {
        return "Switch";
    }

    public int getMaximumInstance() {
        return MAX_DIGITAL_IO_INSTANCE;
    }
}
