#overview
httpbridge 如其名，它是消息总线接口基于http的实现。用于衔接各种异构系统。

> 说明：请尽可能使用messagebus4java-client

##Restful

###produce：

```
/messagebus/queues/{qname}/messages?secret={secret}&token={token}&apiType={apiType}
```

http method : `POST`

request params :

* path : qname - 队列名称 （必填）
* querystring : 
	* secret - 自身标识 （必填）
	* token - 授权token （必填）
	* apiType - 鉴别API，值为 `produce` （必填）
* request body : 
	* messages - 消息对象数组 （必填）
	
message对象键值对：

```js
{
	messageId: 582793753824251900,
	type: "queue",
	timestamp: "Mar 31, 2015 2:37:13 PM",
	priority: 0,
	expiration: null,
	headers: null,
	contentEncoding: null,
	contentType: "text/plain",
	replyTo: "emapDemoConsume",
	appId: "4",
	userId: null,
	clusterId: null,
	correlationId: "erpDemoProduce",
	deliveryMode: 2,
	msgType: "QueueMessage",
	content: "test"
}
```
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```

###consume:

```
/messagebus/queues/messages?secret={secret}&apiType={apiType}&mode={sync}&num={num}
```

http method : `GET`

request params : 

* querystring : 
	* secret - 自身标识 （必填）
	* apiType - 鉴别API，值为 `consume` （必填）
	* mode - 取值 `pull` 或 `push` （必填）
	* num - 希望获取的消息数目，范围 0 < num <=100 （mode 为`pull`时有效）
	
response : 

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: [
		{
			messageHeader: {
				messageId: 520133271997313000,
				type: "appMessage",
				timestamp: null,
				priority: 0,
				expiration: null,
				deliveryMode: 2,
				headers: null,
				contentEncoding: null,
				contentType: null,
				replyTo: null,
				appId: null,
				userId: null,
				clusterId: null,
				correlationId: null
			},
			messageBody: {
				messageBody: [
					116,
					101,
					115,
					116
				]
			},
			messageType: "AppMessage"
		}
	]
}

```

> num 只在mode为 `pull`时有效，并且num跟最终返回response中返回的数目 **不一定相等** (小于或等于num)
> 在 `push` 模式下，一次长连接最长有效时间为 **60s**（该值目前为服务端指定，客户端不可自定义）；客户端在两种情况下才应该再次发起长连接请求：
> 
> * 客户端收到服务端回复的超时响应
> * 客户端收到消息响应

###publish
```
/messagebus/queues/messages?secret={secret}&apiType={apiType}
```

http method : `POST` 

request params : 

* querystring :
	* secret - 自身标识 （必填）
	* apiType - 鉴别API，值为 `publish` (必填)
* request body :
	* messages - 推送消息对象集合
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```

###subscribe
```
/messagebus/queues/messages?secret={secret}&apiType={apiType}
```

http method : `GET`

request params :

* querystring :
	* secret - 自身标识（必填）
	* apiType - 鉴别API，值为 `subscribe` (必填)

response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: [
		{
			messageHeader: {
				messageId: 520133271997313000,
				type: "appMessage",
				timestamp: null,
				priority: 0,
				expiration: null,
				deliveryMode: 2,
				headers: null,
				contentEncoding: null,
				contentType: null,
				replyTo: null,
				appId: null,
				userId: null,
				clusterId: null,
				correlationId: null
			},
			messageBody: {
				messageBody: [
					116,
					101,
					115,
					116
				]
			},
			messageType: "AppMessage"
		}
	]
}
```

> subscribe 目前只提供push模式，一次调用，当前默认失效时间为60秒，客户端不可设置


###request:

```
/messagebus/queues/{qname}/messages?secret={secret}&token={token}&apiType={apiType}&timeout={timeout}
```

http method : `POST`

request params :

* path : qname - 队列名称
* querystring : 
	* secret - 自身标识 （必填）
	* token - 授权token (必填)
	* apiType - 鉴别API，值为 `request`（必填）
	* timeout - 超时时间，单位为秒（必填）
* request body : 
	* message - 消息对象 （客户端为阻塞等待，每次只能请求一条）
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: {
			messageHeader: {
				messageId: 520133271997313000,
				type: "appMessage",
				timestamp: null,
				priority: 0,
				expiration: null,
				deliveryMode: 2,
				headers: null,
				contentEncoding: null,
				contentType: null,
				replyTo: null,
				appId: null,
				userId: null,
				clusterId: null,
				correlationId: null
			},
			messageBody: {
				messageBody: [
					116,
					101,
					115,
					116
				]
			},
			messageType: "AppMessage"
	}
}
```

> request 用于模拟req / resp模型，它收到的response是另一个client通过下面的response接口发送的。它用于立即等待处理结果的模型，而是否发送响应，这取决于目标队列的处理器，因此它提供一个timeout来避免无限等待。


##技术实现
### messagebus-client 的代理
messagebus-httpbridge 用于为messagebus提供http访问的支持。本质上，httpbridge的server相当于一个 **代理服务器**，在server内部也是通过调用 `messagebus-client` 来实现与messagebus的交互。

### jetty continuation
作为一种轻量级java web container。jetty拥有一些不错的特性，比如高度的模块化、扩展性强等。但选择jetty的主要原因还是来自于它的 `continuation` 技术能更好得应对 `server push` 模型(长连接，comet)。这里它用于 `consume`的 `async`模型。

jetty 的 continuation mod 支持异步的servlet，它支持对同一个请求dispatch多次，这样就不必建立thread-request map可以在waiting的时候快速将线程返回到线程池。

