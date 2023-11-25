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
package org.openhab.binding.easyfamily.internal.dto.json.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easyfamily.internal.dto.json.login.Sysinfo;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class DataResponse {
    @SerializedName("SYSINFO")
    public Sysinfo sysinfo = new Sysinfo();

    @SerializedName("OPERANDS")
    public Operands operands = new Operands();
}
