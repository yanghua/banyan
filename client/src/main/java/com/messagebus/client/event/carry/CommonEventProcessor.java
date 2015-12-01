package com.messagebus.client.event.carry;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.common.Constants;
import com.messagebus.common.RandomHelper;
import com.messagebus.common.UUIDGenerator;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by yanghua on 6/25/15.
 */
public abstract class CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(CommonEventProcessor.class);

    private static final Random random = new Random();

    protected void onValidate(CarryEvent event) {
        //todo
    }

    @Subscribe
    public void onMsgIdGenerate(MsgIdGenerateEvent event) {
        logger.debug("=-=-=- event : onMsgIdGenerate =-=-=-");
        MessageContext context = event.getMessageContext();
        Message[]      msgs    = context.getMessages();
        for (Message msg : msgs) {
            UUIDGenerator generator = new UUIDGenerator(random.nextInt(31), Constants.DEFAULT_DATACENTER_ID_FOR_UUID);
            logger.debug("message id is : " + generator.nextId());
            msg.setMessageId(generator.nextId());
            msg.setCorrelationId(context.getSource().getName());
        }
    }

    @Subscribe
    public void onMsgBodySizeCheck(MsgBodySizeCheckEvent event) {
        logger.debug("=-=-=- event : onMsgBodySizeCheck =-=-=-");
        MessageContext     context = event.getMessageContext();
        ConfigManager.Sink sink    = context.getSink();

        if (sink != null && !Strings.isNullOrEmpty(sink.getMsgBodySize())) {
            String msgBodySizeStr = sink.getMsgBodySize();
            int    msgBodySize    = Integer.parseInt(msgBodySizeStr);

            if (msgBodySize != -1) {
                Message[] msgs = context.getMessages();
                for (Message msg : msgs) {
                    if (msg.getContent().length > msgBodySize) {
                        logger.error("message body's size can not be more than : " + msgBodySizeStr
                                + " B, the limit comes from queue name : " + sink.getName());
                        throw new RuntimeException("message body's size can not be more than : " + msgBodySizeStr + " B");
                    }
                }
            }
        }
    }

    @Subscribe
    public void onTagGenerate(TagGenerateEvent event) {
        logger.debug("=-=-=- event : onTagGenerate =-=-=-");
        MessageContext context = event.getMessageContext();
        String         tag     = "consumer.tag." + RandomHelper.randomNumberAndCharacter(6);
        context.setConsumerTag(tag);
    }

    @Subscribe
    public void onMsgBodyCompress(MsgBodyCompressEvent event) {
        //TODO : 不提供自定义功能,有总线决定压缩方式
//        logger.debug("=-=-=- event : onMsgBodyCompress =-=-=-");
//        MessageContext context = event.getMessageContext();
//        String compressAlgo = context.getSink().getCompress();
//        if (!Strings.isNullOrEmpty(compressAlgo)) {
//            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
//            if (compressor != null) {
//                for (Message msg : context.getMessages()) {
//                    if (msg.getHeaders() == null) msg.setHeaders(new HashMap<String, Object>(1));
//                    msg.getHeaders().put(Constants.MESSAGE_HEADER_KEY_COMPRESS_ALGORITHM, compressAlgo);
//                    msg.setContent(compressor.compress(msg.getContent()));
//                }
//            } else {
//                logger.error("the target node with name : " + context.getSink().getName()
//                                 + " configured a compress named : " + compressAlgo
//                                 + " but client can not get the compressor instance. ");
//            }
//        }
    }

    @Subscribe
    public void onPreConsume(PreConsumeEvent event) {
        logger.debug("=-=-=- event : onPreConsume =-=-=-");
        MessageContext context = event.getMessageContext();
        if (!context.isSync()) {
            QueueingConsumer consumer = null;
            try {
                consumer = ProxyConsumer.consume(context.getChannel(),
                        context.getSink().getQueueName(),
                        false,      //auto ack false
                        context.getConsumerTag());
            } catch (IOException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }

            //add external params
            context.getOtherParams().put("consumer", consumer);
        }
    }

    @Subscribe
    public void onAsyncMessageLoop(AsyncMessageLoopEvent event) {
        logger.debug("=-=-=- event : onAsyncMessageLoop =-=-=-");
        MessageContext  context         = event.getMessageContext();
        ExecutorService executor        = Executors.newFixedThreadPool(1);
        Future<?>       messageLoopTask = executor.submit(new MessageLoopTask(context));

        //block until interrupt or timeout
        try {
            messageLoopTask.get(context.getTimeout(), context.getTimeoutUnit());
        } catch (InterruptedException e) {
            logger.info(" consume interrupted!");
        } catch (ExecutionException e) {
            logger.error(" execution exception : ", e);
        } catch (TimeoutException e) {
            logger.info("message loop timeout after : "
                    + context.getTimeout() + " [" + context.getTimeoutUnit() + "]");
        } finally {
            //close the consume based on this channel
            synchronized (context.getChannel()) {
                try {
                    if (context.getChannel().isOpen()) {
                        context.getChannel().basicCancel(context.getConsumerTag());
                    }
                } catch (IOException e1) {
                    logger.error(" occurs a IOException when closing channel : ", e1);
                }
            }
        }
    }

    public abstract void process(MessageContext msgContext);

    public static class MsgIdGenerateEvent extends CarryEvent {
    }

    public static class TagGenerateEvent extends CarryEvent {
    }

    public static class PreConsumeEvent extends CarryEvent {
    }

    public static class MsgBodyCompressEvent extends CarryEvent {
    }

    public static class MsgBodySizeCheckEvent extends CarryEvent {
    }

    public static class AsyncMessageLoopEvent extends CarryEvent {
    }


    /**
     * another message loop task for consume and process message
     */
    private class MessageLoopTask implements Runnable {

        private MessageContext msgContext;

        public MessageLoopTask(MessageContext msgContext) {
            this.msgContext = msgContext;
        }

        @Override
        public void run() {
            QueueingConsumer currentConsumer = (QueueingConsumer) msgContext.getOtherParams().get("consumer");
            int              retryCount      = 0, retryTotalCount = 10;
            boolean          stop            = false;
            try {
                while (!stop) {
                    try {
                        QueueingConsumer.Delivery delivery = currentConsumer.nextDelivery();

                        final Message msg = MessageFactory.createMessage(delivery);

                        if (msg == null) continue;

                        doUncompress(msgContext, msg);

                        msgContext.setConsumeMsgs(new ArrayList<Message>(1) {{
                            this.add(msg);
                        }});

                        process(msgContext);

                        //TODO: 参数化配置
                        msgContext.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (InterruptedException e) {
                        logger.info(" message loop task interrupted!");
                    } catch (ShutdownSignalException e) {
                        retryCount++;
                        if (retryCount >= retryTotalCount) stop = true;
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    protected void validateMessagesProperties(MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (msg.getAppId() == null || msg.getAppId().isEmpty())
                msg.setAppId(context.getSource().getAppId());

            //timestamp
            if (msg.getTimestamp() == 0)
                msg.setTimestamp(currentDate.getTime());
        }
    }

    protected void doUncompress(MessageContext context, Message receivedMsg) {
        //TODO : 不提供自定义功能,有总线决定压缩方式
//        String compressAlgo = context.getSource().getCompress();
//        if (!Strings.isNullOrEmpty(compressAlgo)) {
//            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
//            if (compressor != null) {
//                receivedMsg.setContent(compressor.uncompress(receivedMsg.getContent()));
//            } else {
//                logger.error("the target node with name : " + context.getSink().getName()
//                                 + " configured a compress named : " + compressAlgo
//                                 + " but client can not get the compressor instance. ");
//            }
//        }
    }
}
