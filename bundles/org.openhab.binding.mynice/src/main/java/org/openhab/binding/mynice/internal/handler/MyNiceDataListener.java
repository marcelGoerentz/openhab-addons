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
package org.openhab.binding.mynice.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.xml.dto.Device;

/**
 * The {@link MyNiceDataListener} is notified by the bridge thing handler with updated data from
 * the IP4Wifi.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface MyNiceDataListener {

    public void onDataFetched(List<Device> devices);
}