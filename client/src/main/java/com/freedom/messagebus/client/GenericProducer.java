package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.message.Message;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.client.model.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

/**
 * a generic producer implements the IProducer interface
 */
class GenericProducer extends AbstractMessageCarryer implements IProducer {

    private static final Log logger = LogFactory.getLog(GenericProducer.class);


    public GenericProducer(GenericContext context) {
        super(MessageCarryType.PRODUCE, context);
    }

    /**
     * simple producer just produces a message
     *
     * @param msg       a general message
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    @Override
    public void produce(@NotNull Message msg,
                        MessageFormat msgFormat,
                        @NotNull String appKey,
                        @NotNull String to,
                        @NotNull String msgType) {
        MessageContext context = this.innerProduce(msgFormat, appKey, to, msgType);
        context.setMessages(new Message[]{msg});
        carry(context);
    }

    /**
     * simple producer just produces a message but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msg       a general message
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    @Override
    public void produceWithTX(@NotNull Message msg,
                              MessageFormat msgFormat,
                              @NotNull String appKey,
                              @NotNull String to,
                              @NotNull String msgType) {
        MessageContext context = this.innerProduce(msgFormat, appKey, to, msgType);
        context.setMessages(new Message[]{msg});
        context.setEnableTransaction(true);
        carry(context);
    }

    /**
     * a producer produces a set of messages
     *
     * @param msgs      a general message's array
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    @Override
    public void batchProduce(@NotNull Message[] msgs,
                             MessageFormat msgFormat,
                             @NotNull String appKey,
                             @NotNull String to,
                             @NotNull String msgType) {
        MessageContext context = this.innerProduce(msgFormat, appKey, to, msgType);
        context.setMessages(msgs);
        carry(context);
    }

    /**
     * a producer produces a set of messages but surrounds with a transaction
     * Note: make sure that your scenario very care about security, otherwise do NOT use it!
     *
     * @param msgs      a general message's array
     * @param msgFormat message's format (object/stream/text/byte/map)
     * @param appKey    the app key which the consumer representation
     * @param to        the message's destination
     * @param msgType   message type (business / system)
     */
    @Override
    public void batchProduceWithTX(@NotNull Message[] msgs,
                                   MessageFormat msgFormat,
                                   @NotNull String appKey,
                                   @NotNull String to,
                                   @NotNull String msgType) {
        MessageContext context = this.innerProduce(msgFormat, appKey, to, msgType);
        context.setMessages(msgs);
        context.setEnableTransaction(true);
        carry(context);
    }

    private MessageContext innerProduce(MessageFormat msgFormat,
                                        @NotNull String appKey,
                                        @NotNull String to,
                                        @NotNull String msgType) {
        MessageContext context = new MessageContext();
        context.setCarryType(MessageCarryType.PRODUCE);
        context.setMsgType(msgType);
        context.setAppKey(appKey);
        context.setRuleValue(to);
        context.setMsgFormat(msgFormat);

        context.setZooKeeper(this.context.getZooKeeper());
        context.setPool(this.context.getPool());
        context.setConfigManager(this.context.getConfigManager());
        context.setConnection(this.context.getConnection());

        return context;
    }
}
