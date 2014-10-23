package com.freedom.messagebus.client.handler.broadcast;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class BroadcastParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(BroadcastParamValidator.class);

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

            if (!MessageType.BroadcastMessage.getType().equals(msg.getMessageHeader().getType())) {
                logger.error("[validateMessagesProperites] there is a message is not  `BroadcastMessage`. ");
            }
        }
    }
}
