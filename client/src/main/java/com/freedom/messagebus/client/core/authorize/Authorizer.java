package com.freedom.messagebus.client.core.authorize;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.IMessageReceiveListener;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.messageBody.AuthrespMessageBody;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;

public class Authorizer {

    private static Log  logger = LogFactory.getLog(Authorizer.class);
    private static Gson gson   = new Gson();

    private static volatile Authorizer instance;

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
     *
     * @param authMsg
     * @return
     */
    @NotNull
    public Message syncRequestAuthorize(@NotNull final Message authMsg,
                                        @NotNull Channel channel)
        throws IOException, AuthRequestTimeoutException {
        //check /var/message bus dir is exists
        if (!checkConfigDir()) {

        }

        final Message authRespMsg = MessageFactory.createMessage(MessageType.AuthrespMessage);

        //consume proxy/message/sys/auth queue
        ProxyConsumer proxyConsumer = new ProxyConsumer();
        proxyConsumer.consume(channel, CONSTS.DEFAULT_AUTH_QUEUE_NAME, new IMessageReceiveListener() {
            @Override
            public void onMessage(final Message message) {
                instance.boxAuthrespMsg(authRespMsg, message);
                instance.notify();
            }
        });

        //send auth request message
        ProxyProducer.produce(CONSTS.PROXY_EXCHANGE_NAME,
                              channel,
                              CONSTS.DEFAULT_AUTH_ROUTING_KEY_NAME,
                              "".getBytes(),
                              this.initMsgProperites());

        try {
            //block until auth response and get config info
            instance.wait(CONSTS.DEFAULT_AUTH_REQUEST_TIMEOUT_SECONDS);
        } catch (InterruptedException e) {
            throw new AuthRequestTimeoutException(e);
        }

        return authRespMsg;
    }

    private boolean checkConfigDir() throws IOException {
        Path path = Paths.get(CONSTS.CONFIG_BASE_DIR);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            return true;

        //dir
        Path dirPath = FileSystems.getDefault().getPath(CONSTS.CONFIG_BASE_DIR);
        Files.createDirectories(dirPath);

        Path topologyFilePath = FileSystems.getDefault().getPath(
            CONSTS.CONFIG_BASE_DIR + CONSTS.TOPOLOGY_FILE_NAME);
        Path pathFilePath = FileSystems.getDefault().getPath(
            CONSTS.CONFIG_BASE_DIR + CONSTS.PATH_FILE_NAME);

        Files.createFile(topologyFilePath);
        Files.createFile(pathFilePath);

        return false;
    }

    private void boxAuthrespMsg(final Message authrespMsg, Message response) {
        return;
    }

    private AMQP.BasicProperties initMsgProperites() {
        //TODO
        return null;
    }
}
