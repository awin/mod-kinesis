/*
 * Copyright 2013 ZANOX AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zanox.vertx.mods.internal;

public class KinesisProperties {

    private KinesisProperties() {}

    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String MAX_CONNECTION = "maxConnection";
    public static final String RETRY_POLICY = "retryPolicy";
    public static final String SOCKET_TIMEOUT = "socketTimeout";
    public static final String USE_REAPER = "useReaper";
    public static final String USER_AGENT = "userAgent";
    public static final String STREAM_NAME = "streamName";
    public static final String PARTITION_KEY = "partitionKey";
	public static final String REGION = "region";
	public static final String AWS_ACCESS_KEY = "awsAccessKey";
	public static final String AWS_SECRET_KEY = "awsSecretKey";
}
