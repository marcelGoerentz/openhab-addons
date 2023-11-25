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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Operands {
    @SerializedName("MWRANGE")
    public List<MWRange> mwRange = new ArrayList<>();

    @SerializedName("NWRANGE")
    public List<NWRange> nwRange = new ArrayList<>();

    @SerializedName("IALL")
    public String iAll = "";

    @SerializedName("OALL")
    public String oAll = "";

    @SerializedName("AIALL")
    public String aIAll = "";

    @SerializedName("AOALL")
    public String aOAll = "";
}
