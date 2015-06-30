package com.messagebus.client.event.carry;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.common.compress.CompressorFactory;
import com.messagebus.common.compress.ICompressor;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanghua on 6/26/15.
 */
public class PublishEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(PublishEventProcessor.class);

    public PublishEventProcessor() {
    }

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
        MessageContext context = event.getMessageContext();
        if (!context.getCarryType().equals(MessageCarryType.PUBLISH)) {
            throw new RuntimeException("message carry type should be publish ");
        }

        this.validateMessagesProperties(context);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext context = event.getMessageContext();
        Node sourceNode = context.getSourceNode();
        boolean hasPermission = true;

        hasPermission = sourceNode.getCommunicateType().equals("publish")
            || sourceNode.getCommunicateType().equals("publish-subscribe");

        if (!hasPermission) {
            throw new RuntimeException("can not publish message! maybe the communicate is error. "
                                           + " secret is : " + context.getSecret());
        }
    }

    @Subscribe
    public void onPublishFiltrate(FiltrateEvent event) {
        logger.debug("=-=-=- event : onPublishFiltrate =-=-=-");
        MessageContext context = event.getMessageContext();
        List<Node> pushToNodes = context.getConfigManager()
                                        .getNodeView(context.getSecret())
                                        .getSubscribeNodes();
        if (pushToNodes == null || pushToNodes.size() == 0) return;

        context.getOtherParams().put("publishList", pushToNodes);
    }

    @Subscribe
    public void onPublish(PublishEvent event) {
        logger.debug("=-=-=- event : onPublish =-=-=-");
        MessageContext context = event.getMessageContext();
        try {
            List<Node> publishNodes = (List<Node>) context.getOtherParams().get("publishList");
            for (Node node : publishNodes) {
                for (Message msg : context.getMessages()) {
                    //process message content (compress)
                    Message compressedMsg = this.doCompress(msg, node);
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(compressedMsg);

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          node.getRoutingKey(),
                                          compressedMsg.getContent(),
                                          properties);
                }
            }
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        } catch (CloneNotSupportedException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    private Message doCompress(Message msg, Node Subscriber) throws CloneNotSupportedException {
        String compressAlgo = Subscriber.getCompress();
        if (!Strings.isNullOrEmpty(compressAlgo)) {
            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
            if (compressor != null) {
                Message clonedMsg = (Message) msg.clone();
                if (clonedMsg.getHeaders() == null) clonedMsg.setHeaders(new HashMap<String, Object>(1));

                clonedMsg.getHeaders().put(Constants.MESSAGE_HEADER_KEY_COMPRESS_ALGORITHM, compressAlgo);
                clonedMsg.setContent(compressor.compress(clonedMsg.getContent()));

                return clonedMsg;
            } else {
                logger.error("the target subscriber with name : " + Subscriber.getName()
                                 + " configured a compress named : " + compressAlgo
                                 + " but client can not get the compressor instance. ");
            }
        }

        return msg;
    }


    //region publish events definition
    public static class ValidateEvent extends CarryEvent {
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class FiltrateEvent extends CarryEvent {
    }

    public static class PublishEvent extends CarryEvent {
    }
    //endregion

}
