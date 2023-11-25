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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jsoup.Jsoup;
import org.openhab.binding.easyfamily.internal.EasyFamilyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EasyFamilyHttpClient} class manages the httpClient connection.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EasyFamilyHttpClient {

    private final Logger logger = LoggerFactory.getLogger(EasyFamilyHttpClient.class);
    private final HttpClient client;
    private final EasyFamilyConfiguration config;
    private final String uri;

    private final @Nullable ScheduledFuture<?> timer;
    private boolean readyToSend = true;

    public boolean isClosed = false;

    public EasyFamilyHttpClient(EasyFamilyConfiguration config) {
        this.config = config;
        StringBuilder uriBuilder = new StringBuilder().append("http");
        if (config.encryption) {
            this.client = setUpHttpsClient();
            uriBuilder.append("s://");
        } else {
            this.client = new HttpClient();
        }
        uriBuilder.append(config.ipAddress);
        if ((config.encryption && config.port != 443) || (!config.encryption && config.port != 80)) {
            uriBuilder.append(":").append(config.port);
        }
        uri = uriBuilder.toString();
        try {
            startClient();
        } catch (Exception e) {
            logger.error("Couldn't start Client! Exception: {}", e.toString());
            isClosed = true;
            timer = null;
            return;
        }

        Runnable runnable = () -> readyToSend = true;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        timer = scheduler.scheduleWithFixedDelay(runnable, 50, 50, TimeUnit.MILLISECONDS);
    }

    private HttpClient setUpHttpsClient() {
        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
        sslContextFactory.setExcludeProtocols("SSL", "SSLv2", "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1");
        return new HttpClient(sslContextFactory);
    }

    public synchronized void startClient() throws Exception {
        client.setFollowRedirects(false);
        client.setIdleTimeout(config.connectionTimeOut);
        client.setName("EasyFamilyHttpClient");
        client.start();
    }

    public synchronized void stopClient() throws Exception {
        client.stop();
    }

    public synchronized EasyFamilyHttpResponse sendMsg(String path)
            throws InterruptedException, TimeoutException, ExecutionException, URISyntaxException, DataFormatException {
        if (!readyToSend) {
            Thread.sleep(50);
        }
        Request request = setRequset(path);
        ContentResponse response = request.send();
        readyToSend = false;
        if (path.contains("xml")) {
            return new EasyFamilyHttpResponse(decompressResponse(response.getContent()), response.getStatus());
        } else {
            String tmp = response.getContentAsString();
            String content = tmp != null ? tmp : "";
            return new EasyFamilyHttpResponse(content, response.getStatus());
        }
    }

    private Request setRequset(String path) throws URISyntaxException {
        Request request = client.newRequest(new URI(uri + path));
        request.method(HttpMethod.GET);
        request.version(HttpVersion.HTTP_1_1);
        request.path(path);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.config.apiKey);
        request.header(HttpHeader.HOST, this.config.ipAddress);
        request.header(HttpHeader.CONNECTION, "keep-alive");
        return request;
    }

    private String decompressResponse(byte @Nullable [] compressed) throws DataFormatException {
        String content = "";
        if (compressed != null) {
            Inflater decomp = new Inflater(true);
            decomp.setInput(compressed);
            int decompressLength = compressed.length * 7;
            byte[] decompressed = new byte[decompressLength];
            int resultLength = decomp.inflate(decompressed);
            content = Jsoup.parse(new String(decompressed, 0, resultLength, StandardCharsets.UTF_8)).body().toString()
                    .replace("<body>", "").replace("</body>", "").strip();
            decomp.end();
        }
        return content;
    }

    /**
     *
     */
    public synchronized boolean isConnected() {
        String state = this.client.getState();
        return "STARTED".equals(state);
    }

    /**
     * 
     */
    public synchronized void close() {
        this.isClosed = true;
        try {
            stopClient();
            ScheduledFuture<?> timer = this.timer;
            if (timer != null) {
                timer.cancel(true);
            }
        } catch (Exception e) {
            logger.error("Couldn't close HttpClient! Exception: {}", e.toString());
        }
    }
}
