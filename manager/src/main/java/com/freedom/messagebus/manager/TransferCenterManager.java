package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.model.Router;
import com.freedom.messagebus.common.model.TransferCenter;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * User: yanghua
 * Date: 7/2/14
 * Time: 10:17 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public final class TransferCenterManager extends AbstractInitializer {

    private static final Log logger = LogFactory.getLog(TransferCenterManager.class);

    @NotNull
    public static TransferCenterManager defaultTransferCenterManager(@NotNull String host) {
        return new TransferCenterManager(host);
    }

    private TransferCenterManager(String host) {
        super(host);
    }

    public void createTransferCenter(@NotNull Router router, @NotNull TransferCenter tc)
        throws IOException {
        super.init();

        //both declare and binding
        this.channel.queueDeclare(tc.getTCN(), tc.isDurable(), false, false, null);
        this.channel.queueBind(tc.getTCN(), router.getRouterName(), tc.getRoutingKey());

        super.close();
    }

    public void declareTransferCenter(@NotNull TransferCenter transferCenter) throws IOException {
        super.init();
        this.channel.queueDeclare(transferCenter.getTCN(), transferCenter.isDurable(), false, false, null);
        super.close();
    }

    public void bindTransferCenter(@NotNull Router router, @NotNull TransferCenter tc) throws IOException {
        super.init();

        this.channel.queueBind(tc.getTCN(), router.getRouterName(), tc.getRoutingKey(), null);

        super.close();
    }

    public void deleteTransferCenter(@NotNull TransferCenter transferCenter) throws IOException {
        super.init();

        this.channel.queueDelete(transferCenter.getTCN());

        super.close();
    }

    public boolean isTransferCenterExists(@NotNull TransferCenter tc) throws IOException {
        super.init();
        boolean result = true;
        try {
            AMQP.Queue.DeclareOk declareOk = this.channel.queueDeclarePassive(tc.getTCN());
        } catch (IOException e) {
            result = false;
        }
        super.close();

        return result;
    }
}
