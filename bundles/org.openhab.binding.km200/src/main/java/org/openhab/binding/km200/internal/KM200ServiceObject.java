/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * The KM200CommObject representing a service on a device with its all capabilities
 *
 * @author Markus Eckhardt - Initial contribution
 * @author Marcel Goerentz - Made immutable fields final and mutable fields volatile for safe concurrent access
 *         from the shared worker pool
 */
@NonNullByDefault
public class KM200ServiceObject {
    private final int readable;
    private final int writeable;
    private final int recordable;
    private final int virtual;
    private volatile boolean updated;
    private final @Nullable String parent;
    private final String fullServiceName;
    private final String serviceType;
    private volatile @Nullable JsonObject jsonData;
    private volatile @Nullable Object value;
    private volatile @Nullable Object valueParameter;

    /* Device services. Populated concurrently while services are being discovered, hence the concurrent map. */
    public final Map<String, KM200ServiceObject> serviceTreeMap;

    public KM200ServiceObject(String fullServiceName, String serviceType, int readable, int writeable, int recordable,
            int virtual, @Nullable String parent) {
        serviceTreeMap = new ConcurrentHashMap<>();
        this.fullServiceName = fullServiceName;
        this.serviceType = serviceType;
        this.readable = readable;
        this.writeable = writeable;
        this.recordable = recordable;
        this.virtual = virtual;
        this.parent = parent;
        updated = false;
    }

    /* Sets */
    public void setValue(Object val) {
        value = val;
    }

    public void setUpdated(boolean updt) {
        updated = updt;
    }

    public void setValueParameter(Object val) {
        valueParameter = val;
    }

    public void setJSONData(JsonObject data) {
        jsonData = data;
    }

    /* gets */
    public int getReadable() {
        return readable;
    }

    public int getWriteable() {
        return writeable;
    }

    public int getRecordable() {
        return recordable;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getFullServiceName() {
        return fullServiceName;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public @Nullable Object getValueParameter() {
        return valueParameter;
    }

    public @Nullable String getParent() {
        return parent;
    }

    public int getVirtual() {
        return virtual;
    }

    public boolean getUpdated() {
        return updated;
    }

    public @Nullable JsonObject getJSONData() {
        return jsonData;
    }
}
