package com.freedom.messagebus.client.handler.publish;

import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class PublishParamValidator extends AbstractParamValidator {

    private static Log logger = LogFactory.getLog(PublishParamValidator.class);

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        super.handle(context, chain);

        this.validateMessageProperties(context);

        chain.handle(context);
    }

    private void validateMessageProperties(@NotNull MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (msg.getMessageHeader().getAppId() == null || msg.getMessageHeader().getAppId().isEmpty())
                msg.getMessageHeader().setAppId(context.getAppId());

            //timestamp
            if (msg.getMessageHeader().getTimestamp() == null)
                msg.getMessageHeader().setTimestamp(currentDate);

            if (!MessageType.PubSubMessage.getType().equals(msg.getMessageHeader().getType())) {
                logger.error("[validateMessagesProperites] there is a message is not  `PubSubMessage`. ");
            }
        }
    }
}
