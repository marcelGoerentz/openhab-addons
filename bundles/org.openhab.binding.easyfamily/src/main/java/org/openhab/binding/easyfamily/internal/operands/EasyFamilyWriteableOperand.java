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
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyWriteableOperand} is an abstract class for representing the writeable operands of a
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EasyFamilyWriteableOperand extends EasyFamilyOperand implements WebAccess {

    private final String path;
    private final String query;

    EasyFamilyWriteableOperand(String channelUID, int number, ChannelUID uid) {
        super(channelUID, number, uid);
        this.path = setPath();
        this.query = setQuery();
    }

    public String getPath() {
        return this.path;
    }

    public String getQuery() {
        return this.query;
    }
}
