package com.freedom.messagebus.client.core.authorize;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.HttpHelper;
import com.freedom.messagebus.common.IMessageReceiveListener;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.messageBody.AuthrespMessageBody;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Authorizer {

    private static Log  logger = LogFactory.getLog(Authorizer.class);
    private static Gson gson   = new Gson();

    private static Authorizer instance;

    private Authorizer() {
    }

    public static Authorizer getInstance() {
        if (instance == null) {
            synchronized (Authorizer.class) {
                if (instance == null) {
                    instance = new Authorizer();
                }
            }
        }

        return instance;
    }

    /**
     * blocked request
     * @param authMsg
     * @return
     */
    @NotNull
    public Message syncRequestAuthorize(@NotNull Message authMsg,
                                        @NotNull String urlStr) {
        //TODO: uncompleted
        String responseStr = HttpHelper.syncHTTPGet(null, null);

        if (responseStr.isEmpty()) {
//            throw new IllegalFormatException("the authorized response is empty");
        }

        AuthrespMessageBody responseBody = gson.fromJson(responseStr, AuthrespMessageBody.class);
        Message authRespMsg = MessageFactory.createMessage(MessageType.AuthrespMessage);
        authRespMsg.setMessageBody(responseBody);

        return authRespMsg;
    }
}
