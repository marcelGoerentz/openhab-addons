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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.EasyDevice;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyNullOperand} is representing a null object
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyNullOperand extends EasyFamilyOperand {

    public EasyFamilyNullOperand(String channelUID, Integer number, ChannelUID uid) {
        super(channelUID, number, uid);
    }

    @Override
    public String getAcceptedItemType() {
        return "";
    }

    @Override
    public int getMaximumInstance() {
        return 0;
    }

    @Override
    public void setQueries(EasyDevice device) {
        return;
    }
}
