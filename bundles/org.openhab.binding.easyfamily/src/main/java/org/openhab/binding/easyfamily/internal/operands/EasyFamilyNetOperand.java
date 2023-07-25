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
public abstract class EasyFamilyNetOperand extends EasyFamilyWriteableOperand {

    public final int netID;

    EasyFamilyNetOperand(String type, int number, int netID, ChannelUID uid) {
        super(type, number, uid);
        this.netID = netID;
    }

    protected abstract String setPath();

    protected abstract String setQuery();
}
