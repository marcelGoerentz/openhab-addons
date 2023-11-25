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
package org.openhab.binding.easyfamily.internal.httpclient;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EasyFamilyHttpResponse} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyHttpResponse {

    public final String content;
    public final int httpCode;

    public EasyFamilyHttpResponse() {
        content = "";
        httpCode = 0;
    }

    public EasyFamilyHttpResponse(String content, int httpCode) {
        this.content = content;
        this.httpCode = httpCode;
    }
}
