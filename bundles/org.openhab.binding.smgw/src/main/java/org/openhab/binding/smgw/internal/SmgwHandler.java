/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.smgw.internal;

import static org.openhab.binding.smgw.internal.SmgwBindingConstants.BINDING_ID;
import static org.openhab.binding.smgw.internal.SmgwBindingConstants.CHANNEL_TIMESTAMP;
import static org.openhab.binding.smgw.internal.SmgwBindingConstants.CHANNEL_TYPE_METER;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmgwHandler} is responsible for refreshing the smart meter's data and handling REFRESH commands.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SmgwHandler extends BaseThingHandler {
    private static final URI URI_NOT_SET = URI.create("");
    private final Logger logger = LoggerFactory.getLogger(SmgwHandler.class);
    private final HttpClient httpClient;
    private final CronScheduler cronScheduler;
    private SmgwConfiguration config = new SmgwConfiguration();
    private URI uri = URI_NOT_SET;
    private @Nullable ScheduledCompletableFuture<?> cronJob;
    private boolean channelsCreated = false;
    private List<String> channelIDs = new ArrayList<>();

    public SmgwHandler(Thing thing, HttpClient httpClient, CronScheduler cronScheduler) {
        super(thing);
        this.httpClient = httpClient;
        this.cronScheduler = cronScheduler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && !URI_NOT_SET.equals(uri)) {
            cancelRefreshJob();
            getData();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SmgwConfiguration.class);
        try {
            uri = new URI("https://" + config.hostname + "/cgi-bin/hanservice.cgi");
        } catch (URISyntaxException e) {
            uri = URI_NOT_SET;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not create URI from given hostname");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        getData();
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
    }

    private void cancelRefreshJob() {
        ScheduledCompletableFuture<?> cronJob = this.cronJob;
        if (cronJob != null) {
            cronJob.cancel(true);
            this.cronJob = null;
        }
    }

    private void getData() {
        if (URI_NOT_SET.equals(uri)) {
            logger.warn("getData() called, but URI is not set. Please describe what happened and report a bug.");
            return;
        }
        // clear cookies
        CookieStore cookieStore = httpClient.getCookieStore();
        List<HttpCookie> cookies = cookieStore.get(uri);
        cookies.forEach(cookie -> cookieStore.remove(uri, cookie));

        // clear auth
        AuthenticationStore authStore = httpClient.getAuthenticationStore();
        Authentication.Result authResult = authStore.findAuthenticationResult(uri);
        if (authResult != null) {
            authStore.removeAuthenticationResult(authResult);
        }
        Authentication authentication = authStore.findAuthentication("Digest", uri, Authentication.ANY_REALM);
        if (authentication != null) {
            authStore.removeAuthentication(authentication);
        }

        // add new auth
        authStore.addAuthentication(
                new DigestAuthentication(uri, Authentication.ANY_REALM, config.username, config.password));

        CompletableFuture<SmgwResponse> future = new CompletableFuture<>();
        httpClient.newRequest(uri).send(new ResponseListener(future));
        future.thenCompose(this::onLoginSuccess).thenCompose(this::onMeterForm).handle(this::onShowMeterValue);
    }

    private CompletableFuture<SmgwResponse> onLoginSuccess(SmgwResponse response) {
        Element tknElement = response.document().selectFirst("input[name='tkn']");
        if (tknElement == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Could not determine tkn"));
        }
        String tkn = tknElement.val();
        String showMeterValuesForm = "tkn=" + tkn + "&action=meterform";
        CompletableFuture<SmgwResponse> future = new CompletableFuture<>();
        httpClient.POST(uri).content(new StringContentProvider(showMeterValuesForm)).send(new ResponseListener(future));
        return future;
    }

    private CompletableFuture<SmgwResponse> onMeterForm(SmgwResponse response) {
        Element tknElement = response.document().selectFirst("input[name='tkn']");
        Element midElement = response.document().selectFirst("select[name='mid'] option");
        if (tknElement == null || midElement == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Could not determine mid or tkn"));
        }
        String tkn = tknElement.val();
        String mid = midElement.val();
        String localDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        String showMeterValues = "tkn=" + tkn + "&mid=" + mid + "&action=showMeterValues&from=" + localDate + "&to="
                + localDate;

        CompletableFuture<SmgwResponse> future = new CompletableFuture<>();
        httpClient.POST(uri).content(new StringContentProvider(showMeterValues))
                .header(HttpHeader.COOKIE, response.cookies()).send(new ResponseListener(future));

        return future;
    }

    private @Nullable Object onShowMeterValue(@Nullable SmgwResponse response, @Nullable Throwable t) {
        if (t != null || response == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            Element meterTables = response.document().getElementById("metervalue");
            if (!channelsCreated) {
                channelsCreated = true;
                createChannelsFromTableFindings(meterTables);
            }
            if (!channelIDs.isEmpty()) {
                Element saveDateTimeElement = null;
                for (int i = 0; i < 10; i++) {
                    Element meter = response.document().selectFirst("#table_metervalues_line" + (i + 1));
                    if (meter == null) {
                        break;
                    } else {
                        Element valueElement = meter.selectFirst("#table_metervalues_col_wert");
                        Element unitElement = meter.selectFirst("#table_metervalues_col_einheit");
                        Element dateTimeElement = meter.selectFirst("#table_metervalues_col_timestamp");
                        if (dateTimeElement == null) {
                            dateTimeElement = saveDateTimeElement;
                        }
                        if (valueElement == null || unitElement == null || dateTimeElement == null) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        } else {
                            saveDateTimeElement = dateTimeElement;
                            QuantityType<Energy> value = new QuantityType<>(
                                    valueElement.text() + " " + unitElement.text());
                            DateTimeType dateTime = DateTimeType.valueOf(dateTimeElement.text().replace(" ", "T"));
                            updateState(channelIDs.get(i * 2), value);
                            updateState(channelIDs.get(i * 2 + 1), dateTime);
                            updateStatus(ThingStatus.ONLINE);
                        }
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
        ScheduledCompletableFuture<?> cronJob = this.cronJob;
        if (cronJob == null || cronJob.isDone()) {
            this.cronJob = cronScheduler.schedule(this::getData, "5 * * * * ? *");
        }
        return null;
    }

    private void createChannelsFromTableFindings(@Nullable Element meterValue) {
        if (meterValue == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            List<Channel> newChannelList = new ArrayList<>();
            for (int i = 1; i < 11; i++) {
                Element meter = meterValue.selectFirst("#table_metervalues_line" + i);
                if (meter == null) {
                    break;
                } else {
                    Element nameElement = meter.selectFirst("#table_metervalues_col_name");
                    if (nameElement == null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    } else {
                        String channelName = nameElement.text();
                        String channelID = channelName.replace(" ", "-");
                        channelID = channelID.substring(0, channelID.lastIndexOf("-")); // Zählerstand is not valid
                        channelIDs.add(channelID);
                        ChannelUID channelUID = new ChannelUID(thing.getUID().getAsString() + ":" + channelID);
                        ChannelTypeUID typeUID = new ChannelTypeUID("system", CHANNEL_TYPE_METER);
                        Channel newChannel = ChannelBuilder.create(channelUID, "Number:Energy").withLabel(channelName)
                                .withType(typeUID).build();
                        newChannelList.add(newChannel);
                        channelID += "-Timestamp";
                        channelIDs.add(channelID);
                        channelName += " Timestamp";
                        channelUID = new ChannelUID(thing.getUID().getAsString() + ":" + channelID);
                        typeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TIMESTAMP);
                        newChannel = ChannelBuilder.create(channelUID, "DateTime").withLabel(channelName)
                                .withType(typeUID).build();
                        newChannelList.add(newChannel);
                    }
                }
            }
            if (newChannelList.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                ThingBuilder thingBuilder = editThing();
                thingBuilder.withoutChannels(thing.getChannels()); // remove the old channels so there won't be
                                                                   // duplicates
                Thing updatedThing = thingBuilder.withChannels(newChannelList).build();
                updateThing(updatedThing);
            }
        }
    }

    private static class ResponseListener extends BufferingResponseListener {
        private final Logger logger = LoggerFactory.getLogger(ResponseListener.class);
        private final CompletableFuture<SmgwResponse> resultFuture;

        public ResponseListener(CompletableFuture<SmgwResponse> resultFuture) {
            this.resultFuture = resultFuture;
        }

        @Override
        public void onComplete(@NonNullByDefault({}) Result result) {
            if (result.isSucceeded()) {
                Response response = result.getResponse();
                int status = response.getStatus();
                if (HttpStatus.isSuccess(status)) {
                    String setCookies = response.getHeaders().get(HttpHeader.SET_COOKIE);
                    String cookies = setCookies != null ? setCookies
                            : result.getRequest().getHeaders().get(HttpHeader.COOKIE);
                    Document doc = Jsoup.parse(getContentAsString());
                    resultFuture.complete(new SmgwResponse(cookies, doc));
                    return;
                }
            }
            logger.warn("Failed to request {}", result.getRequest().getURI());
            resultFuture.completeExceptionally(new IllegalStateException());
        }
    }

    private record SmgwResponse(String cookies, Document document) {
    }
}
