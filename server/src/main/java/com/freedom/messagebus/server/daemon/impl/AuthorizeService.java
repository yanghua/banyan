package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.freedom.messagebus.common.IMessageReceiveListener;

@DaemonService(value = "authorizeService", policy = RunPolicy.ONCE)
public class AuthorizeService extends AbstractInitializer implements Runnable, IService {

    private static final Log logger = LogFactory.getLog(AuthorizeService.class);

    public AuthorizeService(String host) {
        super(host);
    }

    @Override
    public void run() {
        ProxyConsumer consumer = new ProxyConsumer();
//        try {
//            super.init();
//
//            consumer.consume(super.channel, CONSTS.DEFAULT_AUTH_QUEUE_NAME, new IMessageReceiveListener() {
//                @Override
//                public void onMessage(Message message) {
//                    //send http sync request or other method
//
//                    //send auth response
////                    ProxyProducer.produce(CONSTS.PROXY_EXCHANGE_NAME,
////                                          channel,
////                                          "");
//                }
//            });
//
//        } catch (IOException e) {
//            consumer.shutdown();
//        }
    }
}
