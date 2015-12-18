package com.messagebus.client;

import com.messagebus.client.carry.*;
import com.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * the main operator of messagebus client
 */
public class Messagebus extends InnerClient implements IProducer, IConsumer,
        IRequester, IResponser,
        IRpcRequester, IRpcResponser,
        IPublisher, ISubscriber,
        IBroadcaster {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private IProducer     producer;
    private IConsumer     consumer;
    private IPublisher    publisher;
    private ISubscriber   subscriber;
    private IRequester    requester;
    private IResponser    responser;
    private IRpcRequester rpcRequester;
    private IRpcResponser rpcResponser;
    private IBroadcaster  broadcaster;

    private Messagebus() {
        super();

        producer = CarryFactory.createProducer(context);
        consumer = CarryFactory.createConsumer(context);
        publisher = CarryFactory.createPublisher(context);
        subscriber = CarryFactory.createSubscriber(context);
        requester = CarryFactory.createRequester(context);
        responser = CarryFactory.createResponser(context);
        rpcRequester = CarryFactory.createRpcRequester(context);
        rpcResponser = CarryFactory.createRpcResponser(context);
        broadcaster = CarryFactory.createBroadcaster(context);
    }

    /**
     * 生产消息
     *
     * @param secret 通过管控台申请的自身队列标识
     * @param to     目标队列名称（必须通过管控台申请与目标队列的通信权限，否则无法通信）
     * @param msg    待发送的消息对象
     * @param token  自身队列跟目标队列通信需要的token，用于校验单向通信是否被授权
     */
    @Override
    public void produce(String secret, String to, Message msg, String token) {
        producer.produce(secret, to, msg, token);
    }

    /**
     * 批量生产消息
     *
     * @param secret 通过管控台申请的自身队列标识
     * @param to     目标队列名称（必须通过管控台申请与目标队列的通信权限，否则无法通信）
     * @param msgs   待发送的消息对象集合
     * @param token  自身队列跟目标队列通信需要的token，用于校验单向通信是否被授权
     */
    @Override
    public void batchProduce(String secret, String to, Message[] msgs, String token) {
        producer.batchProduce(secret, to, msgs, token);
    }

    /**
     * 实时消息消费(Push)，该消息消费API被设计为阻塞式的，具体的阻塞时间依赖<param>timeout</param>,
     * 以及<param>TimeUnit</param>这两个参数确定。被阻塞期间，会产生一定时间内的消息消费的事件处理循环。
     * 在这段时间内，只要总线的队列里有消息，都会被推送到客户端来。因此该API的实用场景为：实时消费。
     * 需要注意的是，如果实时消费的时间较长，请将<param>timeout</param>的值设置为: Integer.MAX_VALUE。
     * 并在一个独立的线程上进行异步消费，然后将该线程的控制权外放给主线程或其他控制线程。
     * 另外如果实时消费处于web环境下，推荐将消费线程的启停通过继承<code>ServletContextListener</code>
     * 类实现一个特定的Listener来加以控制。
     *
     * @param secret    通过管控台申请的自身队列标识
     * @param timeout   阻塞超时时间值，该值必须与参数<param>unit</param>一起参考才有意义
     * @param unit      阻塞超时时间的单位，该值必须与参数<param>timeout</param>一起参考才有意义
     * @param onMessage 收到消息后触发的事件，用于实现自己的消息处理实现
     */
    @Override
    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage) {
        consumer.consume(secret, timeout, unit, onMessage);
    }

    /**
     * 同步获取式(Pull)消息消费API，该API提供同步获取<param>expectedNum</param>条消息的功能
     * 需要注意的是，该参数<param>expectedNum</param>为单次调用能够获取到的最大值
     *
     * @param secret      通过管控台申请的自身队列标识
     * @param expectedNum 希望获取到的消息条数
     * @return 消费到的消息对象的集合
     */
    @Override
    public List<Message> consume(String secret, int expectedNum) {
        return consumer.consume(secret, expectedNum);
    }

    /**
     * 请求消息API，该接口用于实现常用C/S或B/S架构的request/response的request方，
     * 当消息发送后，它阻塞等待响应消息，直到：
     * - 收到响应
     * - 等待指定的时间后超时（抛出MessageResponseTimeoutException异常）
     *
     * @param secret  通过管控台申请的自身队列标识
     * @param to      目标队列的队列名称
     * @param msg     请求消息对象
     * @param token   自身队列跟目标队列通信需要的token，用于校验单向通信是否被授权
     * @param timeout 指定的超时时间，默认单位为秒
     * @return 获取到的响应消息对象
     * @throws MessageResponseTimeoutException 当超过指定的<param>timeout</param>时间后，
     *                                         仍未收到响应，将抛出该异常
     */
    @Override
    public Message request(String secret, String to, Message msg, String token, long timeout)
            throws MessageResponseTimeoutException {
        return requester.request(secret, to, msg, token, timeout);
    }

    /**
     * 应答/响应API，该接口用于实现request/response的response方，因为response方通常需要一个事件处理循环，
     * 所以该API也是阻塞式的，阻塞的时间依据参数<param>timeout</param>以及<param>timeUnit</param>联合确定。
     * 如果阻塞的时间非常长，请将<param>timeout</param>设置为Integer.MAX_VALUE，
     * 然后以一个独立的事件处理线程来控制它，并将该事件处理线程的控制权暴露给主控线程
     *
     * @param secret    通过管控台申请的自身队列标识
     * @param onRequest 当前请求消息到来时触发的时间，用于获取请求消息，并已返回值的方式返回响应消息
     * @param timeout   阻塞超时时间值，该值必须与参数<param>unit</param>一起参考才有意义
     * @param timeUnit  阻塞超时时间的单位，该值必须与参数<param>timeout</param>一起参考才有意义
     */
    @Override
    public void response(String secret, IRequestListener onRequest, long timeout, TimeUnit timeUnit) {
        responser.response(secret, onRequest, timeout, timeUnit);
    }

    /**
     * 发布消息API，该接口会将消息发布给所有通过管控台申请的所有订阅者
     *
     * @param secret 通过管控台申请的自身队列标识
     * @param msgs   要发布的消息数组
     */
    @Override
    public void publish(String secret, Message[] msgs) {
        publisher.publish(secret, msgs);
    }

    /**
     * 订阅消息API，这里假设订阅的场景具有较强的实时性，因此它以阻塞式的事件方式提供调用
     * 阻塞的时间依据参数<param>timeout</param>以及<param>timeUnit</param>联合确定。
     * 如果阻塞的时间非常长，请将<param>timeout</param>设置为Integer.MAX_VALUE，
     * 然后以一个独立的处理线程来控制它，并将该处理线程的控制权暴露给主控线程
     *
     * @param secret    通过管控台申请的自身队列标识
     * @param from
     * @param token
     * @param onMessage 订阅某个频道队列(Channel Queue)后，收到消息的事件处理器
     * @param timeout   阻塞超时时间值，该值必须与参数<param>unit</param>一起参考才有意义
     * @param unit      阻塞超时时间的单位，该值必须与参数<param>timeout</param>一起参考才有意义
     */
    @Override
    public void subscribe(String secret, String from, String token, IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        subscriber.subscribe(secret, from, token, onMessage, timeout, unit);
    }

    /**
     * 广播消息的接口，广播权限需要从管控台申请
     *
     * @param secret 通过管控台申请的自身队列标识
     * @param msgs   待广播的消息对象数组
     */
    @Override
    public void broadcast(String secret, Message[] msgs) {
        broadcaster.broadcast(secret, msgs);
    }

    /**
     * 广播消息接收API，注意：不是每个队列都能收到广播消息。通常情况下，
     * 只有能获取消息的队列才能收到广播消息，广播消息不是必须要求消费的，但推荐在调用消息总线的所有API时，
     * 设置广播消息处理器。如果不设置该处理器，在收到广播消息时，广播消息将被丢弃！
     *
     * @param notificationListener 广播消息接收处理器
     */
    @Deprecated
    public void setNotificationListener(IMessageReceiveListener notificationListener) {
        throw new UnsupportedOperationException("this interface has been deprecated! ");
    }

    /**
     * 支持JSON协议的远程过程调用(JSON-RPC)的请求接口，用于请求一个远程方法，并指定一个获取响应的超时时间。
     * 注意，RPC的请求方需要通过管控台授权，拿到token才能发起请求，另外参数顺序必须与目标方法的参数顺序一致。
     *
     * @param secret               通过管控台申请的自身队列标识
     * @param target               目标队列
     * @param methodName           目标方法名称
     * @param params               目标方法的处理参数
     * @param token                自身队列跟目标队列通信需要的token，用于校验单向通信是否被授权
     * @param timeoutOfMilliSecond 超时的毫秒数
     * @return 远程方法调用的返回值，通常只支持：String/Object/Map这些简单java原生对象格式
     */
    @Override
    public Object call(String secret, String target, String methodName,
                       Object[] params, String token, long timeoutOfMilliSecond) {
        return rpcRequester.call(secret, target, methodName, params, token, timeoutOfMilliSecond);
    }

    /**
     * 支持JSON协议的远程过程调用(JSON-RPC)的响应接口，用于作为一个远程方法的响应方，因此为阻塞式事件处理形式。
     * 阻塞的时间依据参数<param>timeout</param>以及<param>timeUnit</param>联合确定。
     * 如果阻塞的时间非常长，请将<param>timeout</param>设置为Integer.MAX_VALUE，
     * 然后以一个独立的处理线程来控制它，并将该处理线程的控制权暴露给主控线程
     *
     * @param secret           通过管控台申请的自身队列标识
     * @param clazzOfInterface 用于响应处理的接口的Class实例
     * @param serviceProvider  用于响应处理的服务提供者对象，该对象必须实现<param>clazzOfInterface</param>对应的接口
     * @param timeout          阻塞超时时间值，该值必须与参数<param>unit</param>一起参考才有意义
     * @param timeUnit         阻塞超时时间的单位，该值必须与参数<param>timeout</param>一起参考才有意义
     */
    @Override
    public void callback(String secret, Class<?> clazzOfInterface,
                         Object serviceProvider, long timeout, TimeUnit timeUnit) {
        rpcResponser.callback(secret, clazzOfInterface, serviceProvider, timeout, timeUnit);
    }

    /**
     * 原始数据格式请求API，该API为request的原始形式，另一个作用是辅助实现以消息总线作为底层通信的
     * 第三方RPC框架的接入。具体实现可参考package:
     * com.messagebus.client.extension.thrift.TAMQPClientTransport
     *
     * @param secret               通过管控台申请的自身队列标识
     * @param target               目标队列
     * @param requestMsg           请求消息被序列化后的字节数组
     * @param token                自身队列跟目标队列通信需要的token，用于校验单向通信是否被授权
     * @param timeoutOfMilliSecond 等待响应的超时时间(毫秒数)
     * @return 响应消息被序列化后的字节数组
     */
    @Override
    public byte[] primitiveRequest(String secret, String target, byte[] requestMsg,
                                   String token, long timeoutOfMilliSecond) {
        return requester.primitiveRequest(secret, target, requestMsg, token, timeoutOfMilliSecond);
    }

    /**
     * 用于构建RPC响应服务器的帮助方法，该方法用于辅助第三方RPC框架来构造用于消息总线的通信方式以及事件处理循环
     *
     * @param secret          通过管控台申请的自身队列标识
     * @param rpcMsgProcessor 一个RPC消息处理器，当有RPC请求消息到来时触发
     * @return 返回一个被包装过的RPCServer
     */
    @Override
    public WrappedRpcServer buildRpcServer(String secret, IRpcMessageProcessor rpcMsgProcessor) {
        return rpcResponser.buildRpcServer(secret, rpcMsgProcessor);
    }

    /**
     * 注册消息总线支持的事件处理器；事件处理器对象遵循Guava的事件订阅模式（@Subscribe）
     *
     * @param eventProcessor 事件处理器对象的实例
     */
    public void registerEventProcessor(Object eventProcessor) {
        componentEventBus.register(eventProcessor);
    }


    /**
     * 注销消息总线支持的事件处理器；
     *
     * @param eventProcessor 事件处理器对象的实例
     */
    public void unregisterEventProcessor(Object eventProcessor) {
        componentEventBus.unregister(eventProcessor);
    }

}
