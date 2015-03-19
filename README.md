Vert.x Kinesis Module
===========

Kinesis module allows to receive events published by other Vert.x verticles and send those events to AWS Kinesis.
+[![Build Status](https://travis-ci.org/zanox/mod-kinesis.svg?branch=master)](https://travis-ci.org/zanox/mod-kinesis)


Dependencies
==========

This module requires a Kinesis stream. After you have this module integrated into your application and a message has been sent to Kinesis, 
you may test the results by creating a Kinesis consumer.


Name
==========

The module name is kinesis.


Configuration
===========

When deploying this module, you need to provide the following configuration:
```javascript
{
    "address": <address>,
    "streamName": <streamName>,
    "partitionKey": <partitionKey>,
    "region": <region>,
    "endpoint": <endpoint>,
    "connectionTimeout": <connectionTimeout>,
    "maxConnection": <maxConnection>,
    "socketTimeout": <socketTimeout>,
    "useReaper": <useReaper>,
    "userAgent": <userAgent>
}
```

For example:
```javascript
{
    "address": "kinesis.verticle",
    "streamName": "kinesisTestStream",
    "partitionKey": "partitionkey",
    "region": "eu-west-1",
    "connectionTimeout": 50000,
    "maxConnection": 50,
    "socketTimeout": 50000,
    "useReaper": "true",
    "userAgent": "Mozilla/5.0" 
}
```

The detailed description of each parameter:

* `address` (mandatory) - The address of Vert.x's EventBus, where the event has been sent by your application in order to be consumed by this module later on.
* `streamName` (mandatory) - The name of the Kinesis stream where the data will be put 
* `partitionKey` (mandatory) - Determines which shard in the stream the data record is assigned to.
* `region` (mandatory) - The regional endpoint for this client's service calls.
* `endpoint` (optional) - A Kinesis endpoint, e.g., `http://localhost:4567`. Useful for testing against a service like [Kinesalite](https://github.com/mhart/kinesalite).
* `connectionTimeout` (optional) - The amount of time to wait (in milliseconds) when initially establishing a connection before giving up and timing out. 
* `maxConnection` (optional) - The maximum number of allowed open HTTP connections.
* `socketTimeout` (optional) - The amount of time to wait (in milliseconds) for data to be transfered over an established, open connection before the connection times out and is closed.
* `useReaper` (optional) - Whether the IdleConnectionReaper is to be started as a daemon thread
* `userAgent` (optional) - The HTTP user agent header to send with all requests.

Currently this version of mod-kinesis uses DEFAULT_RETRY_POLICY.

mod-kinesis is using the DefaultAWSCredentialsProviderChain of the AWS Java SDK (http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html). AWS credentials provider chain looks for credentials in this order:
* Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY
* Java System Properties - aws.accessKeyId and aws.secretKey
* Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
* Instance profile credentials delivered through the Amazon EC2 metadata service

Instance profile credentials are the preferred way to get the necessary credentials. This approach uses IAM roles for EC2-instances (see example here: https://www.youtube.com/watch?v=C4AyfV3Z3xs).


Installation
=======

```
vertx install com.zanox.vertx.mods~mod-kinesis~1.4.13
```

If you get a "not found" exception, you might need to edit the repos.txt of your Vert.x installation to use https.


Usage
=======

You can test this module locally, just deploy it in your application specifying necessary configuration.
Make sure you have a Kinesis stream running in your preferred region.

Then deploy mod-kinesis module in your application like specified below:
Example:

```java
        JsonObject config = new JsonObject();
        config.putString("address", "kinesis.verticle");
        config.putString("streamName", "kinesisTestStream");
        config.putString("partitionKey", "myPartitionKey");
        config.putString("region", "eu-west-1");
        
        container.deployModule("com.zanox.vertx.mods~mod-kinesis~1.4.13", config);

```

You can send messages from your application in Vert.x's JsonObject format, where the key must be `"payload"` string, and the value can be either byte array or String. See below for more details:

For Byte Array type
```java
JsonObject jsonObject = new JsonObject();
jsonObject.putString("payload", "your message goes here".getBytes());
```

For String type
```java
JsonObject jsonObject = new JsonObject();
jsonObject.putString("payload", "your message goes here");
```

Additionally you can specify a partitionKey for each message:

```java
JsonObject jsonObject = new JsonObject();
jsonObject.putString("payload", "your message goes here".getBytes());
jsonObject.putString("partitionKey", "your partition key goes here");
```

Then you can verify that you receive those messages in Kinesis by creating a consumer.

Now you will see the messages being consumed.


License
=========
Copyright 2015, Zanox AG under Apache License. See `LICENSE`

Author: Sascha Möllering

Contributing
============
1. Fork the repository on Github
2. Create a named feature branch
3. Develop your changes in a branch
4. Write tests for your change (if applicable)
5. Ensure all the tests are passing
6. Submit a Pull Request using Github
