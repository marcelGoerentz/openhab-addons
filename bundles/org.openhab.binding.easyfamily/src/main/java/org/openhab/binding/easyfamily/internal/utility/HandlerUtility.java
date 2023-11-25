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
package org.openhab.binding.easyfamily.internal.utility;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.URIUtil;
import org.openhab.binding.easyfamily.internal.dto.json.login.LoginResponse;

/**
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class HandlerUtility {

    public static void parseURLEncodedData(@Nullable LoginResponse login) {
        if (login != null) {
            String parsedData = URIUtil.decodePath(login.sysinfo.devName);
            if (parsedData != null) {
                login.sysinfo.devName = parsedData;
            }
            parsedData = URIUtil.decodePath(login.sysinfo.ethernetDomain);
            if (parsedData != null) {
                login.sysinfo.ethernetDomain = parsedData;
            }
        }
    }
}
