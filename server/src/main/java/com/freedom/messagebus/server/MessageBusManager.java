package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.CommonUtil;
import com.freedom.messagebus.common.RouterType;
import com.freedom.messagebus.common.model.TransferCenter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * User: yanghua
 * Date: 7/2/14
 * Time: 2:34 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public final class MessageBusManager extends AbstractInitializer {

    private static final Log logger = LogFactory.getLog(MessageBusManager.class);

    @NotNull
    public static MessageBusManager defaultMsgBusManager(@NotNull String host) {
        return new MessageBusManager(host);
    }

    private MessageBusManager(@NotNull String host) {
        super(host);
    }

    public void register(@NotNull String appKey, boolean durable,
                         @NotNull List<TransferCenter> transferCenters) throws IOException {
        super.init();
        //create 4 router for each router-type
        for (RouterType type : RouterType.values()) {
            String routerName = CommonUtil.getRouterName(appKey, type);
            this.channel.exchangeDeclare(routerName, type.toString(), durable);

            //create queue
            for (TransferCenter tc : transferCenters) {
                if (tc.getRouterType().equals(type)) {
                    this.channel.queueDeclare(tc.getTCN(), tc.isDurable(), false, false, null);

                    //bind router and queue
                    this.channel.queueBind(tc.getTCN(), routerName, tc.getRoutingKey());
                }
            }
        }

        super.close();
    }

    public void deRegister(@NotNull String appKey, @NotNull List<String> tcns) throws IOException {
        super.init();

        //delete queue
        for (String q : tcns) {
            //TODO: pass more params (safety delete)
            this.channel.queueDelete(q);
        }

        //delete router
        for (RouterType type : RouterType.values()) {
            String routerName = CommonUtil.getRouterName(appKey, type);
            //TODO: pass more params (safety delete)
            this.channel.exchangeDelete(routerName);
        }

        super.close();
    }

}
