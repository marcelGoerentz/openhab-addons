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
package org.openhab.binding.easyfamily.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link EasyFamilyConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyConfiguration {

    // Connection fields by parameters
    public String ipAddress = "";
    public Integer port = 0;
    public Boolean encryption = false;
    public Integer connectionTimeOut = 5000;
    public Integer pollingInterval = 1000;

    // Authentification fields
    public String apiKey = "";
    public String user = "";
    public String password = "";
    public String credentials = "";

    // Commentary list field
    public Boolean loadCommentaryList = false;

    public JsonObject getAsJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("IP", this.ipAddress);
        jsonObject.addProperty("PORT", this.port);
        jsonObject.addProperty("ENCRYPTION", this.encryption);
        jsonObject.addProperty("APIKEY", this.apiKey);
        jsonObject.addProperty("USER", this.user);
        jsonObject.addProperty("PASSWORD", this.password);
        jsonObject.addProperty("TIMEOUT", this.connectionTimeOut);
        jsonObject.addProperty("POLLING", this.pollingInterval);
        return jsonObject;
    }
}
