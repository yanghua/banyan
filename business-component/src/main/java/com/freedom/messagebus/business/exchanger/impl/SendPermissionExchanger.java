package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.Exchanger;
import com.freedom.messagebus.business.model.SendPermission;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

@Exchanger(table = "SEND_PERMISSION", path = CONSTS.PUBSUB_AUTH_SEND_PERMISSION_CHANNEL)
public class SendPermissionExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(SendPermissionExchanger.class);

    public SendPermissionExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }


    @Override
    public void upload(byte[] obj) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }


    @Override
    public Object download(byte[] originalData) throws IOException {
        return this.dataConverter.deSerializeArray(originalData, SendPermission[].class);
    }

    @Override
    public Object download() throws IOException {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, SendPermission[].class);
    }
}
