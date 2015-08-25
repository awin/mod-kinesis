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

package com.zanox.vertx.mods;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.zanox.vertx.mods.exception.KinesisException;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Context;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.zanox.vertx.mods.internal.EventProperties.PAYLOAD;
import static com.zanox.vertx.mods.internal.KinesisProperties.*;

/**
 * This verticle is responsible for processing messages.
 * It subscribes to Vert.x's specific EventBus address to handle messages published by other verticles
 * and sends messages to Kinesis.
 */

public class KinesisMessageProcessor extends BusModBase implements Handler<Message<JsonObject>> {

	private int retryCounter;
    private AmazonKinesisAsyncClient kinesisAsyncClient;
	private String streamName, partitionKey, region;

	@Override
	public void handle(Message<JsonObject> jsonObjectMessage) {
		try {
			sendMessageToKinesis(jsonObjectMessage);
		} catch (KinesisException exc) {
			logger.error(exc);
		}
	}

	@Override
	public void start() {
		super.start();

		kinesisAsyncClient = createClient();

		// Get the address of EventBus where the message was published
		final String address = getMandatoryStringConfig("address");

		vertx.eventBus().registerHandler(address, this);
	}

	@Override
	public void stop() {
		if (kinesisAsyncClient != null) {
			kinesisAsyncClient.shutdown();
		}
	}

	private AmazonKinesisAsyncClient createClient() {

		// Building Kinesis configuration
		int connectionTimeout = getOptionalIntConfig(CONNECTION_TIMEOUT, ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT);
		int maxConnection = getOptionalIntConfig(MAX_CONNECTION, ClientConfiguration.DEFAULT_MAX_CONNECTIONS);

		// TODO: replace default retry policy
		RetryPolicy retryPolicy = ClientConfiguration.DEFAULT_RETRY_POLICY;
		int socketTimeout = getOptionalIntConfig(SOCKET_TIMEOUT, ClientConfiguration.DEFAULT_SOCKET_TIMEOUT);
		boolean useReaper = getOptionalBooleanConfig(USE_REAPER, ClientConfiguration.DEFAULT_USE_REAPER);
		String userAgent = getOptionalStringConfig(USER_AGENT, ClientConfiguration.DEFAULT_USER_AGENT);
		String endpoint = getOptionalStringConfig(ENDPOINT, null);


		streamName = getMandatoryStringConfig(STREAM_NAME);
		partitionKey = getMandatoryStringConfig(PARTITION_KEY);
		region = getMandatoryStringConfig(REGION);

		logger.info(" --- Stream name: " + streamName);
		logger.info(" --- Partition key: " + partitionKey);
		logger.info(" --- Region: " + region);
		if(endpoint != null) {
			logger.info(" --- Endpoint: " + endpoint);
		}

		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setConnectionTimeout(connectionTimeout);
		clientConfiguration.setMaxConnections(maxConnection);
		clientConfiguration.setRetryPolicy(retryPolicy);
		clientConfiguration.setSocketTimeout(socketTimeout);
		clientConfiguration.setUseReaper(useReaper);
		clientConfiguration.setUserAgent(userAgent);

		/*
		AWS credentials provider chain that looks for credentials in this order:
			Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY
			Java System Properties - aws.accessKeyId and aws.secretKey
			Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
			Instance profile credentials delivered through the Amazon EC2 metadata service
		*/

		AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();

		// Configuring Kinesis-client with configuration
		AmazonKinesisAsyncClient kinesisAsyncClient = new AmazonKinesisAsyncClient(awsCredentialsProvider, clientConfiguration);
		Region awsRegion = RegionUtils.getRegion(region);
		kinesisAsyncClient.setRegion(awsRegion);
		if(endpoint != null) {
			kinesisAsyncClient.setEndpoint(endpoint);
		}

		return kinesisAsyncClient;
	}

	protected void sendMessageToKinesis(final Message<JsonObject> event) throws KinesisException {
		if (kinesisAsyncClient == null) {
			throw new KinesisException("AmazonKinesisAsyncClient is not initialized");
		}

		if (!isValid(event.body().getString(PAYLOAD))) {
			logger.error("Invalid message provided.");
			return;
		}

		JsonObject object = event.body();
		logger.debug(" --- Got event " + event.toString());
		logger.debug(" --- Got body + " + object.toString());

		byte[] payload = object.getBinary(PAYLOAD);

		if (payload == null) {
			logger.debug(" --- Payload is null, trying to get the payload as String");
			payload = object.getString(PAYLOAD).getBytes();
		}
		logger.debug("Binary payload size: " + payload.length);

		String msgPartitionKey = object.getString(PARTITION_KEY);
		String requestPartitionKey = msgPartitionKey != null ? msgPartitionKey : partitionKey;

		PutRecordRequest putRecordRequest = new PutRecordRequest();
		putRecordRequest.setStreamName(streamName);
		putRecordRequest.setPartitionKey(requestPartitionKey);

		logger.debug("Writing to streamName " + streamName + " using partitionkey " + requestPartitionKey);

		putRecordRequest.setData(ByteBuffer.wrap(payload));

        retryCounter = 0;
		this.sendUsingAsyncClient(putRecordRequest, event);
	}

	private void sendUsingAsyncClient(final PutRecordRequest putRecordRequest, Message<JsonObject> event) {

        if (retryCounter == 3) {
            sendError(event, "Failed sending message to Kinesis");
        }

		final Context ctx = vertx.currentContext();
		kinesisAsyncClient.putRecordAsync(putRecordRequest, new AsyncHandler<PutRecordRequest,PutRecordResult>() {
			public void onSuccess(PutRecordRequest request, final PutRecordResult recordResult) {
				ctx.runOnContext(v -> {
					logger.debug("Sent message to Kinesis: " + recordResult.toString());
					sendOK(event);
				});
			}
			public void onError(final java.lang.Exception iexc) {
				ctx.runOnContext(v -> {
                    retryCounter++;
                    logger.info("Failed sending message to Kinesis, retry: " + retryCounter + " ... ", iexc);
                    vertx.setTimer(500, timerID -> sendUsingAsyncClient(putRecordRequest, event));
				});
			}
		});
	}

	private boolean isValid(String str) {
		return str != null && !str.isEmpty();
	}
}
