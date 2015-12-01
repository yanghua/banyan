package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.common.Constants;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.messagebus.interactor.rabbitmq.QueueManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yanghua on 6/27/15.
 */
public class RequestEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(RequestEventProcessor.class);

    public RequestEventProcessor() {
    }

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
        MessageContext context = event.getMessageContext();

        if (!context.getCarryType().equals(MessageCarryType.REQUEST)) {
            logger.error("message carry type should be request");
            throw new RuntimeException("message carry type should be request");
        }

        if (!MessageCarryType.lookup(context.getSource().getType()).equals(MessageCarryType.REQUEST)) {
            logger.error("message carry type should be request");
            throw new RuntimeException("message carry type should be request");
        }

        Message[] msgs = context.getMessages();
        if (msgs == null || msgs.length != 1) {
            logger.error(" request message must be just one! ");
            throw new RuntimeException(" request message must be just one! ");
        }

        if (context.getSink() == null) {
            logger.error(" sink can not be null. ");
            throw new RuntimeException(" sink can not be null. ");
        }

        this.validateMessagesProperties(context);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext context = event.getMessageContext();
        ConfigManager.Source source = context.getSource();
        ConfigManager.Sink sink = context.getSink();
        String token = context.getToken();

        boolean hasPermission = false;

        ConfigManager.Stream stream = context.getStream();
        hasPermission = stream != null && stream.getToken().equals(token)
            && stream.getSourceSecret().equals(source.getSecret())
            && stream.getSinkSecret().equals(sink.getSecret());

        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + source.getName() +
                             "] to queue [" + sink.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + source.getName() +
                                           "] to queue [" + sink.getName() + "]");
        }
    }

    @Subscribe
    public void onTempQueueInitialize(TempQueueInitializeEvent event) {
        logger.debug("=-=-=- event : onTempQueueInitialize =-=-=-");
        MessageContext context = event.getMessageContext();
        String correlationId = context.getSource().getName();
        context.getMessages()[0].setCorrelationId(correlationId);
        QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
        try {
            queueManager.create(correlationId);
        } catch (IOException e) {
            logger.error(" occurs a IOException : ", e);
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onRequest(RequestEvent event) {
        logger.debug("=-=-=- event : onRequest =-=-=-");
        MessageContext context = event.getMessageContext();
        Message reqMsg = context.getMessages()[0];
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(reqMsg);
        try {
            ProxyProducer.produceWithTX(Constants.PROXY_EXCHANGE_NAME,
                                        context.getChannel(),
                                        context.getSink().getRoutingKey(),
                                        reqMsg.getContent(),
                                        properties);
        } catch (IOException e) {
            logger.error(" occurs a IOException when sending a request : ", e);
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onBlockAndTimeout(BlockAndTimeoutResponseEvent event) {
        logger.debug("=-=-=- event : onBlockAndTimeout =-=-=-");
        MessageContext context = event.getMessageContext();
        String correlationId = context.getMessages()[0].getCorrelationId();

        try {
            //just receive one
            QueueingConsumer consumer = ProxyConsumer.consume(
                    context.getChannel(),
                    correlationId,
                    true,
                    context.getConsumerTag());
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(context.getTimeout() * 1000);

            //timeout
            if (delivery == null) {
                context.setIsTimeout(true);
                return;
            }

            final Message msg = MessageFactory.createMessage(delivery);

            if (msg == null) return;

            context.setConsumeMsgs(new ArrayList<Message>(1) {{
                this.add(msg);
            }});
        } catch (IOException e) {
            logger.error("[handle] occurs a exception : ", e);
        } catch (InterruptedException e) {
        } finally {
            //delete temp queue
            QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
            String msgIdStr = String.valueOf(correlationId);
            try {
                if (queueManager.exists(msgIdStr)) {
                    queueManager.delete(msgIdStr);
                }
            } catch (IOException e) {
                logger.error("[handle] finally block occurs a IOException : ", e);
            }
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    //region event definition
    public static class ValidateEvent extends CarryEvent {
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class TempQueueInitializeEvent extends CarryEvent {
    }

    public static class RequestEvent extends CarryEvent {
    }

    public static class BlockAndTimeoutResponseEvent extends CarryEvent {
    }
    //endregion

}
