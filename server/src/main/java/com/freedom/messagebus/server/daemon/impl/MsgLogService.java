package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

@DaemonService(value = "msgLogService", policy = RunPolicy.ONCE)
public class MsgLogService extends AbstractInitializer implements Runnable, IService {

    private static final Log logger = LogFactory.getLog(MsgLogService.class);

    public MsgLogService(String host) {
        super(host);
    }

    @Override
    public void run() {
        try {
            super.init();
            QueueingConsumer consumer = ProxyConsumer.consume(this.channel, CONSTS.DEFAULT_CONSOLE_QUEUE_NAME);
            if (consumer == null)
                throw new IOException(" consumer is null ");

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                AMQP.BasicProperties properties = delivery.getProperties();
                byte[] msgBody = delivery.getBody();

                String msgTypeStr = properties.getType();
                if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                    logger.error("[run] message type is null or empty");
                }

                MessageType msgType = MessageType.lookup(msgTypeStr);
                Message msg = new Message();
                initMessage(msg, msgType, properties, msgBody);

                logger.info("[message-id] : " + msg.getMessageHeader().getMessageId());

                this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            logger.error("[run] occurs a IOException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.info(" consumer closed! ");
        } finally {
            try {
                super.close();
            } catch (IOException e) {
                logger.error("[run] occurs a IOException : " + e.getMessage());
            }
        }
    }

    private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
        msg.setMessageHeader(MessageHeaderProcessor.unbox(properties, msgType));
        msg.setMessageType(msgType);

        IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msgType);
        msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
    }
}
