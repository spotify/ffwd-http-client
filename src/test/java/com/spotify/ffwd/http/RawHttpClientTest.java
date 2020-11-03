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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;

import okhttp3.OkHttpClient;
import org.mockserver.model.HttpRequest;


import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RawHttpClientTest {
    private static final  String  REQUEST_JSON =  "{\"commonTags\":{\"what\":\"error-rate\"}," +
            "\"commonResource\":{},\"points\":"
            + "[{\"key\":\"test_key\",\"tags\":{\"what\":\"error-rate\"},\"resource\":"
            + "{},\"value\":1234.0,\"timestamp\":11111}]}";
    private static final int SUCCESS = 200;
    private static final int ERROR_CODE = 500;

    @Rule
    public MockServerRule mockServer = new MockServerRule(this);

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private MockServerClient mockServerClient;

    private final ObjectMapper mapper = HttpClient.Builder.setupApplicationJson();

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private RawHttpClient rawHttpClient;

    @Before
    public void setUp() throws Exception {
        mockServer.getPort();

        rawHttpClient =
            new RawHttpClient(mapper, okHttpClient, "http://localhost:" + mockServer.getPort());
    }

    @After
    public void tearDown() throws Exception {
        okHttpClient.connectionPool().evictAll();
        okHttpClient.dispatcher().executorService().shutdown();

        if (okHttpClient.cache() != null) {
            okHttpClient.cache().close();
        }
    }

    @Test
    public void testPing() {
        mockServerClient
            .when(request().withMethod("GET").withPath("/ping"))
            .respond(response().withStatusCode(SUCCESS));
        rawHttpClient.ping().toCompletable().await();
    }

    @Test
    public void testSendBatchSuccess() {
        sendRequestToMockClient(REQUEST_JSON, RawHttpClient.V1_BATCH_ENDPOINT, SUCCESS);
        rawHttpClient.sendBatch(TestUtils.BATCH).toCompletable().await();
    }

    @Test
    public void testSendBatchSuccessV2() {
        final String batchRequest = TestUtils.createJsonString(TestUtils.BATCH_V2);
        sendRequestToMockClient(batchRequest, RawHttpClient.V2_BATCH_ENDPOINT, SUCCESS);
        rawHttpClient.sendBatch(TestUtils.BATCH_V2).toCompletable().await();
    }

    @Test
    public void testSendBatchFail() {
        expected.expectMessage("500: Internal Server Error");
        sendRequestToMockClient(REQUEST_JSON, RawHttpClient.V1_BATCH_ENDPOINT, ERROR_CODE);
        rawHttpClient.sendBatch(TestUtils.BATCH).toCompletable().await();
    }

    private void sendRequestToMockClient(final String requestJson, final String url, final int statusCode) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/" + url)
                        .withHeader("content-type", "application/json")
                        .withBody(requestJson))
                .respond(response().withStatusCode(statusCode));
    }
}
