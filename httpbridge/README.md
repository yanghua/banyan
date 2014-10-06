#overview
httpbridge 如其名，它是消息总线接口基于http的实现。用于衔接各种异构系统。

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

```
```

###consume:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={consume}&mode={sync}&num={num}&timeout={timeout}
```

http method : `GET`

request params : 

* path : qname - 队列名称
* querystring : 
	* appkey - 授权key （必填）
	* type - 鉴别API，值为 `consume` （必填）
	* mode - 取值 `sync` 或 `async` （必填）
	* num - 希望获取的消息数目，范围 0 < num <=100 （mode 为 sync时有效）
	* timeout - 超时时间，单位为毫秒 （mode 为 sync时有效）
	
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

> consume 模式为`sync`时，请求属于普通的http请求，而模式为`async`是则为长连接请求，请求参数：num 与 timeout 只在mode为 `sync`时有效，并且num跟最终返回response中返回的数目 **不一定相等**


###request:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&timeout={timeout}
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

```

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
	
> response 跟上面的request是同一个模型的另一端，需要注意的是，这里的response指的是 **发送响应** 而不是接收响应！


##技术实现

