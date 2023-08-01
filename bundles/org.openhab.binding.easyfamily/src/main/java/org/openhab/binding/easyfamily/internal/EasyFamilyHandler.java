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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.BINDING_ID;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ANALOG_INPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ANALOG_OUTPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_INPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_NET_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_OUTPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_ID_STATE;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_INPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_IOX;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MAPPING;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_BYTES;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_DWORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKER_WORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_BYTES;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_DWORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKER_WORDS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_OUTPUTS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_STATE;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;

import javax.net.ssl.SSLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.URIUtil;
import org.openhab.binding.easyfamily.internal.httpclient.EasyFamilyHttpClient;
import org.openhab.binding.easyfamily.internal.httpclient.EasyFamilyHttpResponse;
import org.openhab.binding.easyfamily.internal.httpclient.PingClient;
import org.openhab.binding.easyfamily.internal.json.data.DataResponse;
import org.openhab.binding.easyfamily.internal.json.data.MWRange;
import org.openhab.binding.easyfamily.internal.json.data.NWRange;
import org.openhab.binding.easyfamily.internal.json.login.LoginResponse;
import org.openhab.binding.easyfamily.internal.json.login.Sysinfo;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyOperand;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyOperandFactory;
import org.openhab.binding.easyfamily.internal.operands.EasyFamilyWriteableOperand;
import org.openhab.binding.easyfamily.internal.xml.Comment;
import org.openhab.binding.easyfamily.internal.xml.ConfigData;
import org.openhab.binding.easyfamily.internal.xml.Entry;
import org.openhab.binding.easyfamily.internal.xml.Ich;
import org.openhab.binding.easyfamily.internal.xml.PData;
import org.openhab.binding.easyfamily.internal.xml.XNFO;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link EasyFamilyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EasyFamilyHandler.class);
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final EasyDevice device;

    private @Nullable ScheduledFuture<?> getInfo;

    private List<String> deletedChannels = new ArrayList<String>();

    private EasyFamilyConfiguration config = new EasyFamilyConfiguration();
    private EasyFamilyHttpClient client;
    private int initializationFailureCounter = 0;

    public EasyFamilyHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(thing);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.device = new EasyDevice(i18nProvider, localeProvider);
        this.config = new EasyFamilyConfiguration();
        this.client = new EasyFamilyHttpClient(config);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(EasyFamilyConfiguration.class);
        if (!checkWebserver()) {
            if (!checkAPIKeyLength()) {
                return;
            }
        }
        if (this.initializationFailureCounter >= 3) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "@text/initFailed");
            dispose();
            return;
        }
        // Set status to unknown until the device is fully initialized
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);
        // retrieve the config params
        scheduler.execute(() -> {
            boolean initialized = startInitialization();

            if (initialized) {
                device.statusRef = 1000 / config.pollingInterval; // Reference in s
                // define the polling task
                Runnable runnable = new PollForData();
                // Configure and start the polling from the device
                this.getInfo = scheduler.scheduleWithFixedDelay(runnable, 50, config.pollingInterval,
                        TimeUnit.MILLISECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
                closeClient();
            }
        });
    }

    private boolean checkAPIKeyLength() {
        if (this.config.apiKey.length() < 80) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/checkCredentials");
            return false;
        } else {
            return true;
        }
    }

    private boolean checkWebserver() {
        Map<String, String> properties = editProperties();
        for (String key : properties.keySet()) {
            if ("Webserver".equals(key)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/noWebserver");
                return false;
            }
        }
        return true;
    }

    private boolean startInitialization() {
        EasyFamilyChannelUtility channelUtility = new EasyFamilyChannelUtility();
        try {
            if (!isDeviceReachable()) {
                return false;
            }
            this.client = new EasyFamilyHttpClient(config);
            if (!checkClientConnection()) {
                initializationFailureCounter++;
                initialize();
            }
            String path = "/api/get/data";
            String query = "?elm=LOGIN";
            EasyFamilyHttpResponse response = client.sendMsg(path + query);
            if (response.httpCode != 200) {
                closeClient();
                this.initializationFailureCounter++;
                updateStatus(ThingStatus.OFFLINE);
                initialize();
            }
            LoginResponse login = new Gson().fromJson(response.content, LoginResponse.class);

            if (login != null) {
                // Extract device properties
                parseURLEncodedData(login);
                Map<String, String> propertyMap = device.setDeviceProperties(login.sysinfo);
                updateProperties(propertyMap);
                // Extract device info
                setDeviceConfig(login.sysinfo);
                if (this.config.loadCommentaryList) {
                    addChannels();
                }

                // Create a HashMap to cache channel states
                List<Channel> wrongIDList = device.createMap(this.thing);
                for (Channel channel : wrongIDList) {
                    device.statusCounter++;
                    channelUtility.parseChannelInfo(channel.getUID().getId());
                    if (device.statusCounter >= 1 * device.statusRef) {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/wrongID" + " " + channelUtility.channelType + channelUtility.channelNumber);
                        device.statusCounter = 0;
                    }
                }
                if ("RUN".equals(login.sysinfo.state)) {
                    updateChannel(CHANNEL_ID_STATE, OnOffType.ON, 1);
                } else {
                    updateChannel(CHANNEL_ID_STATE, OnOffType.OFF, 0);
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (SocketTimeoutException e) {
            logger.info("Connection timed out!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/connectionTimedOut");
            closeClient();
            return false;
        } catch (SSLException e) {
            logger.info("SSL Exception! Error: {}", e.toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/SSLException");
            closeClient();
            return false;
        } catch (NoRouteToHostException e) {
            logger.info("Host not reachable");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/deviceNotReachable");
            closeClient();
            return false;
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.warn("URI Syntax Exception: {}", e.toString());
            closeClient();
            return false;
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE);
            this.initializationFailureCounter++;
            closeClient();
            initialize();
            logger.debug("Something unexpected happened {}", e.toString());
        }
        return true;
    }

    private void parseURLEncodedData(LoginResponse login) {
        String parsedData = URIUtil.decodePath(login.sysinfo.devName);
        if (parsedData != null)
            login.sysinfo.devName = parsedData;
        parsedData = URIUtil.decodePath(login.sysinfo.ethernetDomain);
        if (parsedData != null)
            login.sysinfo.ethernetDomain = parsedData;
    }

    @Override
    public void thingUpdated(Thing thing) {
        List<Channel> newChannels = thing.getChannels();
        List<Channel> oldChannels = this.thing.getChannels();
        List<Channel> extractedChannels = new ArrayList<>();

        device.operands.clear();

        EasyFamilyOperandFactory factory = new EasyFamilyOperandFactory();
        extractedChannels.addAll(oldChannels);
        if (newChannels.size() < oldChannels.size()) {
            for (Channel channel : oldChannels) {
                for (Channel channel2 : newChannels) {
                    if (channel.getUID().equals(channel2.getUID())) {
                        extractedChannels.remove(channel);
                        device.operands.put(channel2.getUID().getId(), factory.getOperand(channel2.getUID()));
                    }
                }
            }
            for (Channel channel : extractedChannels) {
                deletedChannels.add(channel.getUID().getAsString());
            }
        }
        dispose();
        this.thing = thing;
        initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof DecimalType) {
            try {
                String val = "";
                EasyFamilyHttpResponse response = new EasyFamilyHttpResponse();
                if (!checkClientConnection()) {
                    closeClient();
                    this.client = new EasyFamilyHttpClient(config);

                }
                if (device.operands.containsKey(channelUID.getId())) {
                    EasyFamilyOperand tmpOperand = device.operands.get(channelUID.getId());
                    if (tmpOperand instanceof EasyFamilyWriteableOperand) {
                        EasyFamilyWriteableOperand operand = (EasyFamilyWriteableOperand) tmpOperand;
                        if (command instanceof OnOffType) {
                            if (channelUID.getId().equals(CHANNEL_ID_STATE)) {
                                if (command.equals(OnOffType.ON)) {
                                    val = "RUN";
                                } else {
                                    val = "STOP";
                                }
                            } else {
                                if (command.equals(OnOffType.ON)) {
                                    val = "1";
                                } else {
                                    val = "0";
                                }
                            }
                        } else if (command instanceof DecimalType) {
                            val = command.toString();
                        }
                        synchronized (this.client) {
                            response = client.sendMsg(operand.getPath() + operand.getQuery() + val);
                        }
                    }
                }

                if (202 != response.httpCode) {
                    logger.debug("This is the content of the response: {}", response.content);
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted exception: {}", e.toString());
                closeClient();
            } catch (TimeoutException e) {
                logger.error("Time out exception: {}", e.toString());
                closeClient();
                ;
            } catch (ExecutionException e) {
                logger.error("Execution exception: {}", e.toString());
                closeClient();
            } catch (Exception e) {
                logger.error("Exception: {}", e.toString());
                closeClient();
            }
        } else if (command instanceof RefreshType) {
            List<Channel> wrongIDs = this.device.createMap(this.thing);
            if (!wrongIDs.isEmpty()) {
                logger.debug("There are some wrong IDs!");
            }
            thingUpdated(thing);
        }
    }

    // Kill the runnable if the handler is going to be deleted or unitialized
    @Override
    public void dispose() {
        try {
            ScheduledFuture<?> getInfo = this.getInfo;
            if (getInfo != null) {
                while (!getInfo.isCancelled()) {
                    getInfo.cancel(true);
                    this.getInfo = null;
                }
            }
            closeClient();
        } catch (Exception exception) {
            logger.error("Something went wrong while disposing the Handler! This Exception has been thrown: {}",
                    exception.toString());
        }
    }

    @Override
    public void finalize() {
        try {
            ScheduledFuture<?> getInfo = this.getInfo;
            if (getInfo != null) {
                while (!getInfo.isCancelled()) {
                    getInfo.cancel(true);
                    this.getInfo = null;
                }
            }
            closeClient();
        } catch (Exception exception) {
            logger.error("Something went wrong while finalizing the Handler! This Exception has been thrown: {}",
                    exception.toString());
        }
    }

    /**
     * Close Client in a safe way
     */
    private void closeClient() {
        if (!this.client.isClosed) {
            this.client.close();
        }
    }

    /**
     * 
     */
    private boolean checkClientConnection() {
        if (!this.client.isConnected()) {
            this.client.close();
            return false;
        }
        return true;
    }

    private boolean checkClientClosed() {
        if (!this.client.isClosed) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method will ping a given host and returns true if it
     * was successfull or false if it fails
     * 
     * @return Wheter the connection could be established or not (true/false)
     */
    // Try to connect to the device and indicate if it is reachable or not
    private boolean isDeviceReachable() throws IOException {
        return PingClient.pingClient(config.ipAddress);
    }

    private boolean addChannels()
            throws InterruptedException, TimeoutException, ExecutionException, URISyntaxException, DataFormatException {
        ThingBuilder thingBuilder = editThing();
        if (!checkClientConnection()) {
            return false;
        }
        EasyFamilyHttpResponse response = new EasyFamilyHttpResponse();
        synchronized (this.client) {
            response = client.sendMsg("/int/cgi/extra.xml"); // get commentary list
        }
        if (response.httpCode != 200) {
            closeClient();
            ;
            return false;
        }

        // setup xstream
        XStream xstream = setUpXStream();

        // Deserializing xml
        List<Channel> commentaryChannelList = generateChannels(xstream, response.content);

        // Remove old channels from thing
        List<Channel> oldChannelList = thing.getChannels();
        thingBuilder.withoutChannels(oldChannelList);
        // Add channels to the new channelList
        List<Channel> newChannels = new ArrayList<Channel>();
        for (Channel oldChannel : oldChannelList) {
            if (oldChannel.getUID().getId().equals(CHANNEL_STATE)) {
                newChannels.add(0, oldChannel);
            } else if (oldChannel.getUID().getId().equals(CHANNEL_IOX)) {
                if (newChannels.contains(oldChannelList.get(0))) {
                    newChannels.add(1, oldChannel);
                } else {
                    newChannels.add(0, oldChannel);
                }
            } else if (newChannels.contains(oldChannel)) {
                continue;
            } else if (!deletedChannels.contains(oldChannel.getUID().getAsString())) {
                newChannels.add(oldChannel);
            }
        }
        // Add commentaryList channels to the new channels list
        for (Channel channel : commentaryChannelList) {
            boolean channelExists = false;
            for (int i = 0; i < newChannels.size(); i++) {
                String oldChannelLabel = channel.getLabel();
                if (channel.getUID().equals(newChannels.get(i).getUID())) {
                    channelExists = true;
                } else if (oldChannelLabel != null) {
                    String newChannelLabel = newChannels.get(i).getLabel();
                    if (newChannelLabel != null) {
                        if (oldChannelLabel.equals(newChannelLabel)) {
                            channelExists = true;
                        }
                    }
                }
            }
            if (newChannels.contains(channel) || channelExists) {
                continue;
            } else if (!deletedChannels.contains(channel.getUID().getAsString())) {
                newChannels.add(channel);
            }
        }
        thingBuilder.withChannels(newChannels);
        updateThing(thingBuilder.withChannels(newChannels).build());
        return true;
    }

    private XStream setUpXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.allowTypesByWildcard(new String[] { EasyFamilyHandler.class.getPackageName() + ".**" });
        xstream.setClassLoader(getClass().getClassLoader());
        xstream.autodetectAnnotations(true);
        xstream.alias("xnfo", XNFO.class);
        xstream.alias("e", Entry.class);
        xstream.alias("i", Ich.class);
        xstream.alias("r", ConfigData.class);
        xstream.alias("p", PData.class);
        xstream.alias("cm", Comment.class);
        return xstream;
    }

    private List<Channel> generateChannels(XStream xStream, String response) {
        XNFO xml = (XNFO) xStream.fromXML(response);
        xml.stripComments();
        List<Comment> comments = xml.getCommentList();
        List<Channel> commentaryChannelList = new ArrayList<>();
        Channel commentChannel;
        for (Comment comment : comments) {
            String channelMapID = CHANNEL_MAPPING.get(comment.getName().replaceAll("[0-9]", ""));
            if (channelMapID != null) {
                commentChannel = newChannel(channelMapID, comment.getName(), comment.getComment());
                commentaryChannelList.add(commentChannel);
            }
        }
        return commentaryChannelList;
    }

    private Channel newChannel(String channelID, String id, String name) {
        String thingUID = thing.getUID().getAsString();
        ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, channelID);
        if (isSwitch(channelID)) {
            return ChannelBuilder.create(new ChannelUID(thingUID + ":" + id), "Switch").withLabel(name)
                    .withType(typeUID).build();
        } else {
            return ChannelBuilder.create(new ChannelUID(thingUID + ":" + id), "Number").withLabel(name)
                    .withType(typeUID).build();
        }
    }

    private boolean isSwitch(String channelID) {
        boolean isSwitch = false;
        switch (channelID) {
            case CHANNEL_ID_INPUTS:
            case CHANNEL_ID_OUTPUTS:
            case CHANNEL_ID_MARKERS:
            case CHANNEL_ID_NET_MARKERS:
                isSwitch = true;
        }
        return isSwitch;
    }

    /**
     * This method is polling the data from the Device
     * 
     * @throws DataFormatException
     * 
     * @throws SocketTimeoutException when the connection timed out
     * @throws IOException when the host is unknown
     */
    // Request inputs, outputs and markers
    private void getData()
            throws InterruptedException, TimeoutException, ExecutionException, URISyntaxException, DataFormatException {
        // If socket is closed or not connected, open a new one and reconnect
        if (checkClientClosed()) {
            this.client = new EasyFamilyHttpClient(config);
            if (!checkClientConnection()) {
                return;
            }
        } else if (!checkClientConnection()) {
            this.client = new EasyFamilyHttpClient(config);
            if (!checkClientConnection()) {
                return;
            }
        }

        StringBuilder query = new StringBuilder("?elm=STATE+EXTSTATE");
        if (device.enableIORead) {
            if (device.querries.queryForIOs) {
                query.append("+I+O+AI+AO");
            }
        } else if (device.querries.queryForIOs) {
            device.statusCounter++;
            if (device.statusCounter >= 2 * device.statusRef) {
                device.statusCounter = 0;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/ioRead");
            }
        }

        boolean outOfMarkerRange = false;
        if (device.deviceMarkerRangeLower != 0 && device.querries.queryForMarkers) {
            if (device.deviceMarkerRangeLower > device.querries.queryMarkerRangeLower) {
                device.statusCounter++;
                outOfMarkerRange = true;
                if (device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/markerOutOfRange");
                }
            } else if (device.deviceMarkerRangeUpper < device.querries.queryMarkerRangeUpper) {
                outOfMarkerRange = true;
                device.statusCounter++;
                if (device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/markerOutOfRange");
                }
            } else {
                int multi = device.deviceMarkerRangeLower % 2 + device.deviceMarkerRangeLower / 2;
                // Query all markers, markerBytes , markerWords and markerDoubleWords when in
                // Range
                query.append("+MW(").append(multi).append(",").append(device.deviceMarkerRangeUpper).append(")");
            }
        } else if (device.querries.queryForMarkers) {
            device.statusCounter++;
            if (device.statusCounter >= 2 * device.statusRef) {
                device.statusCounter = 0;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/markerRead");
            }
        }
        boolean outOfNetMarkerRange = false;
        if (device.deviceNetMarkerRangeLower != 0 && device.querries.queryForNetMarkers) {
            if (device.deviceNetMarkerRangeLower > device.querries.queryNetMarkerRangeLower) {
                device.statusCounter++;
                outOfNetMarkerRange = true;
                if (device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/netMarkerOutOfRange");
                }
            } else if (device.deviceNetMarkerRangeUpper < device.querries.queryNetMarkerRangeUpper) {
                outOfNetMarkerRange = true;
                device.statusCounter++;
                if (device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/netMarkerOutOfRange");
                }
            } else {
                // Query all ND markers, which are in range
                for (int i = 1; i < 9; i++) {
                    query.append("+NW(").append(i).append(";").append(device.deviceNetMarkerRangeLower % 2).append(",")
                            .append(device.deviceNetMarkerRangeUpper).append(")");
                }
            }
        } else if (device.querries.queryForNetMarkers) {
            device.statusCounter++;
            if (device.statusCounter >= 2 * device.statusRef) {
                device.statusCounter = 0;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/netMarkerRead");
            }
        }

        String path = "/api/get/data";
        EasyFamilyHttpResponse response = new EasyFamilyHttpResponse();
        synchronized (this.client) {
            response = this.client.sendMsg(path + query);
        }
        if (response.httpCode != 200) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            closeClient();
            return;
        }
        if (!response.content.contains("SYSINFO")) {
            logger.debug("This was the content of the response: {}", response.content);
        }
        DataResponse dataResponse = new Gson().fromJson(response.content, DataResponse.class);
        if (dataResponse != null) {
            dataResponse = new DataResponse(dataResponse);
        } else {
            return;
        }

        // Extract the base64 strings and update the channels
        if ("RUN".equals(dataResponse.sysinfo.state)) {
            updateChannel(CHANNEL_STATE, OnOffType.ON, 1);
        } else {
            updateChannel(CHANNEL_STATE, OnOffType.OFF, 0);
        }

        if ("1".equals(dataResponse.sysinfo.extState.extBus)) {
            updateChannel(CHANNEL_IOX, OnOffType.ON, 1);
        } else {
            updateChannel(CHANNEL_IOX, OnOffType.OFF, 0);
        }

        if (device.enableIORead && device.querries.queryForIOs) {
            updateBitValues(dataResponse.operands.iAll, CHANNEL_INPUTS, "");
            updateBitValues(dataResponse.operands.oAll, CHANNEL_OUTPUTS, "");
            updateAIOs(dataResponse.operands.aIAll, CHANNEL_ANALOG_INPUTS);
            updateAIOs(dataResponse.operands.aOAll, CHANNEL_ANALOG_OUTPUTS);
        }
        if (device.deviceMarkerRangeLower != 0 && device.querries.queryForMarkers && !outOfMarkerRange) {
            for (MWRange mwrange : dataResponse.operands.mwRange) {
                updateMarker(mwrange.v, CHANNEL_MARKERS, "");
            }

        }
        if (device.deviceNetMarkerRangeLower != 0 && device.querries.queryForNetMarkers && !outOfNetMarkerRange) {
            int i = 1;
            for (NWRange nwrange : dataResponse.operands.nwRange) {
                updateMarker(nwrange.v, CHANNEL_NET_MARKERS, Integer.toString(i));
                i++;
            }
        }
        if (device.firstValues) {
            device.firstValues = false;
        }
        giveInfo();
    }

    /**
     * This method updates channel which have bit values Therefore it takes the
     * base64String decode it and parse the bytes. It needs the channelID to update
     * it.
     * 
     * @param base64String
     * @param channelType
     */
    private void updateBitValues(String base64String, String channelType, String index) {
        // Decode Base64 string and save as a byte array
        byte[] bytes = Base64.getDecoder().decode(base64String);
        // Analyze the String and update the corresponding channel
        for (int i = 0; i < bytes.length; i++) {
            int val = bytes[i];
            // Now analyze the binary string and update the channel
            for (int j = 0; j < 8; j++) {
                StringBuilder channelUID = new StringBuilder();
                if (!"".equals(index)) {
                    channelUID.append(index);
                }
                int bit = val % 2;
                int channelNumber = (i * 8 + (j + 1));
                if (channelNumber < 10) {
                    channelUID.append(channelType).append(0);
                } else {
                    channelUID.append(channelType);
                }
                channelUID.append(channelNumber);
                updateChannel(channelUID.toString(), OnOffType.ON, bit);
                val = val / 2;
            }
        }
    }

    /**
     * This method updates the analog input and output channels. Therefor it needs
     * the base64String and the channel type
     * 
     * @param base64String
     * @param channelType
     */
    private void updateAIOs(String base64String, String channelType) {
        // Decode Base64 string and save as a byte array
        byte[] bytes = Base64.getDecoder().decode(base64String);
        int val;
        int channelNumber;

        for (int index = 0; index < 48; index++) {
            StringBuilder channelUID = new StringBuilder();
            channelNumber = index + 1;
            int j = (channelNumber * 4) - 1;
            val = ((bytes[j] & 0xff) << 24) | ((bytes[j - 1] & 0xff) << 16) | ((bytes[j - 2] & 0xff) << 8)
                    | (bytes[j - 3] & 0xff);
            if (channelNumber < 10) {
                channelUID.append(channelType).append(0).append(channelNumber);
            } else {
                channelUID.append(channelType).append(channelNumber);
            }
            updateChannel(channelUID.toString(), DecimalType.ZERO, val);
        }
    }

    /**
     * This method parse the given base64String to update all bytes, words and
     * doublewords It will obtain a substring from the given base64String to update
     * the bit values of the bytes using the
     * {@link #updateBitValues(base64String, channelType) updateBitValues} method.
     * It also uses the {@link #updateChannel(String, Command, int) updateChannel}
     * method to communicate the actual values of the channels.
     * 
     * @param base64String string which contains the base64 coded bytes
     * @param channelType string which contains the channel type that should be
     *            updated
     * @param index integer value of the NETid for updating NETmarkers
     */
    private void updateMarker(String base64String, String channelType, String index) {
        String markerBit = "";
        int leftPos;
        int rightPos;
        // Markers are only in MW01 - MW32
        if ("M".equals(channelType)) {
            if (device.querries.queryMarkerRangeLower < 32) {
                if (device.querries.queryMarkerRangeLower == 1) {
                    leftPos = 0;
                } else {
                    leftPos = 8 * device.querries.queryMarkerRangeLower / 3;
                    if (!(device.querries.queryMarkerRangeLower % 3 == 0)) {
                        leftPos++;
                    }
                }
                rightPos = 8 * device.querries.queryMarkerRangeUpper / 3;
                if (!(device.querries.queryMarkerRangeUpper % 3 == 0)) {
                    rightPos++;
                }
                markerBit = base64String.substring(leftPos, rightPos);
                updateBitValues(markerBit, CHANNEL_MARKERS, "");
            }
        } else {
            if (device.querries.queryNetMarkerRangeLower < 32) {
                if (device.querries.queryNetMarkerRangeLower == 1) {
                    leftPos = 0;
                } else {
                    leftPos = 8 * device.querries.queryNetMarkerRangeLower / 3;
                    if (!(device.querries.queryNetMarkerRangeLower % 3 == 0)) {
                        leftPos++;
                    }
                }
                rightPos = 8 * device.querries.queryNetMarkerRangeUpper / 3;
                if (!(device.querries.queryNetMarkerRangeUpper % 3 == 0)) {
                    rightPos++;
                }
                markerBit = base64String.substring(leftPos, rightPos);
                updateBitValues(markerBit, CHANNEL_NET_MARKERS, index);
            }
        }

        byte[] bytes = Base64.getDecoder().decode(base64String);
        String channelUID;
        int val;
        int j = 1;
        String channelID = index + channelType;
        int channelNumber;
        for (int i = 0; i < bytes.length; i++) {
            channelNumber = i + 1;
            channelUID = channelID + "B";
            if (channelNumber < 10) {
                channelUID = channelUID + "0" + channelNumber;
            } else {
                channelUID = channelUID + channelNumber;
            }
            val = bytes[i] & 0xff;
            updateChannel(channelUID, DecimalType.ZERO, val);
            switch (j) {
                case 2:
                    channelNumber = channelNumber / 2;
                    channelUID = channelID + "W";
                    if (channelNumber < 10) {
                        channelUID = channelUID + "0" + channelNumber;
                    } else {
                        channelUID = channelUID + channelNumber;
                    }
                    val = ((bytes[i] & 0xff) << 8) | (bytes[i - 1] & 0xff);
                    updateChannel(channelUID, DecimalType.ZERO, val);
                    break;
                case 4:
                    channelNumber = channelNumber / 2;
                    channelUID = channelID + "W";
                    if (channelNumber < 10) {
                        channelUID = channelUID + "0" + channelNumber;
                    } else {
                        channelUID = channelUID + channelNumber;
                    }
                    val = ((bytes[i] & 0xff) << 8) | (bytes[i - 1] & 0xff);
                    updateChannel(channelUID, DecimalType.ZERO, val);
                    channelNumber = channelNumber / 2;
                    channelUID = channelID + "D";
                    if (channelNumber < 10) {
                        channelUID = channelUID + "0" + channelNumber;
                    } else {
                        channelUID = channelUID + channelNumber;
                    }
                    val = ((bytes[i] & 0xff) << 24) | ((bytes[i - 1] & 0xff) << 16) | ((bytes[i - 2] & 0xff) << 8)
                            | (bytes[i - 3] & 0xff);
                    updateChannel(channelUID, DecimalType.ZERO, val);
                    j = 0;
                    break;
            }
            j++;
        }
    }

    /**
     * This method communicates to the framework that the channel should be updated
     * with the given value. Therefor it will check whether channel is requested by
     * the user or not.
     * 
     * @param channelID string with unique identification of the channel
     * @param command
     * @param val integer with the value which should be updated
     */
    private void updateChannel(String channelID, Command command, int val) {
        if (device.operands.containsKey(channelID)) {
            EasyFamilyOperand operand = device.operands.get(channelID);
            if (operand != null) {
                if (operand.value == val && !device.firstValues) {
                    return;
                }
                operand.value = val;
                if (command instanceof OnOffType) {
                    updateState(operand.uid, operand.getState(true));
                    return;
                } else if (command instanceof DecimalType) {
                    updateState(operand.uid, operand.getState(false));
                    return;
                }
            } else {
                return;
            }
        }
    }

    /**
     * This method parse the LoginRepsonse for the {@link #initialize() initialize}
     * method and save the properties of the device.
     * 
     * @param sysinfo needs the Sysinfo Object retrieved from the Login Response
     */
    private void setDeviceConfig(Sysinfo sysinfo) {
        device.isInNETGroup = (0 < sysinfo.netID);
        device.enableIORead = (1 == sysinfo.enableIORead);
        device.accessMode = (1 == sysinfo.access_Mode);
        device.deviceMarkerRangeLower = (short) sysinfo.mAccess[0];
        device.deviceMarkerRangeUpper = (short) sysinfo.mAccess[1];
        device.deviceNetMarkerRangeLower = (short) sysinfo.nAccess[0];
        device.deviceNetMarkerRangeUpper = (short) sysinfo.nAccess[1];
    }

    /**
     * This method gives the user information about configuration problems with the
     * channels
     */
    private void giveInfo() {
        InfoStrings info = new InfoStrings(device.bundleContext);
        StringBuilder errorString = new StringBuilder(info.notAccessible);
        StringBuilder wrongChannel = new StringBuilder();
        EasyFamilyChannelUtility channelUtility = new EasyFamilyChannelUtility();
        // get a list of all the channels bound to the thing
        List<Channel> channelList = thing.getChannels();
        // iterate through the list and add them to them map if there are supported
        for (Channel channel : channelList) {
            String channelID = channel.getUID().getId();
            channelUtility.parseChannelInfo(channelID);
            switch (channelUtility.channelType) {
                case CHANNEL_IOX:
                case CHANNEL_STATE:
                    break;
                case CHANNEL_INPUTS:
                case CHANNEL_OUTPUTS:
                    if (channelUtility.channelNumber > 128) {
                        errorString.append(channelID + ", ");
                    }
                    break;
                case CHANNEL_MARKERS:
                case CHANNEL_MARKER_BYTES:
                case CHANNEL_MARKER_WORDS:
                    if (channelUtility.channelNumber > 512) {
                        errorString.append(channelID + ", ");
                    }
                    break;
                case CHANNEL_MARKER_DWORDS:
                    if (channelUtility.channelNumber > 256) {
                        errorString.append(channelID + ", ");
                    }
                    break;
                case CHANNEL_NET_MARKERS:
                    if (device.isInNETGroup) {
                        if (channelUtility.channelNumber > 512) {
                            errorString.append(channelID + ", ");
                        }
                    } else {
                        if (errorString.indexOf(info.notInNET) == -1) {
                            errorString.append(info.notInNET);
                        }
                    }
                    break;
                case CHANNEL_NET_MARKER_BYTES:
                    if (device.isInNETGroup) {
                        if (channelUtility.channelNumber > 64) {
                            errorString.append(channelID + ", ");
                        }
                    } else {
                        if (errorString.indexOf(info.notInNET) == -1) {
                            errorString.append(info.notInNET);
                        }
                    }
                    break;
                case CHANNEL_NET_MARKER_WORDS:
                    if (device.isInNETGroup) {
                        if (channelUtility.channelNumber > 32) {
                            errorString.append(channelID + ", ");
                        }
                    } else {
                        if (errorString.indexOf(info.notInNET) == -1) {
                            errorString.append(info.notInNET);
                        }
                    }
                    break;
                case CHANNEL_NET_MARKER_DWORDS:
                    if (device.isInNETGroup) {
                        if (channelUtility.channelNumber > 16) {
                            errorString.append(channelID + ", ");
                        }
                    } else {
                        if (errorString.indexOf(info.notInNET) == -1) {
                            errorString.append(info.notInNET);
                        }
                    }
                    break;
                case CHANNEL_ANALOG_INPUTS:
                case CHANNEL_ANALOG_OUTPUTS:
                    if (channelUtility.channelNumber > 48) {
                        errorString.append(channelID + ", ");
                    }
                    break;
                default:
                    if (wrongChannel.indexOf(info.wrongOperand) == -1) {
                        wrongChannel.append(info.wrongOperand + channelID + ", ");
                    } else {
                        wrongChannel.append(channelID + ", ");
                    }

            }
        }
        if (errorString.toString().equals(info.notAccessible) && wrongChannel.length() <= 0) {
            return;
        } else {
            device.statusCounter++;
            if (device.statusCounter >= 1 * device.statusRef) {
                if (errorString.toString().equals(info.notAccessible)) {
                    errorString.delete(0, errorString.length() - 1);
                }
                errorString.append(wrongChannel.toString());
                if (errorString.length() < 2) {
                    logger.debug("Error String contains less characters then expected");
                    return;
                }
                errorString.delete(errorString.length() - 2, errorString.length() - 1);
                errorString.append("!");
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorString.toString());
                device.statusCounter = 0;
            }
        }
    }

    private class PollForData implements Runnable {
        public void run() {
            try {
                getData();
                device.statusCounter++;
                if (device.statusCounter >= 3 * device.statusRef) {
                    updateStatus(ThingStatus.ONLINE);
                    device.statusCounter = 0;
                }
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Interrupted exception: {}", e.toString());
                closeClient();
            } catch (TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/connectionTimedOut");
                logger.debug("Timeout exception: {}", e.toString());
                closeClient();
            } catch (ExecutionException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Execution exception: {}", e.toString());
                closeClient();
            } catch (URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("URI Syntax exception: {}", e.toString());
                closeClient();
            } catch (DataFormatException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("DataFormat exception: {}", e.toString());
                closeClient();
            }
        };
    }

    /**
     * Get the localized info strings
     */
    public class InfoStrings {

        String notAccessible = "";
        String notInNET = "";
        String wrongOperand = "";

        public InfoStrings(BundleContext bundleContext) {
            @Nullable
            String tmp = i18nProvider.getText(bundleContext.getBundle(), "notAccessible", "No access to operands:",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.notAccessible = tmp + " ";
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "notInNET", "device is not in a NET Group,",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.notInNET = tmp + " ";
            }
            tmp = i18nProvider.getText(bundleContext.getBundle(), "wrongID", "Wrong Operand at channel:",
                    localeProvider.getLocale());
            if (tmp != null) {
                this.wrongOperand = tmp + " ";
            }
        }
    }
}
