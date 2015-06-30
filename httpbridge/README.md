#Restful API
##produce：

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={produce}
```

http method : `POST`

request params :

* path : qname - queue name
* querystring : 
	* appkey - auth key （must）
	* type - identify API，value `produce` （must）
* request body : 
	* messages - message object list （must）
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```

##consume:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={consume}&mode={sync}&num={num}
```

http method : `GET`

request params : 

* path : qname - queue name
* querystring : 
	* appkey - auth key （must）
	* type - identify API，value `consume` （must）
	* mode - value `sync` or `async` （must）
	* num - except num，from  0 < num to 100(equals) （mode must be sync）
	
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

##request:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={type}&timeout={timeout}
```

http method : `POST`

request params :

* path : qname - queue name
* querystring : 
	* appkey - auth key（must）
	* type - identify API，value `request`（must）
	* timeout - timeout，unit microsecond（must）
* request body : 
	* message - message object （client blocked and just once）
	
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


##response:

```
/messagebus/queues/{qname}/messages?appkey={appkey}
```
http method : `POST`

request params : 

* path : qname - queue name
* querystring : 
	* appkey - auth key（must）
	* type - identify API，value `response`（must）
* request body :
	* message - message object
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```
