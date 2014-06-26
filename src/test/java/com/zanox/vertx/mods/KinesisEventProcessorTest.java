/*
 * Copyright 2014 ZANOX AG
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KinesisEventProcessorTest {

	@Mock
	private Logger logger;

	@Mock
	private Message<JsonObject> event;

	@InjectMocks
	private KinesisMessageProcessor kinesisMessageProcessor;

	@Test
	public void sendMessageToKinesis() throws Exception{
		KinesisMessageProcessor kinesisMessageProcessorSpy = spy(kinesisMessageProcessor);

		JsonObject jsonObjectMock = mock(JsonObject.class);
		when(event.body()).thenReturn(jsonObjectMock);
		when(jsonObjectMock.getString(anyString())).thenReturn("test");


		kinesisMessageProcessorSpy.handle(event);
		verify(kinesisMessageProcessorSpy).sendMessageToKinesis(event);
	}
}
