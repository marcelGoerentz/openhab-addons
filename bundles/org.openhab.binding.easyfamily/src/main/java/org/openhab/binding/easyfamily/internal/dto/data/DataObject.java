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
package org.openhab.binding.easyfamily.internal.dto.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.EasyFamilyHandler;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyNullOperand;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class DataObject {
    protected String type;
    public final int number;
    public final int value;
    public final int netIndex;

    DataObject(String type, int number, int value, int netIndex) {
        this.type = type;
        this.number = number;
        this.value = value;
        this.netIndex = netIndex;
    }

    public String getChannelID() {
        StringBuilder channelID = new StringBuilder();
        if (this.netIndex > 0) {
            channelID.append(this.netIndex);
        }
        channelID.append(this.type);
        if (this.number < 10) {
            channelID.append(0);
        }
        return channelID.append(this.number).toString();
    }

    public void updateOperandValue(EasyFamilyHandler handler) {
        var operand = handler.getOperandByChannelID(getChannelID());
        if (!(operand instanceof EasyFamilyNullOperand)) {
            if (operand.updateValue(value)) {
                handler.updateChannelState(operand.getUid(), operand.getState());
            }
        }
    }
}
