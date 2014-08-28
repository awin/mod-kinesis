package com.zanox.vertx.mods;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ByteArraySerializerIT extends TestVerticle {

	private static final String ADDRESS = "default-address";
	private static final String MESSAGE = "Test message!";

	@Override
	public void start() {

		JsonObject config = new JsonObject();
		config.putString("address", ADDRESS);

		container.deployModule(System.getProperty("vertx.modulename"), config, new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> asyncResult) {
				assertTrue(asyncResult.succeeded());
				assertNotNull("DeploymentID should not be null", asyncResult.result());
				ByteArraySerializerIT.super.start();
			}
		});
	}

	/*
	@Test(expected = FailedToSendMessageException.class)
	public void sendMessage() throws Exception {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putBinary(EventProperties.PAYLOAD, MESSAGE.getBytes());

		Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
			public void handle(Message<JsonObject> message) {
				assertEquals("error", message.body().getString("status"));
				assertTrue(message.body().getString("message").equals("Failed to send message to Kafka broker..."));
				testComplete();
			}
		};
		vertx.eventBus().send(ADDRESS, jsonObject, replyHandler);
	}
	*/
}
