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
 * The {@link EasyFamilyNetOperand} is an abstract class for representing the available NET operands on a
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyNetOperand extends EasyFamilyWriteableOperand {

    public final int netID;
    public final int divisor;

    EasyFamilyNetOperand(String channelUID, int number, int netID, ChannelUID uid, int divisor) {
        super(channelUID, number, uid);
        this.netID = netID;
        this.divisor = divisor;
    }

    @Override
    public void setQueries(EasyDevice device) {
        device.queries.queryForNetMarkers = true;
        if (this instanceof EasyFamilyNetMarkerDWord) {
            device.queries.setNetMarkerRanger((short) (this.number * this.divisor));
        } else {
            device.queries.setNetMarkerRanger((short) (this.number / this.divisor));
        }
    }
}
