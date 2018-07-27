# Overview

[![Build Status](https://travis-ci.org/yanghua/banyan.svg?branch=messagebus-ng)](https://travis-ci.org/yanghua/banyan)

![img 15][15]


```
banyan is a tree has thick branches which grows in the East Asia. 
```
Thanks for [@ok95](https://github.com/ok95) & [@Joy-Zhang](https://github.com/Joy-Zhang) given the good idea & guidance.

![img 14][14]

banyan used to communicate and integrate over multi-app. It depends on [RabbitMQ](http://www.rabbitmq.com/) as backend broker(message exchanger). Most scenario:

* enterprise information Integration
* oriented-component & oriented-module distributed developer
* infrastructure for esb or soa
* json-rpc / IDL-RPC(thrift)
* configured compress/uncompress
* http-bridge for all platform
* web-console UI

the necessity of encapsulating with RabbitMQ:

* provide router pattern
* embed permission into client-jar
* removed create & delete & update operation from client, replaced with central-register mode

more information please see : [banyan-category](http://blog.csdn.net/column/details/banyan.html)

## Tree topology structure
the message bus's implementation is based on Rabbitmq. It can takes advantage of multiple message exchange-types rabbitmq provided and builds many kinds of router pattern. The message bus's router topology lists below:


![img 3][3]

the advantages of the tree topology:

* hide the router topology from client-caller (just need to know the `proxy` node)
* multiple message communication pattern (p2p, pub/sub, broadcast)
* implement the message log without interrupting the message channel
* communication-policy configure once , push everywhere

##web-console
banyan has its' own web console that built as a Apache-ofbiz's component. The web console provide a dashboard about rabbitmq : 

![img 16][16]

and some core model's maintenance such as queue : 

![img 17][17]

##Restful API

* [apis](https://github.com/yanghua/banyan/tree/master/httpbridge)

## Scenario & usage

### Produce & consume

* [produce](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsume.java#L34) / [pull consume](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsume.java#L46)
* [produce](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsume.java#L34) / [push consume](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsume.java#L64)
* [produce](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsume.java#L34) / [async consume with another thread](https://github.com/yanghua/banyan/blob/master/scenario%2Fsrc%2Fmain%2Fjava%2Fcom%2Fmessagebus%2Fscenario%2Fclient%2FProduceConsume.java#L91)
* [produce-loopback](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsumeLoopback.java#L15) / [consume-loopback](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FProduceConsumeLoopback.java#L15)


### Request & response

* [request](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FRequestResponse.java#L35) / [response](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FRequestResponse.java#L35) 

### Publish & subscirbe

* [publish](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FPublishSubscribe.java#L31) / [subscribe](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FPublishSubscribe.java#L31)

### Broadcast & notification-handler

* [broadcast](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FBroadcast.java#L31) / [notification-handler](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FBroadcast.java#L31)

### Json-rpc(wrapped-offical-java-client)

* [rpc-request](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FRpcRequestResponse.java#L28) / [rpc-response](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Fapi%2FRpcRequestResponse.java#L28)

### Thrift-rpc(thrid-party-rpc-integrated)

* [rpc-request](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Ffeature%2FThriftWithAMQPRpc.java#L36) / [rpc-response](https://github.com/yanghua/banyan/blob/master/client%2Fsrc%2Ftest%2Fjava%2Fcom%2Fmessagebus%2Fclient%2Ffeature%2FThriftWithAMQPRpc.java#L59)


### Http-restful

* [http-apis](https://github.com/yanghua/banyan/tree/master/scenario/src/main/java/com/messagebus/scenario/httpBridge)

## Benchmark
it shows the  `client` performance:

### Hardware
client : 

```
OS : Mac os x Yosemite (version 10.10)
Processor : 2.5GHz Intel Core i5
Memory : 8GB 1600 MHz DDR3
JDK Version : 1.7.0_45
```

server :

```
OS : Ubuntu Server 14.04.1 (GNU/Linux 3.13.0-37-generic x86_64)
Processor : Intel(R) Xeon(R) CPU E3-1230 V2 @ 3.30GHz (8核)
Memory : 8GB
JDK Version : 1.7.0_72
```

### Produce
* single thread，multiple message size ，cycle send，compare：

![img 10][10]

* single thread，same message size，use client channel pool or not，compare：

![img 11][11]

### Consume
* single thread，multiple message size，async receive，compare：

![img 12][12]

* single thread，same message size，use client channel pool or not，compare：

![img 13][13]

## Licence
Copyright (c) 2014-2015 yanghua. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.



[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/module-dependency.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/router-topology.png
[4]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/carry-inherits.png
[5]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/handle-chain.png
[6]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/handler-chain-config.png
[7]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/node.png
[8]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/node-db-info.png
[9]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/common/message-design.png
[10]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/produce/singleThreadClientVSOriginal.png
[11]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/produce/singleThreadOptionPool.png
[12]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/consume/singleThreadClientVSOriginal.png
[13]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/consume/singleThreadOptionPool.png
[14]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/rabbitmq-offical-screenshot.png
[15]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/banyan.jpg
[16]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/webconsole-dashboard.png
[17]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/webconsole-queueManage.png
