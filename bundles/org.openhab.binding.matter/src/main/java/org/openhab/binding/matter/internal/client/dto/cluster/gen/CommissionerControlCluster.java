/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * CommissionerControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class CommissionerControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0751;
    public static final String CLUSTER_NAME = "CommissionerControl";
    public static final String CLUSTER_PREFIX = "commissionerControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_SUPPORTED_DEVICE_CATEGORIES = "supportedDeviceCategories";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates the device categories specified in SupportedDeviceCategoryBitmap that are supported by this
     * Commissioner Control Cluster server.
     * A client shall NOT send the RequestCommissioningApproval command if the intended node to be commissioned does not
     * conform to any of the values specified in SupportedDeviceCategories.
     */
    public SupportedDeviceCategoryBitmap supportedDeviceCategories; // 0 SupportedDeviceCategoryBitmap R M

    // Structs
    /**
     * This event shall be generated by the server following a RequestCommissioningApproval command which the server
     * responded to with SUCCESS.
     * &gt; [!NOTE]
     * &gt; The approval is valid for a period determined by the manufacturer and characteristics of the node presenting
     * the Commissioner Control Cluster. Clients SHOULD send the CommissionNode command immediately upon receiving a
     * CommissioningRequestResult event.
     */
    public class CommissioningRequestResult {
        public BigInteger requestId; // uint64
        public BigInteger clientNodeID; // node-id
        public Integer statusCode; // status
        public Integer fabricIndex; // FabricIndex

        public CommissioningRequestResult(BigInteger requestId, BigInteger clientNodeID, Integer statusCode,
                Integer fabricIndex) {
            this.requestId = requestId;
            this.clientNodeID = clientNodeID;
            this.statusCode = statusCode;
            this.fabricIndex = fabricIndex;
        }
    }

    // Bitmaps
    public static class SupportedDeviceCategoryBitmap {
        /**
         * Aggregators which support Fabric Synchronization may be commissioned.
         * The FabricSynchronization bit shall be set to 1 if and only if the server supports commissioning nodes that
         * support Fabric Synchronization.
         */
        public boolean fabricSynchronization;

        public SupportedDeviceCategoryBitmap(boolean fabricSynchronization) {
            this.fabricSynchronization = fabricSynchronization;
        }
    }

    public CommissionerControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1873, "CommissionerControl");
    }

    protected CommissionerControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is sent by a client to request approval for a future CommissionNode call. This is required to be a
     * separate step in order to provide the server time for interacting with a user before informing the client that
     * the CommissionNode operation may be successful.
     * If the command is not executed via a CASE session, the command shall fail with a status code of
     * UNSUPPORTED_ACCESS.
     * The server may request approval from the user, but it is not required.
     * The server shall always return SUCCESS to a correctly formatted RequestCommissioningApproval command, and then
     * generate a CommissioningRequestResult event associated with the command’s accessing fabric once the result is
     * ready.
     * Clients SHOULD avoid using the same RequestID. If the RequestID and client NodeID of a
     * RequestCommissioningApproval match a previously received RequestCommissioningApproval and the server has not
     * returned an error or completed commissioning of a device for the prior request, then the server SHOULD return
     * FAILURE.
     * The parameters for RequestCommissioningApproval command are as follows:
     */
    public static ClusterCommand requestCommissioningApproval(BigInteger requestId, Integer vendorId, Integer productId,
            String label) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (requestId != null) {
            map.put("requestId", requestId);
        }
        if (vendorId != null) {
            map.put("vendorId", vendorId);
        }
        if (productId != null) {
            map.put("productId", productId);
        }
        if (label != null) {
            map.put("label", label);
        }
        return new ClusterCommand("requestCommissioningApproval", map);
    }

    /**
     * This command is sent by a client to request that the server begins commissioning a previously approved request.
     * The server shall return FAILURE if the CommissionNode command is not sent from the same NodeID and on the same
     * fabric as the RequestCommissioningApproval or if the provided RequestID to CommissionNode does not match the
     * value provided to RequestCommissioningApproval.
     * If the command is not executed via a CASE session, the command shall fail with a status code of
     * UNSUPPORTED_ACCESS.
     * Upon receipt, the server shall respond with ReverseOpenCommissioningWindow if CommissioningRequestResult was
     * generated with StatusCode of SUCCESS for the matching RequestID field and NodeID of the client.
     * The server shall return FAILURE if the CommissionNode command is received after the server has already responded
     * to a client with ReverseOpenCommissioningWindow for a matching RequestID field and NodeID of the client unless
     * the client has sent another RequestCommissioningApproval and received an additional CommissioningRequestResult.
     * The parameters for CommissionNode command are as follows:
     */
    public static ClusterCommand commissionNode(BigInteger requestId, Integer responseTimeoutSeconds) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (requestId != null) {
            map.put("requestId", requestId);
        }
        if (responseTimeoutSeconds != null) {
            map.put("responseTimeoutSeconds", responseTimeoutSeconds);
        }
        return new ClusterCommand("commissionNode", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "supportedDeviceCategories : " + supportedDeviceCategories + "\n";
        return str;
    }
}
