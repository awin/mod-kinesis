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

package com.zanox.vertx.mods.exception;

public class KinesisException extends Exception {

	public KinesisException() {
		super();    //To change body of overridden methods use File | Settings | File Templates.
	}

	public KinesisException(String message) {
		super(message);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public KinesisException(String message, Throwable cause) {
		super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public KinesisException(Throwable cause) {
		super(cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	protected KinesisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
