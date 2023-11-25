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
package org.openhab.binding.easyfamily.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

    // Commentary list field
    public Boolean loadCommentaryList = false;
}
