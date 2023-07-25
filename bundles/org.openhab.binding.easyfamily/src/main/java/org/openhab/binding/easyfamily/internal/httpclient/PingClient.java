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
package org.openhab.binding.easyfamily.internal.httpclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PingClient} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class PingClient {
    public static boolean pingClient(String ip) {
        try {
            Process p = Runtime.getRuntime().exec("ping " + ip);
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = "";
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if ((s = inputStreamReader.readLine()) != null) {
                    output.append(s);
                } else {
                    break;
                }
            }
            p.destroy();
            s = output.toString();
            if (s.contains("ttl") || s.contains("TTL")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            /* ignore */
        }
        return false;
    }
}
