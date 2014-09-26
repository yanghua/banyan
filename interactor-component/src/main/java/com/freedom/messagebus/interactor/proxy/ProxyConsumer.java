package com.freedom.messagebus.interactor.proxy;

import com.freedom.messagebus.common.IMessageReceiveListener;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProxyConsumer implements Runnable {

    private static final Log logger = LogFactory.getLog(ProxyConsumer.class);

    @NotNull
    private Thread                  currentThread;
    private Channel                 channel;
    private String                  queueName;
    private IMessageReceiveListener listener;
    private QueueingConsumer        consumer;

    public ProxyConsumer() {
        currentThread = new Thread(this);
        this.currentThread.setDaemon(false);
    }

    @Override
    public void run() {
        if (consumer == null) {
            logger.error("[run] consumer object is null");
            return;
        }

        try {
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                AMQP.BasicProperties properties = delivery.getProperties();
                byte[] msgBody = delivery.getBody();

                String msgTypeStr = properties.getType();
                if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                    logger.error("[run] message type is null or empty");
                    continue;
                }

                MessageType msgType = MessageType.lookup(msgTypeStr);
                Message msg = new Message();
                initMessage(msg, msgType, properties, msgBody);

                listener.onMessage(msg);

                this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (InterruptedException e) {
            logger.info("[run] close the consumer's message handler!");
        } catch (IOException e) {
            logger.error("[run] occurs a IOException : " + e.getMessage());
        } catch (ConsumerCancelledException e) {
            logger.info("[run] the consumer has been canceled ");
        } catch (Exception e) {
            logger.error("[run] occurs a Exception : " + e.getMessage());
        }

        logger.info("******** thread id " + this.currentThread.getId() + " quit from message receiver ********");
    }

    public void consume(@NotNull Channel channel,
                        @NotNull String queueName,
                        IMessageReceiveListener listener) throws IOException {
        this.channel = channel;
        this.queueName = queueName;
        this.listener = listener;
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);
        this.consumer = consumer;

        //launch consume event loop!!!
        this.currentThread.start();
    }

    public void shutdown() {
        this.currentThread.interrupt();
    }

    protected long getThreadID() {
        return this.currentThread.getId();
    }

    private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
        msg.setMessageHeader(MessageHeaderProcessor.unbox(properties, msgType));
        msg.setMessageType(msgType);

        IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msgType);
        msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
    }

    /**
     * common consume mostly for customization
     *
     * @param channel
     * @param queueName
     * @return
     * @throws IOException
     */
    public static QueueingConsumer consume(@NotNull Channel channel,
                                           @NotNull String queueName) throws IOException {
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);
        return consumer;
    }

}
