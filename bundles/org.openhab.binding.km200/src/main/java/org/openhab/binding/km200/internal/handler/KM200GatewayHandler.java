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
package org.openhab.binding.km200.internal.handler;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link KM200GatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 * @author Marcel Goerentz - Reworked initialization, polling and command handling to run asynchronously on a
 *         shared bounded worker pool instead of a coarse device-wide lock and deferred send queue
 */
@NonNullByDefault
public class KM200GatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_KMDEVICE);

    /** Safety net so discovery cannot hang forever, e.g. if the gateway becomes unresponsive mid-discovery. */
    private static final long DISCOVERY_TIMEOUT_MINUTES = 5;

    /**
     * Number of attempts made to reach the gateway during a single reachability check before the bridge is
     * reported offline. The {@code /gateway/DateTime} endpoint is observed to not respond occasionally even
     * though the gateway is otherwise reachable, so a single failed request must not immediately flip the bridge
     * offline.
     */
    private static final int GATEWAY_REACHABLE_MAX_ATTEMPTS = 3;

    /** Delay between two consecutive gateway reachability attempts. */
    private static final long GATEWAY_REACHABLE_RETRY_DELAY_SECONDS = 2;

    private List<KM200GatewayStatusListener> listeners = new CopyOnWriteArrayList<>();

    private final KM200GatewayConnector connector = new KM200GatewayConnector();
    private final KM200Device remoteDevice;
    private final KM200DataHandler dataHandler;
    private int refreshInterval;
    private volatile boolean disposed = true;

    public KM200GatewayHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        refreshInterval = 120;
        remoteDevice = new KM200Device(httpClient);
        dataHandler = new KM200DataHandler(remoteDevice);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (null != channel) {
            if (command instanceof DateTimeType || command instanceof DecimalType || command instanceof StringType) {
                prepareMessage(thing, channel, command);
            } else {
                logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
            }
        }
    }

    @Override
    public void initialize() {
        disposed = false;
        try {
            int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES/ECB/NoPadding");
            if (maxKeyLen <= 128) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Java Cryptography Extension (JCE) have to be installed");
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "AES encoding not supported");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Connecting to gateway...");
        /*
         * Reading the device's capabilities involves many HTTP round-trips and can take a while, it must not
         * block the framework thread which calls initialize().
         */
        connector.restart();
        connector.execute(this::initializeGateway);
    }

    /**
     * Discovers the gateway's capabilities and starts the periodic polling once that succeeded. This is executed
     * on a background thread since it can take a considerable amount of time.
     */
    private void initializeGateway() {
        if (getDevice().getInited()) {
            return;
        }
        logger.info("Update KM50/100/200 gateway configuration, it takes a minute....");
        getConfiguration();
        if (!getDevice().isConfigured()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            logger.debug("The KM50/100/200 gateway configuration is not complete");
            return;
        }
        if (!checkConfiguration()) {
            return;
        }
        /* configuration and communication seems to be ok */
        readCapabilities();
        if (disposed) {
            return;
        }
        if (!getDevice().getInited()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Reading the gateway's capabilities failed");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        connector.scheduleWithFixedDelay(this::pollDevice, 30, refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        disposed = true;
        /* Interrupt any in-flight gateway requests so this returns promptly. */
        connector.shutdownNow();
        synchronized (getDevice()) {
            getDevice().setInited(false);
            getDevice().setIP4Address("");
            getDevice().setCryptKeyPriv("");
            getDevice().setMD5Salt("");
            getDevice().setGatewayPassword("");
            getDevice().setPrivatePassword("");
            getDevice().serviceTreeMap.clear();
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleRemoval() {
        for (Thing actThing : getThing().getThings()) {
            actThing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ""));
        }
        this.updateStatus(ThingStatus.REMOVED);
    }

    /**
     * Gets bridges configuration
     */
    private void getConfiguration() {
        Configuration configuration = getConfig();
        for (String key : configuration.keySet()) {
            logger.trace("initialize Key: {} Value: {}", key, configuration.get(key));
            switch (key) {
                case "ip4Address":
                    String ip = (String) configuration.get("ip4Address");
                    if (ip != null && !ip.isBlank()) {
                        try {
                            InetAddress.getByName(ip);
                        } catch (UnknownHostException e) {
                            logger.warn("IP4_address is not valid!: {}", ip);
                        }
                        getDevice().setIP4Address(ip);
                    } else {
                        logger.debug("No ip4_address configured!");
                    }
                    break;
                case "privateKey":
                    String privateKey = (String) configuration.get("privateKey");
                    if (privateKey != null && !privateKey.isBlank()) {
                        getDevice().setCryptKeyPriv(privateKey);
                    }
                    break;
                case "md5Salt":
                    String md5Salt = (String) configuration.get("md5Salt");
                    if (md5Salt != null && !md5Salt.isBlank()) {
                        getDevice().setMD5Salt(md5Salt);
                    }
                    break;
                case "gatewayPassword":
                    String gatewayPassword = (String) configuration.get("gatewayPassword");
                    if (gatewayPassword != null && !gatewayPassword.isBlank()) {
                        getDevice().setGatewayPassword(gatewayPassword);
                    }
                    break;
                case "privatePassword":
                    String privatePassword = (String) configuration.get("privatePassword");
                    if (privatePassword != null && !privatePassword.isBlank()) {
                        getDevice().setPrivatePassword(privatePassword);
                    }
                    break;
                case "refreshInterval":
                    refreshInterval = ((BigDecimal) configuration.get("refreshInterval")).intValue();
                    logger.debug("Set refresh interval to: {} seconds.", refreshInterval);
                    break;
                case "maxNbrRepeats":
                    Integer maxNbrRepeats = ((BigDecimal) configuration.get("maxNbrRepeats")).intValue();
                    logger.debug("Set max. number of repeats to: {} seconds.", maxNbrRepeats);
                    remoteDevice.setMaxNbrRepeats(maxNbrRepeats);
                    break;
            }
        }
    }

    /**
     * Checks bridges configuration
     */
    private boolean checkConfiguration() {
        /* Get HTTP Data from device */
        JsonObject nodeRoot = remoteDevice.getServiceNode("/gateway/DateTime");
        if (nodeRoot == null || nodeRoot.isJsonNull()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication possible with gateway");
            return false;
        }
        logger.debug("Test of the communication to the gateway was successful..");

        /* Testing the received data, is decryption working? */
        try {
            nodeRoot.get("type").getAsString();
            nodeRoot.get("id").getAsString();
        } catch (JsonParseException e) {
            logger.debug("The data is not readable, check the key and password configuration! {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong gateway configuration");
            return false;
        }
        return true;
    }

    /**
     * Verifies that the gateway is still reachable and updates the bridge status accordingly. This is called from
     * the periodic polling cycle so that communication failures are reported to the user and cleared again once the
     * gateway is reachable again.
     * <p>
     * The {@code /gateway/DateTime} endpoint occasionally does not respond even though the gateway is otherwise
     * reachable, so up to {@link #GATEWAY_REACHABLE_MAX_ATTEMPTS} attempts are made, with a short delay in
     * between, before the bridge is reported offline.
     *
     * @return {@code true} if the gateway responded and polling should continue in this cycle
     */
    private boolean checkGatewayReachable() {
        for (int attempt = 1; attempt <= GATEWAY_REACHABLE_MAX_ATTEMPTS; attempt++) {
            if (remoteDevice.getServiceNode("/gateway/DateTime") != null) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
                return true;
            }
            if (attempt < GATEWAY_REACHABLE_MAX_ATTEMPTS) {
                logger.debug("Gateway did not respond to reachability check (attempt {}/{}), retrying.", attempt,
                        GATEWAY_REACHABLE_MAX_ATTEMPTS);
                try {
                    TimeUnit.SECONDS.sleep(GATEWAY_REACHABLE_RETRY_DELAY_SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "No communication possible with gateway");
        return false;
    }

    /**
     * Reads the devices capabilities and sets the data structures. The root services are discovered concurrently
     * on a dedicated, bounded thread pool to keep the (potentially large) number of sequential HTTP round-trips
     * from dominating the initialization time.
     */
    private void readCapabilities() {
        try {
            List<CompletableFuture<Void>> rootFutures = new ArrayList<>();
            /* Checking of the device specific services and creating of a service list */
            for (KM200ThingType thing : KM200ThingType.values()) {
                String rootPath = thing.getRootPath();
                if (!rootPath.isEmpty()
                        && (rootPath.indexOf("/", 0) == rootPath.lastIndexOf("/", rootPath.length() - 1))) {
                    if (remoteDevice.getBlacklistMap().contains(rootPath)) {
                        logger.debug("Service on blacklist: {}", rootPath);
                        continue;
                    }
                    KM200ServiceHandler serviceHandler = new KM200ServiceHandler(rootPath, null, remoteDevice,
                            connector);
                    rootFutures.add(serviceHandler.initObject());
                }
            }
            CompletableFuture.allOf(rootFutures.toArray(new CompletableFuture<?>[0])).get(DISCOVERY_TIMEOUT_MINUTES,
                    TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.debug("Gateway capability discovery was interrupted");
            Thread.currentThread().interrupt();
            return;
        } catch (TimeoutException | ExecutionException e) {
            logger.warn("Reading the gateway's capabilities failed: {}", e.getMessage());
            return;
        }
        /* Now init the virtual services */
        KM200VirtualServiceHandler virtualServiceHandler = new KM200VirtualServiceHandler(remoteDevice);
        virtualServiceHandler.initVirtualObjects();
        /* Output all available services in the log file */
        getDevice().listAllServices();
        updateBridgeProperties();
        getDevice().setInited(true);
    }

    /**
     * Adds a GatewayConnectedListener
     */
    public void addGatewayStatusListener(KM200GatewayStatusListener listener) {
        listeners.add(listener);
        listener.gatewayStatusChanged(getThing().getStatus());
    }

    /**
     * Removes a GatewayConnectedListener
     */
    public void removeHubStatusListener(KM200GatewayStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * Refreshes a channel. This dispatches onto the shared worker pool and returns immediately.
     */
    public void refreshChannel(Channel channel) {
        connector.execute(() -> refreshChannelInternal(channel));
    }

    private void refreshChannelInternal(Channel channel) {
        if (!getDevice().getInited()) {
            return;
        }
        String chTypes = channel.getAcceptedItemType();
        if (null == chTypes) {
            logger.warn("Channel {} has not accepted item types", channel.getLabel());
            return;
        }
        String service = KM200Utils.checkParameterReplacement(channel, getDevice());
        KM200ServiceObject object = getDevice().getServiceObject(service);
        if (null == object) {
            return;
        }
        if (object.getVirtual() == 1) {
            String parent = object.getParent();
            if (null != parent) {
                refreshChannels(parent);
            }
        } else {
            object.setUpdated(false);
            updateChannelState(channel, service, chTypes);
        }
    }

    /**
     * Updates bridges properties
     */
    private void updateBridgeProperties() {
        List<String> propertyServices = new ArrayList<>();
        propertyServices.add(KM200ThingType.GATEWAY.getRootPath());
        propertyServices.add(KM200ThingType.SYSTEM.getRootPath());
        Map<String, String> bridgeProperties = editProperties();

        for (KM200ThingType tType : KM200ThingType.values()) {
            List<String> asProperties = tType.asBridgeProperties();
            String rootPath = tType.getRootPath();
            if (rootPath.isEmpty()) {
                continue;
            }
            KM200ServiceObject serObj = getDevice().getServiceObject(rootPath);
            if (null != serObj) {
                for (String subKey : asProperties) {
                    if (serObj.serviceTreeMap.containsKey(subKey)) {
                        KM200ServiceObject subKeyObj = serObj.serviceTreeMap.get(subKey);
                        if (subKeyObj != null) {
                            String subKeyType = subKeyObj.getServiceType();
                            if (!DATA_TYPE_STRING_VALUE.equals(subKeyType)
                                    && !DATA_TYPE_FLOAT_VALUE.equals(subKeyType)) {
                                continue;
                            }
                            if (bridgeProperties.containsKey(subKey)) {
                                bridgeProperties.remove(subKey);
                            }
                            Object value = subKeyObj.getValue();
                            logger.trace("Add Property: {}  :{}", subKey, value);
                            if (null != value) {
                                bridgeProperties.put(subKey, value.toString());
                            }
                        }
                    }
                }
            }
        }
        updateProperties(bridgeProperties);
    }

    /**
     * Prepares and sends a command. This dispatches onto the shared worker pool and returns immediately; the
     * actual HTTP write happens asynchronously.
     */
    public void prepareMessage(Thing thing, Channel channel, Command command) {
        connector.execute(() -> sendCommand(channel, command));
    }

    private void sendCommand(Channel channel, Command command) {
        if (!getDevice().getInited()) {
            return;
        }
        String service = KM200Utils.checkParameterReplacement(channel, getDevice());
        String chTypes = channel.getAcceptedItemType();
        if (null == chTypes) {
            logger.warn("Channel {} has not accepted item types", channel.getLabel());
            return;
        }
        logger.trace("handleCommand channel: {} service: {}", channel.getLabel(), service);
        JsonObject newObject = dataHandler.sendProvidersState(service, command, chTypes,
                KM200Utils.getChannelConfigurationStrings(channel));
        KM200ServiceObject serObjekt = getDevice().getServiceObject(service);
        if (null == serObjekt) {
            return;
        }
        if (newObject != null) {
            if (serObjekt.getVirtual() == 0) {
                getDevice().setServiceNode(service, newObject);
            } else {
                String parent = serObjekt.getParent();
                if (null != parent) {
                    logger.trace("Sending: {} to : {}", newObject, service);
                    getDevice().setServiceNode(parent, newObject);
                }
            }
        } else if (!getDevice().containsService(service) || serObjekt.getVirtual() != 1) {
            logger.debug("Service is not availible: {}", service);
            return;
        }
        if (serObjekt.getVirtual() == 1) {
            /*
             * The written value is one of several virtual channels backed by the same physical parent service, so
             * refresh all its siblings to reflect the change that was just sent.
             */
            String parent = serObjekt.getParent();
            if (null != parent) {
                refreshChannels(parent);
            }
        }
    }

    /**
     * Polls the device: fetches the current state of every linked channel. This is called periodically and
     * dispatches all reads onto the shared worker pool without waiting for them to finish.
     */
    private void pollDevice() {
        if (!getDevice().getInited()) {
            return;
        }
        if (!checkGatewayReachable()) {
            return;
        }
        getDevice().resetAllUpdates(getDevice().serviceTreeMap);
        refreshChannels(null);
    }

    /**
     * Fans out a refresh of every linked channel whose backing service is a child of {@code parent} (or every
     * linked channel if {@code parent} is {@code null}) across the shared worker pool. This method returns
     * immediately: the actual reads happen concurrently and each channel's state is updated as soon as it becomes
     * available, since nothing downstream depends on the whole batch completing together.
     */
    private void refreshChannels(@Nullable String parent) {
        if (parent != null) {
            KM200ServiceObject serParObjekt = getDevice().getServiceObject(parent);
            if (null != serParObjekt) {
                serParObjekt.setUpdated(false);
            }
        }
        for (Thing actThing : getThing().getThings()) {
            KM200ThingHandler actHandler = (KM200ThingHandler) actThing.getHandler();
            if (actHandler == null) {
                continue;
            }
            for (Channel actChannel : actThing.getChannels()) {
                if (!actHandler.checkLinked(actChannel)) {
                    continue;
                }
                String actChTypes = actChannel.getAcceptedItemType();
                if (null == actChTypes) {
                    logger.warn("Channel {} has not accepted item types", actChannel.getLabel());
                    continue;
                }
                String tmpService = KM200Utils.checkParameterReplacement(actChannel, getDevice());
                KM200ServiceObject tmpSerObjekt = getDevice().getServiceObject(tmpService);
                if (null == tmpSerObjekt || (parent != null && !parent.equals(tmpSerObjekt.getParent()))) {
                    continue;
                }
                CompletableFuture.runAsync(() -> updateChannelState(actChannel, tmpService, actChTypes), connector);
            }
        }
    }

    private void updateChannelState(Channel channel, String service, String chTypes) {
        State state = dataHandler.getProvidersState(service, chTypes,
                KM200Utils.getChannelConfigurationStrings(channel));
        if (state != null) {
            try {
                updateState(channel.getUID(), state);
            } catch (IllegalStateException e) {
                logger.warn("Could not get updated item state", e);
            }
        }
    }

    /**
     * Return the device instance.
     */
    public KM200Device getDevice() {
        return remoteDevice;
    }
}
