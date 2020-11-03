/*-
 * -\-\-
 * FastForward HTTP Client
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.ffwd.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HttpClientTest {
    @Rule
    public MockServerRule mockServer = new MockServerRule(this);

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private MockServerClient mockServerClient;

    private HttpClient httpClient;

    private final HttpRequest pingRequest = request().withMethod("GET").withPath("/ping");

    private final List<HttpDiscovery.HostAndPort> servers = new ArrayList<>();

    private final static int SUCCESS = 200;
    private final static int ERROR_CODE = 500;

    @Before
    public void setUp() throws Exception {
        mockServerClient.when(pingRequest).respond(response().withStatusCode(SUCCESS));

        servers.add(new HttpDiscovery.HostAndPort("localhost", mockServer.getPort()));
        servers.add(new HttpDiscovery.HostAndPort("localhost", mockServer.getPort()));
        servers.add(new HttpDiscovery.HostAndPort("localhost", mockServer.getPort()));

        httpClient = new HttpClient.Builder().discovery(new HttpDiscovery.Static(servers)).build();
    }

    @After
    public void tearDown() {
        mockServerClient.verify(pingRequest, VerificationTimes.atLeast(1));
        httpClient.shutdown();
    }

    @Test
    public void testSendBatchSuccess() {
        final String batchRequest = TestUtils.createJsonString(TestUtils.BATCH);
        final HttpRequest request = sendRequest(batchRequest, RawHttpClient.V1_BATCH_ENDPOINT, SUCCESS);
        httpClient.sendBatch(TestUtils.BATCH).toCompletable().await();
        mockServerClient.verify(request, VerificationTimes.atLeast(1));
    }

    @Test
    public void testSendBatchSuccessV2() {
        final String batchRequest = TestUtils.createJsonString(TestUtils.BATCH_V2);
        final HttpRequest request = sendRequest(batchRequest, RawHttpClient.V2_BATCH_ENDPOINT, SUCCESS);
        httpClient.sendBatch(TestUtils.BATCH_V2).toCompletable().await();
        mockServerClient.verify(request, VerificationTimes.atLeast(1));
    }

    private HttpRequest sendRequest(final String json, final String url, final int statusCode){
        final HttpRequest request = request()
                .withMethod("POST")
                .withPath( "/" + url)
                .withHeader("content-type", "application/json")
                .withBody(json);

        mockServerClient.when(request).respond(response().withStatusCode(statusCode));
        return request;
    }

    @Test
    public void testSendBatchFail() {
        expected.expectMessage("500: Internal Server Error");
        final String batchRequest = TestUtils.createJsonString(TestUtils.BATCH);
        final HttpRequest request = sendRequest( batchRequest, RawHttpClient.V1_BATCH_ENDPOINT, ERROR_CODE);
        try {
            httpClient.sendBatch(TestUtils.BATCH).toCompletable().await();
        } finally {
            mockServerClient.verify(request, VerificationTimes.atLeast(3));
        }
    }
}
