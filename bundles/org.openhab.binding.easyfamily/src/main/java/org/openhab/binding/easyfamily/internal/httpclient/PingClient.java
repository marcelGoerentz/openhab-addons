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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PingClient} class will ping a client.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class PingClient {

    private final Logger logger = LoggerFactory.getLogger(PingClient.class);

    public boolean pingClient(String ip) {
        try {
            List<String> commands = new ArrayList<>();
            commands.add("ping");
            commands.add("-w");
            commands.add("3");
            commands.add(ip);
            String s = runCommand(commands);
            return (s.contains("ttl") || s.contains("TTL"));
        } catch (Exception e) {
            logger.debug("This exception occurred when trying to ping the client: {}", e.getMessage());
        }
        return false;
    }

    private static String runCommand(List<String> commands) throws IOException {
        Process p = new ProcessBuilder(commands).start();
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
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
        return s;
    }
}
