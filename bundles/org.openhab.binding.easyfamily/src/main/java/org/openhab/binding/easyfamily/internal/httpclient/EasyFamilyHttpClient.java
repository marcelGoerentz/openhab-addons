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
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private @Nullable ScheduledFuture<?> timer;
    private boolean readyToSend = true;

    public boolean isClosed = false;

    public EasyFamilyHttpClient(EasyFamilyConfiguration config) {
        this.config = config;
        if (config.encryption) {
            this.client = setUpHttpsClient();
            uri = "https://" + config.ipAddress;
        } else {
            this.client = new HttpClient();
            uri = "http://" + config.ipAddress;
        }
        try {
            startClient(this.client);
        } catch (Exception e) {
            logger.error("Couldn't start Client! Exception: {}", e.toString());
            isClosed = true;
            timer = null;
            return;
        }

        Runnable runnable = () -> {
            readyToSend = true;
        };
        timer = scheduler.scheduleWithFixedDelay(runnable, 50, 50, TimeUnit.MILLISECONDS);
    }

    private HttpClient setUpHttpsClient() {
        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
        sslContextFactory.setExcludeProtocols("SSL", "SSLv2", "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1");
        HttpClient client = new HttpClient(sslContextFactory);
        return client;
    }

    private void startClient(HttpClient client) throws Exception {
        client.setFollowRedirects(false);
        client.setIdleTimeout(config.connectionTimeOut);
        client.setName("EasyFamilyHttpClient");
        client.start();
    }

    public EasyFamilyHttpResponse sendMsg(String path)
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
            return new EasyFamilyHttpResponse(response.getContentAsString(), response.getStatus());
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

    private String decompressResponse(byte[] compressed) throws DataFormatException {
        String content = "";
        Inflater decomp = new Inflater(true);
        decomp.setInput(compressed);
        int decompressLength = Math.round(compressed.length * 3);
        byte[] decompressed = new byte[decompressLength];
        int resultLength = decomp.inflate(decompressed);
        content = Jsoup.parse(new String(decompressed, 0, resultLength, StandardCharsets.UTF_8)).body().toString()
                .replace("<body>", "").replace("</body>", "").strip();
        decomp.end();
        return content;
    }

    /**
     * 
     * @return
     */
    public boolean isConnected() {
        String state = this.client.getState();
        return "STARTED".equals(state) ? true : false;
    }

    /**
     * 
     */
    public synchronized void close() {
        this.isClosed = true;
        try {
            this.client.stop();
            ScheduledFuture<?> timer = this.timer;
            if (timer != null) {
                timer.cancel(true);
                timer = null;
            }
        } catch (Exception e) {
            logger.error("Couldn't close HttpClient! Exception: {}", e.toString());
        }
    }

    @Override
    public void finalize() {
        if (!this.client.isStopped() || !this.client.isStopping()) {
            logger.error("Close has not been called");
            try {
                this.client.stop();
                ScheduledFuture<?> timer = this.timer;
                if (timer != null) {
                    timer.cancel(true);
                    timer = null;
                }
            } catch (Exception e) {
                logger.debug("Couldn't stop client in finalze: {}", e.toString());
            }
        }
    }

    /*
     * public KeyStore certificateTest() throws FileNotFoundException, CertificateException, KeyStoreException,
     * NoSuchAlgorithmException, IOException {
     * 
     * FileInputStream fis = new FileInputStream("easyE4.cert"); // path is /var/lib/openhab/ for linux
     * X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509")
     * .generateCertificate(new BufferedInputStream(fis));
     * 
     * KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
     * ks.load(null, null);
     * ks.setCertificateEntry(Integer.toString(1), ca);
     * 
     * return ks;
     * }
     */
}
