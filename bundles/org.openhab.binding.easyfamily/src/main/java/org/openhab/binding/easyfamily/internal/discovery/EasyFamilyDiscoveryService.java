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
package org.openhab.binding.easyfamily.internal.discovery;

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EasyFamilyDiscoveryService} is responsible for discovering new
 * devices
 *
 * @author Marcel Goerentz - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.easyfamily")
@NonNullByDefault
public class EasyFamilyDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int BACKGROUND_DISCOVERY_AFTER_MINUTES = 5;
    private final Logger logger = LoggerFactory.getLogger(EasyFamilyDiscoveryService.class);

    private @Nullable ScheduledFuture<?> easyBackgroundDiscoveryJob;

    public EasyFamilyDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    /**
     * Method to start the scan and send the UDP Packet
     */
    @Override
    protected void startScan() {
        int deviceCounter = 0;
        List<EasyFamilyScanResponse> discoveryList = new ArrayList<>();
        try {
            if (!discoveryList.isEmpty()) {
                discoveryList.clear();
            }
            List<InetAddress> addresses = getIPv4Adresses();
            // Send Broadcast on every interface
            for (int i = 0; i < addresses.size(); i++) {
                DatagramSocket datagramSocket = new DatagramSocket(0, addresses.get(i));
                datagramSocket.setBroadcast(true);
                datagramSocket.setSoTimeout(2000);
                String ipAddress = addresses.get(i).getHostAddress();
                // Define the UDP Packet
                EasyFamilyDatagram datagram = new EasyFamilyDatagram(addresses.get(i).getAddress(),
                        datagramSocket.getLocalPort());

                // sending the UDP-Paket
                InetAddress broadcastAddress = Inet4Address.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(datagram.telegramm, datagram.telegramm.length,
                        broadcastAddress, 11111);
                int localPort = datagramSocket.getLocalPort();
                datagramSocket.send(sendPacket);
                datagramSocket.close();

                // recieve the responses
                datagramSocket = new DatagramSocket(localPort);
                datagramSocket.setSoTimeout(2000);
                byte[] receiveData = new byte[178];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                while (true) {
                    try {
                        datagramSocket.receive(receivePacket);
                        EasyFamilyScanResponse scanResponse = new EasyFamilyScanResponse(receivePacket.getData());
                        if (scanResponse.deviceValid) {
                            // is the received package for this discovery
                            if (scanResponse.requestIp.equals(ipAddress)) {
                                discoveryList.add(scanResponse);
                            }
                        }
                    } catch (Exception e) { // Socket Timed Out!
                        break;
                    }
                }
                datagramSocket.close();
            }
            // inform the framework about the devices found
            buildDiscoveryResult(deviceCounter, discoveryList);
        } catch (IOException e) {
            logger.debug("Discovery Problem! This Exception has been thrown: {}", e.toString());
        }
    }

    /**
     * This Method retrieves all IPv4 addresses of the server
     * 
     * @return A list of all available IPv4 Adresses that are registered
     * @throws SocketException
     */
    private List<InetAddress> getIPv4Adresses() throws SocketException {
        Iterator<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces().asIterator();
        List<InetAddress> addresses = new ArrayList<>();
        // Get IPv4 addresses from all network interfaces
        if (null != networkInterfaces) {
            while (networkInterfaces.hasNext()) {
                NetworkInterface currentNetworkInterface = networkInterfaces.next();
                Iterator<InetAddress> inetAddresses = currentNetworkInterface.getInetAddresses().asIterator();
                while (inetAddresses.hasNext()) {
                    InetAddress currentAddress = inetAddresses.next();
                    if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                        addresses.add(currentAddress);
                    }
                }
            }
        }
        return addresses;
    }

    /**
     * Method to stop the scan
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Method for starting the background discovery
     */
    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> avoidNullException = this.easyBackgroundDiscoveryJob;
        if (null == avoidNullException || avoidNullException.isCancelled()) {
            this.easyBackgroundDiscoveryJob = scheduler.scheduleWithFixedDelay(easyDiscoveryRunnable(), 0,
                    BACKGROUND_DISCOVERY_AFTER_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * Method for stopping the background discovery
     */
    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> avoidNullException = this.easyBackgroundDiscoveryJob;
        if (null != avoidNullException && !avoidNullException.isCancelled()) {
            avoidNullException.cancel(true);
            this.easyBackgroundDiscoveryJob = null;
        }
    }

    /**
     * Method for starting the scan
     */
    protected Runnable easyDiscoveryRunnable() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startScan();
            }
        };
        return runnable;
    }

    /**
     * This method will add all the found devices to the inbox list.
     * There for it will use the {@link EasyFamilyScanResponse} class
     */
    private void buildDiscoveryResult(int deviceCounter, List<EasyFamilyScanResponse> discoveryList) {
        // Get BundleContext for the translation
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        // Build the list
        int devicesWithoutName = 0;
        for (EasyFamilyScanResponse scanResponse : discoveryList) {
            deviceCounter++;
            Map<String, Object> properties = new HashMap<>();
            String deviceName = scanResponse.deviceName;
            String domainName = scanResponse.deviceDomainName;
            if ("".equals(domainName)) {
                properties.put(CONFIG_ID_IP_ADDRESS, scanResponse.deviceIp);
            } else {
                properties.put(CONFIG_ID_IP_ADDRESS, deviceName + "." + domainName);
            }
            if (!scanResponse.deviceHTTPActive) {
                String webServerText = i18nProvider.getText(bundleContext.getBundle(), "noWebserver",
                        "The webserver is not active. Please enable the webserver via easySoft!",
                        localeProvider.getLocale());
                if (webServerText != null) {
                    properties.put("Webserver", webServerText);
                }
            } else {
                properties.put(CONFIG_ID_PORT, scanResponse.deviceHTTPPort);
                properties.put(CONFIG_ID_ENCRYPTION, scanResponse.deviceSSLEnable);
            }
            if ("".equals(deviceName)) {
                devicesWithoutName++;
                deviceName = i18nProvider.getText(bundleContext.getBundle(), "deviceWithoutName", "nameless device",
                        localeProvider.getLocale()) + " (" + devicesWithoutName + ")";
            }
            ThingUID thingUID = new ThingUID(THING_TYPE_EASYE4, scanResponse.deviceMAC);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel(deviceName).build();
            thingDiscovered(discoveryResult);
        }
        logger.info("Found: {} devices and {} without a Name", deviceCounter, devicesWithoutName);
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}
