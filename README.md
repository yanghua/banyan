#overview
message bus can be used to communicate and integrate over multi-app. It used a [RabbitMQ](http://www.rabbitmq.com/) as backend broker(message exchanger). Most scenario:

* enterprise information Integration
* oriented-component & oriented-module distributed developer
* provide support for simple esb or soa

the necessity of encapsulating with RabbitMQ:

* provide router pattern
* embed permission into client-jar
* removed create & delete & update operation from client, replaced with central-register mode


##router with tree topology structure
the message bus's implementation is based on Rabbitmq. It can takes advantage of multiple message exchange-types rabbitmq provided and builds many kinds of router pattern. The message bus's router topology lists below:


![img 3][3]

the advantages of the tree topology:

* hide the router topology from client-caller (just need to know the `proxy` node)
* multiple message communication pattern (p2p, pub/sub, broadcast)
* implement the message log without interrupting the message channel
* communication-policy configure once , push everywhere


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/module-dependency.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/router-topology.png