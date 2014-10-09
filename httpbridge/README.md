#overview
httpbridge 如其名，它是消息总线接口基于http的实现。用于衔接各种异构系统。

> 说明：请尽可能使用messagebus4java-client

##Restful

###produce：

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={produce}
```

http method : `POST`

request params :

* path : qname - 队列名称 （必填）
* querystring : 
	* appkey - 授权key （必填）
	* type - 鉴别API，值为 `produce` （必填）
* request body : 
	* messages - 消息对象列表 （必填）
	
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
/messagebus/queues/{qname}/messages?appkey={appkey}&type={consume}&mode={sync}&num={num}
```

http method : `GET`

request params : 

* path : qname - 队列名称
* querystring : 
	* appkey - 授权key （必填）
	* type - 鉴别API，值为 `consume` （必填）
	* mode - 取值 `sync` 或 `async` （必填）
	* num - 希望获取的消息数目，范围 0 < num <=100 （mode 为 sync时有效）
	
response : 

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: [
		{
			messageHeader: {
				messageId: 518977236485992450,
				type: "appMessage",
				priority: 0,
				deliveryMode: 2
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

> consume 模式为`sync`时，对应于 `pull` 模式，请求属于普通的http请求，而模式为`async`是则为长连接请求，对应于`push`模式，请求参数：num 只在mode为 `sync`时有效，并且num跟最终返回response中返回的数目 **不一定相等** 
> 在 `async` 模式下，一次长连接最长有效时间为 **60s**（该值目前为服务端指定，客户端不可自定义）；客户端在两种情况下会再次发起长连接请求：
> 
> * 客户端收到服务端回复的超时响应
> * 客户端收到消息响应


###request:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={type}&timeout={timeout}
```

http method : `POST`

request params :

* path : qname - 队列名称
* querystring : 
	* appkey - 授权key（必填）
	* type - 鉴别API，值为 `request`（必填）
	* timeout - 超时时间，单位为毫秒（必填）
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
				messageId: 518977236485992450,
				type: "appMessage",
				priority: 0,
				deliveryMode: 2
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

###response:

```
/messagebus/queues/{qname}/messages?appkey={appkey}
```
http method : `POST`

request params : 

* path : qname - 队列名称(此处应为以消息Id命名的临时队列)
* querystring : 
	* appkey - 授权key（必填）
	* type - 鉴别API，值为 `response`（必填）
* request body :
	* message - 消息对象	
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```
	
> response 跟上面的request是同一个模型的另一端，需要注意的是，这里的response指的是 **发送响应** 而不是接收响应！


##技术实现
### messagebus-client 的代理
messagebus-httpbridge 用于为messagebus提供http访问的支持。本质上，httpbridge的server相当于一个 **代理服务器**，在server内部也是通过调用 `messagebus-client` 来实现与messagebus的交互。

### jetty （暂未实现）
作为一种轻量级java web container。jetty拥有一些不错的特性，比如高度的模块化、扩展性强等。但选择jetty的主要原因还是来自于它的 `continuation` 技术能更好得应对 `server push` 模型(长连接，comet)。这里它用于 `consume`的 `async`模型。


