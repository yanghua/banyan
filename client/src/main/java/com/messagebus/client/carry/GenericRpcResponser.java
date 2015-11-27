package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.IRpcMessageProcessor;
import com.messagebus.client.MessageContext;
import com.messagebus.client.WrappedRpcServer;
import com.messagebus.client.event.carry.RpcResponseEventProcessor;
import com.messagebus.client.model.MessageCarryType;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.RpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 4/8/15.
 */
public class GenericRpcResponser extends AbstractMessageCarryer implements IRpcResponser {

    private static final Log logger = LogFactory.getLog(GenericRpcResponser.class);

    public GenericRpcResponser() {
    }

    @Override
    public void callback(String secret, Class<?> clazzOfInterface, Object serviceProvider, long timeout, TimeUnit timeUnit) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.RPCRESPONSE);
        ctx.setSink(this.getContext().getConfigManager().getSinkBySecret(secret));
        Map<String, Object> otherParams = ctx.getOtherParams();
        otherParams.put("serviceProvider", serviceProvider);
        otherParams.put("clazzOfInterface", clazzOfInterface);
        ctx.setTimeout(timeout);
        ctx.setTimeoutUnit(timeUnit);

        this.innerRpcResponse(ctx);
    }

    @Override
    public WrappedRpcServer buildRpcServer(String secret, final IRpcMessageProcessor rpcMsgProcessor) {
        ConfigManager.Sink sink = this.getContext().getConfigManager().getSinkBySecret(secret);
        try {
            RpcServer aServer = new RpcServer(this.getContext().getChannel(), sink.getQueueName()) {

                @Override
                public byte[] handleCall(QueueingConsumer.Delivery request, AMQP.BasicProperties replyProperties) {
                    return rpcMsgProcessor.onRpcMessage(request.getBody());
                }

            };

            Constructor<WrappedRpcServer> rpcServerConstructor = WrappedRpcServer.class.getDeclaredConstructor(RpcServer.class);
            rpcServerConstructor.setAccessible(true);
            WrappedRpcServer wrappedRpcServer = rpcServerConstructor.newInstance(aServer);
            rpcServerConstructor.setAccessible(false);

            return wrappedRpcServer;
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    private void innerRpcResponse(MessageContext ctx) {
        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        RpcResponseEventProcessor eventProcessor = new RpcResponseEventProcessor();
        carryEventBus.register(eventProcessor);

        RpcResponseEventProcessor.RpcResponseEvent rpcResponseEvent = new RpcResponseEventProcessor.RpcResponseEvent();

        rpcResponseEvent.setMessageContext(ctx);
        carryEventBus.post(rpcResponseEvent);
        carryEventBus.unregister(eventProcessor);
    }

}
