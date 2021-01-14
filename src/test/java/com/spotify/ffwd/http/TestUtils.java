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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.spotify.ffwd.http.model.v1.Batch;
import com.spotify.ffwd.http.model.v2.Value;
import java.util.Optional;
import com.google.protobuf.ByteString;

class TestUtils {

  static final Batch.Point POINT =
    Batch.Point.create("test_key",
      Optional.of(ImmutableMap.of("what", "error-rate")),
      Optional.empty(),
      1234L,
      11111L);
  static final com.spotify.ffwd.http.model.v2.Batch.Point DISTRIBUTION_POINT_1 =
    com.spotify.ffwd.http.model.v2.Batch.Point.create("test_key",
      Optional.of(ImmutableMap.of("what", "error-rate")),
      Optional.empty(),
      Value.DoubleValue.create(1234D),
      11111L);

  static final com.spotify.ffwd.http.model.v2.Batch.Point DISTRIBUTION_POINT_2 =
    com.spotify.ffwd.http.model.v2.Batch.Point.create("test_key",
      Optional.of(ImmutableMap.of("what", "error-rate")),
      Optional.empty(),
      Value.DistributionValue.create(ByteString.copyFromUtf8("ABCDEFG_1234")),
      11123L);

  static final Batch BATCH =
    Batch.create(Optional.of(
      ImmutableMap.of("what", "error-rate")), Optional.empty(), ImmutableList.of(POINT));

  static final com.spotify.ffwd.http.model.v2.Batch BATCH_V2 =
    com.spotify.ffwd.http.model.v2.Batch.create(Optional.of(
      ImmutableMap.of("what", "error-rate")),
      Optional.empty(), ImmutableList.of(DISTRIBUTION_POINT_1, DISTRIBUTION_POINT_2));

  static String createJsonString(Object object) {
    final ObjectMapper mapper = HttpClient.Builder.setupApplicationJson();
    try {
      return mapper.writeValueAsString(object);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
}
