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

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.*;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easyfamily.internal.dto.data.AnalogIO;
import org.openhab.binding.easyfamily.internal.dto.data.DigitalIO;
import org.openhab.binding.easyfamily.internal.dto.data.MarkerDoubleWord;
import org.openhab.binding.easyfamily.internal.dto.json.data.DataResponse;
import org.openhab.binding.easyfamily.internal.dto.json.data.MWRange;
import org.openhab.binding.easyfamily.internal.dto.json.data.NWRange;
import org.openhab.binding.easyfamily.internal.dto.json.data.Operands;
import org.openhab.binding.easyfamily.internal.dto.json.login.LoginResponse;
import org.openhab.binding.easyfamily.internal.dto.json.login.Sysinfo;
import org.openhab.binding.easyfamily.internal.dto.xml.Comment;
import org.openhab.binding.easyfamily.internal.dto.xml.ConfigData;
import org.openhab.binding.easyfamily.internal.dto.xml.Entry;
import org.openhab.binding.easyfamily.internal.dto.xml.Ich;
import org.openhab.binding.easyfamily.internal.dto.xml.PData;
import org.openhab.binding.easyfamily.internal.dto.xml.XNFO;
import org.openhab.binding.easyfamily.internal.httpclient.EasyFamilyHttpClient;
import org.openhab.binding.easyfamily.internal.httpclient.EasyFamilyHttpResponse;
import org.openhab.binding.easyfamily.internal.httpclient.PingClient;
import org.openhab.binding.easyfamily.internal.operands.*;
import org.openhab.binding.easyfamily.internal.utility.HandlerUtility;
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
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    private EasyFamilyConfiguration config;
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
        if (!checkWebserver() || !checkAPIKeyLength()) {
            return;
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
                device.statusRef = (float) 1000 / config.pollingInterval; // Reference in s
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
            if (checkClientConnection()) {
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
            @Nullable
            LoginResponse login = new Gson().fromJson(response.content, LoginResponse.class);
            // Extract device properties
            HandlerUtility.parseURLEncodedData(login);
            Sysinfo sysinfo = login != null ? login.sysinfo : new Sysinfo();
            Map<String, String> propertyMap = device.setDeviceProperties(sysinfo);
            updateProperties(propertyMap);
            // Extract device info
            setDeviceConfig(sysinfo);
            if (this.config.loadCommentaryList) {
                addChannelsFromCommentList();
            } else {
                device.operands.clear();
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

            initializeChannelState();
            setStateChannel(sysinfo.state);
            setIOXChannelState(sysinfo.extState.extBus);

            updateStatus(ThingStatus.ONLINE);
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

    @Override
    public void thingUpdated(Thing thing) {
        List<Channel> newChannels = thing.getChannels();
        List<Channel> oldChannels = this.thing.getChannels();

        List<Channel> extractedChannels = new ArrayList<>(oldChannels);
        if (newChannels.size() < oldChannels.size()) {
            for (Channel channel : oldChannels) {
                for (Channel channel2 : newChannels) {
                    if (channel.getUID().equals(channel2.getUID())) {
                        extractedChannels.remove(channel);
                    }
                }
            }
            for (Channel channel : extractedChannels) {
                device.operands.remove(channel.getUID().getId()); // remove operand from opreands map
            }
        } else {
            this.device.addOperandsFromChannelList(extractedChannels); // add new Channels to the operands map
        }
        this.thing = thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof DecimalType) {
            try {
                String val;
                EasyFamilyHttpResponse response = new EasyFamilyHttpResponse();
                if (checkClientConnection()) {
                    closeClient();
                    this.client = new EasyFamilyHttpClient(config);
                }
                if (device.operands.containsKey(channelUID.getId())) {
                    EasyFamilyOperand tmpOperand = device.operands.get(channelUID.getId());
                    if (tmpOperand instanceof EasyFamilyWriteableOperand operand) {
                        if (command instanceof OnOffType) {
                            if (channelUID.getId().equals(CHANNEL_ID_STATE)) {
                                if (command.equals(OnOffType.ON)) {
                                    val = MODE_RUN;
                                } else {
                                    val = MODE_STOP;
                                }
                            } else {
                                if (command.equals(OnOffType.ON)) {
                                    val = "1";
                                } else {
                                    val = "0";
                                }
                            }
                        } else {
                            val = command.toString();
                        }
                        response = client.sendMsg(operand.getPath() + operand.getQuery() + val);
                    }
                }

                if (!String.valueOf(response.httpCode).contains("2")) {
                    logger.debug("This is the content of the response: {}", response.content);
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted exception: {}", e.toString());
                closeClient();
            } catch (TimeoutException e) {
                logger.error("Time out exception: {}", e.toString());
                closeClient();
            } catch (ExecutionException e) {
                logger.error("Execution exception: {}", e.toString());
                closeClient();
            } catch (Exception e) {
                logger.error("Exception: {}", e.toString());
                closeClient();
            }
        } else if (command instanceof RefreshType) {
            var operand = device.operands.get(channelUID.getId());
            if (operand != null) {
                updateState(channelUID, operand.getState());
            }
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
        return !this.client.isConnected();
    }

    private boolean checkClientClosed() {
        return this.client.isClosed;
    }

    /**
     * This method will ping a given host and returns true if it
     * was successful or false if it fails
     *
     * @return Whether the connection could be established or not (true/false)
     */
    // Try to connect to the device and indicate if it is reachable or not
    private boolean isDeviceReachable() {
        return new PingClient().pingClient(config.ipAddress);
    }

    private void addChannelsFromCommentList()
            throws InterruptedException, TimeoutException, ExecutionException, URISyntaxException, DataFormatException {
        // Get old channels from thing to avoid doubled channels
        List<Channel> oldChannelList = thing.getChannels();

        // Create new channel list deleted channels
        List<Channel> newChannelList = createNewChannelList(oldChannelList);

        List<EasyFamilyOperand> operands = new ArrayList<>();
        // Get channel list by the commentary
        List<Channel> commentaryChannelList = getCommentList(operands);

        // Add the channels to the new channels list
        addChannelsFromCommentaryList(commentaryChannelList, newChannelList);

        // Tell the framework about the update
        thingStructureChanged(oldChannelList, newChannelList, operands);
    }

    private void addChannelsFromCommentaryList(List<Channel> commentaryChannelList, List<Channel> channels) {
        // Add commentaryList channels to the new channels list
        for (Channel comChannel : commentaryChannelList) {
            if (!channelExists(channels, comChannel)) {
                channels.add(comChannel);
            }
        }
    }

    private static boolean channelExists(List<Channel> channels, Channel comChannel) {
        for (Channel channel : channels) {
            String oldChannelLabel = comChannel.getLabel();
            if (comChannel.getUID().equals(channel.getUID())) {
                return true;
            } else if (oldChannelLabel != null) {
                String newChannelLabel = channel.getLabel();
                if (newChannelLabel != null) {
                    if (oldChannelLabel.equals(newChannelLabel)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Channel> createNewChannelList(List<Channel> oldChannelList) {
        // Add old channels to the new channelList and make sure that programState and IOXState are there
        List<Channel> newChannelList = new ArrayList<>();
        for (Channel oldChannel : oldChannelList) {
            if (oldChannel.getUID().getId().equals(CHANNEL_STATE)) {
                newChannelList.add(0, oldChannel);
            } else if (oldChannel.getUID().getId().equals(CHANNEL_IOX)) {
                if (newChannelList.contains(oldChannelList.get(0))) {
                    newChannelList.add(1, oldChannel);
                } else {
                    newChannelList.add(0, oldChannel);
                }
            } else {
                newChannelList.add(oldChannel);
            }
        }
        return newChannelList;
    }

    private List<Channel> getCommentList(List<EasyFamilyOperand> operands)
            throws InterruptedException, TimeoutException, ExecutionException, URISyntaxException, DataFormatException {
        List<Channel> emptyList = new ArrayList<>();
        if (checkClientConnection()) {
            return emptyList;
        }
        EasyFamilyHttpResponse response;

        response = client.sendMsg("/int/cgi/extra.xml"); // get commentary list

        if (response.httpCode != 200) {
            closeClient();
            return emptyList;
        }

        // setup xstream
        XStream xstream = setUpXStream();

        // Deserializing xml
        return generateChannels(xstream, response.content, operands);
    }

    protected void thingStructureChanged(List<Channel> oldChannelList, List<Channel> newChannelList,
            List<EasyFamilyOperand> operands) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannels(oldChannelList); // remove the old channels so there won't be duplicates
        Thing updatedThing = thingBuilder.withChannels(newChannelList).build();
        for (EasyFamilyOperand operand : operands) {
            if (!device.operands.containsValue(operand)) {
                device.operands.put(operand.getChannelID(), operand);
            }
        }
        updateThing(updatedThing);
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

    private List<Channel> generateChannels(XStream xStream, String response, List<EasyFamilyOperand> operands) {
        XNFO xml = (XNFO) xStream.fromXML(response);
        xml.stripComments();
        List<Comment> comments = xml.getCommentList();
        List<Channel> commentaryChannelList = new ArrayList<>();
        Channel commentChannel;
        EasyFamilyOperandFactory operandFactory = new EasyFamilyOperandFactory();
        for (Comment comment : comments) {
            String channelUID = thing.getUID().getAsString() + ":" + comment.getName();
            EasyFamilyOperand operand = operandFactory.getOperand(new ChannelUID(channelUID));
            commentChannel = operand.createChannelFromOperand(comment.getComment());
            if (commentChannel != null) {
                commentaryChannelList.add(commentChannel);
                operands.add(operand);
            }
        }
        return commentaryChannelList;
    }

    /**
     * This method is polling the data from the Device
     *
     *
     */
    // Request inputs, outputs and markers
    private void getData()
            throws InterruptedException, TimeoutException, ExecutionException, URISyntaxException, DataFormatException {
        // If socket is closed or not connected, open a new one and reconnect
        if (checkClientClosed()) {
            this.client = new EasyFamilyHttpClient(config);
            if (checkClientConnection()) {
                return;
            }
        } else if (checkClientConnection()) {
            this.client = new EasyFamilyHttpClient(config);
            if (checkClientConnection()) {
                return;
            }
        }

        StringBuilder query = new StringBuilder("?elm=STATE+EXTSTATE");
        queryForIOs(query);

        boolean outOfMarkerRange = isMarkerOutOfRange(query);

        boolean outOfNetMarkerRange = isNetMarkerOutOfRange(query);

        String path = "/api/get/data";
        EasyFamilyHttpResponse response = this.client.sendMsg(path + query);
        if (response.httpCode != 200) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            closeClient();
            return;
        }
        if (!response.content.contains("SYSINFO")) {
            logger.debug("This was the content of the response: {}", response.content);
            return;
        }
        @Nullable
        DataResponse data = new Gson().fromJson(response.content, DataResponse.class);
        DataResponse dataResponse = null != data ? data : new DataResponse();

        setStateChannel(dataResponse.sysinfo.state);
        setIOXChannelState(dataResponse.sysinfo.extState.extBus);
        setIOsState(dataResponse.operands);
        setMarkers(dataResponse.operands, outOfMarkerRange, outOfNetMarkerRange);
        giveInfo();
    }

    private void setMarkers(Operands operands, boolean outOfMarkerRange, boolean outOfNetMarkerRange) {
        if (device.deviceMarkerRangeLower != 0 && device.queries.queryForMarkers && !outOfMarkerRange) {
            for (MWRange mwrange : operands.mwRange) {
                updateMarker(mwrange.v, 0);
            }
        }
        if (device.deviceNetMarkerRangeLower != 0 && device.queries.queryForNetMarkers && !outOfNetMarkerRange) {
            int i = 1;
            for (NWRange nwrange : operands.nwRange) {
                updateMarker(nwrange.v, i++);
            }
        }
    }

    private void setIOsState(Operands operands) {
        if (device.enableIORead && device.queries.queryForIOs) {
            updateDIOs(operands.iAll, operands.oAll);
            updateAIOs(operands.aIAll, operands.aOAll);
        }
    }

    private void setIOXChannelState(String extBus) {
        var operand = getOperandByChannelID(CHANNEL_IOX);
        if (operand.updateValue("1".equals(extBus) ? 1 : 0)) {
            updateChannelState(operand.getUid(), operand.getState());
        }
    }

    private void setStateChannel(String state) {
        var operand = getOperandByChannelID(CHANNEL_STATE);
        if (operand.updateValue(MODE_RUN.equals(state) ? 1 : 0)) {
            updateChannelState(operand.getUid(), operand.getState());
        }
    }

    private void queryForIOs(StringBuilder query) {
        if (device.enableIORead) {
            if (device.queries.queryForIOs) {
                query.append("+I+O+AI+AO");
            }
        } else if (device.queries.queryForIOs) {
            if (++device.statusCounter >= 2 * device.statusRef) {
                device.statusCounter = 0;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/ioRead");
            }
        }
    }

    private boolean isMarkerOutOfRange(StringBuilder query) {
        if (device.deviceMarkerRangeLower != 0 && device.queries.queryForMarkers) {
            if (device.deviceMarkerRangeLower > device.queries.queryMarkerRangeLower) {
                if (++device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/markerOutOfRange");
                }
                return true;
            } else if (device.deviceMarkerRangeUpper < device.queries.queryMarkerRangeUpper) {
                if (++device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/markerOutOfRange");
                }
                return true;
            } else {
                int multi = device.deviceMarkerRangeLower % 2 + device.deviceMarkerRangeLower / 2;
                // Query all markers, markerBytes , markerWords and markerDoubleWords when in
                // Range
                query.append("+MW(").append(multi).append(",").append(device.deviceMarkerRangeUpper).append(")");
            }
        } else if (device.queries.queryForMarkers) {
            if (++device.statusCounter >= 2 * device.statusRef) {
                device.statusCounter = 0;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/markerRead");
            }
        }
        return false;
    }

    private boolean isNetMarkerOutOfRange(StringBuilder query) {
        if (device.deviceNetMarkerRangeLower != 0 && device.queries.queryForNetMarkers) {
            if (device.deviceNetMarkerRangeLower > device.queries.queryNetMarkerRangeLower) {
                if (++device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/netMarkerOutOfRange");
                }
                return true;
            } else if (device.deviceNetMarkerRangeUpper < device.queries.queryNetMarkerRangeUpper) {
                if (++device.statusCounter >= 2 * device.statusRef) {
                    device.statusCounter = 0;
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/netMarkerOutOfRange");
                }
                return true;
            } else {
                // Query all ND markers, which are in range
                for (int i = 1; i < 9; i++) {
                    query.append("+NW(").append(i).append(";").append(device.deviceNetMarkerRangeLower % 2).append(",")
                            .append(device.deviceNetMarkerRangeUpper).append(")");
                }
            }
        } else if (device.queries.queryForNetMarkers) {
            if (++device.statusCounter >= 2 * device.statusRef) {
                device.statusCounter = 0;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/netMarkerRead");
            }
        }
        return false;
    }

    /**
     * This method updates channel which have bit values Therefore it takes the
     * base64String decode it and parse the bytes. It needs the channelID to update
     * it.
     *
     */
    private void updateDIOs(String iAll, String oAll) {
        // Decode Base64 string and save as a byte array
        byte[] bytes = Base64.getDecoder().decode(iAll);
        List<DigitalIO> digitalIOs = getDigitalIOsFromBytes(bytes, CHANNEL_INPUTS);
        bytes = Base64.getDecoder().decode(oAll);
        digitalIOs.addAll(getDigitalIOsFromBytes(bytes, CHANNEL_OUTPUTS));
        for (DigitalIO io : digitalIOs) {
            io.updateOperandValue(this);
        }
    }

    private static List<DigitalIO> getDigitalIOsFromBytes(byte[] bytes, String type) {
        List<DigitalIO> digitalIOs = new ArrayList<>();
        int number = 1;
        for (byte b : bytes) {
            for (byte i = 0; i < 8; i++) {
                digitalIOs.add(new DigitalIO(type, number++, (b >> i) & 1));
            }
        }
        return digitalIOs;
    }

    /**
     * This method updates the analog input and output channels. Therefor it needs
     * the base64String and the channel type
     *
     * @param aIAll encoded String from the device
     * @param aOAll String representation of the channel Type
     */
    private void updateAIOs(String aIAll, String aOAll) {
        List<AnalogIO> analogIOs = getAnalogIOsFromBase64String(aIAll, CHANNEL_ANALOG_INPUTS);
        analogIOs.addAll(getAnalogIOsFromBase64String(aOAll, CHANNEL_ANALOG_OUTPUTS));
        for (AnalogIO analogIO : analogIOs) {
            analogIO.updateOperandValue(this);
        }
    }

    private static List<AnalogIO> getAnalogIOsFromBase64String(String base64String, String channelType) {
        List<AnalogIO> analogIOs = new ArrayList<>();
        byte[] valueBytes = new byte[4];
        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64String));
        int count = buffer.capacity() / 4;
        for (int i = 0; i < count; i++) {
            buffer.get(valueBytes);
            int value = ByteBuffer.wrap(valueBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
            analogIOs.add(new AnalogIO(channelType, i + 1, value));
        }
        return analogIOs;
    }

    /**
     * This method parse the given base64String to update all marker, bytes, words and
     * double words. It will parse the byte values into the DTOs.
     * It will then loop through the DTOs and update the corresponding channels.
     *
     * @param base64String string which contains the base64 coded bytes
     * @param netIndex integer value of the NETid for updating NET markers
     */
    private void updateMarker(String base64String, int netIndex) {
        var doubleWords = getValuesFromBase64String(base64String, netIndex);
        for (var doubleWord : doubleWords) {
            doubleWord.updateOperandValue(this);
        }
    }

    public EasyFamilyOperand getOperandByChannelID(String channelID) {
        return Objects.requireNonNullElseGet(device.operands.get(channelID),
                () -> new EasyFamilyNullOperand("", 0, new ChannelUID("null:null:null:null")));
    }

    /**
     *
     */
    private static MarkerDoubleWord[] getValuesFromBase64String(String base64String, int netIndex) {
        byte[] doubleWordValue = new byte[4];
        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64String));
        int count = buffer.capacity() / 4;
        var doubleWords = new MarkerDoubleWord[count];
        for (int k = 0; k < count; k++) {
            buffer.get(doubleWordValue);
            int value = ByteBuffer.wrap(doubleWordValue).order(ByteOrder.LITTLE_ENDIAN).getInt();
            doubleWords[k] = new MarkerDoubleWord(k + 1, value, netIndex);
        }
        return doubleWords;
    }

    /**
     * This method communicates to the framework that the channel should be updated
     * with the given value. Therefor it will check whether channel is requested by
     * the user or not.
     *
     * @param channelUID Unique identifier of the channel that shall be updated
     * @param val integer with the value which should be updated
     */
    public void updateChannelState(ChannelUID channelUID, State val) {
        updateState(channelUID, val);
    }

    private void initializeChannelState() {
        for (EasyFamilyOperand operand : device.operands.values()) {
            if (operand != null) {
                updateState(operand.getUid(), operand.getState());
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
        device.accessMode = (1 == sysinfo.accessMode);
        device.deviceMarkerRangeLower = (short) sysinfo.mAccess[0];
        device.deviceMarkerRangeUpper = (short) sysinfo.mAccess[1];
        device.deviceNetMarkerRangeLower = (short) sysinfo.nAccess[0];
        device.deviceNetMarkerRangeUpper = (short) sysinfo.nAccess[1];
        device.webServerPort = sysinfo.webSerPort;
    }

    /**
     * This method gives the user information about configuration problems with the
     * channels
     */
    private void giveInfo() {
        InfoStrings info = new InfoStrings(device.bundleContext);
        StringBuilder errorString = new StringBuilder(info.notAccessible);
        StringBuilder wrongChannel = new StringBuilder();
        // iterate through the operands and check for values out of boundaries
        for (EasyFamilyOperand operand : device.operands.values()) {
            if (operand != null) {
                if (operand.getNumber() > operand.getMaximumInstance()) {
                    if (operand instanceof EasyFamilyNetOperand) {
                        if (device.isInNETGroup) {
                            if (operand.getNumber() > 64) {
                                errorString.append(operand.getChannelID()).append(", ");
                            }
                        } else {
                            if (errorString.indexOf(info.notInNET) == -1) {
                                errorString.append(info.notInNET);
                            }
                        }
                    }
                    if (wrongChannel.indexOf(info.wrongOperand) == -1) {
                        wrongChannel.append(info.wrongOperand).append(operand.getChannelID()).append(", ");
                    } else {
                        wrongChannel.append(operand.getChannelID()).append(", ");
                    }
                }
            }
        }
        if (!errorString.toString().equals(info.notAccessible) || !wrongChannel.isEmpty()) {
            if (++device.statusCounter >= 1 * device.statusRef) {
                if (errorString.toString().equals(info.notAccessible)) {
                    errorString.delete(0, errorString.length() - 1);
                }
                errorString.append(wrongChannel);
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
                if (++device.statusCounter >= 3 * device.statusRef) {
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
        }
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
