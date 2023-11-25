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
import org.openhab.binding.easyfamily.internal.EasyDevice;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link EasyFamilyProgrameState} is an abstract class for representing the program state of the
 * device
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyProgrameState extends EasyFamilyWriteableOperand {

    EasyFamilyProgrameState(ChannelUID uid) {
        super(CHANNEL_ID_STATE, 0, uid);
        setPath();
        setQuery();
    }

    @Override
    public String setPath() {
        return "/api/set/mode";
    }

    @Override
    public String setQuery() {
        return "?op=state&v1=";
    }

    public String getAcceptedItemType() {
        return ITEM_TYPE_SWITCH;
    }

    @Override
    public int getMaximumInstance() {
        return MAX_DEFAULT_INSTANCE;
    }

    @Override
    public void setQueries(EasyDevice device) {
        return;
    }
}
