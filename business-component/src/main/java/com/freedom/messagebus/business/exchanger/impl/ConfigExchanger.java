package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.Exchanger;
import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

@Exchanger(table = "CONFIG", path = CONSTS.PUBSUB_CONFIG_CHANNEL)
public class ConfigExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(ConfigExchanger.class);

    public ConfigExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload(byte[] originalData) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Object download(byte[] originalData) throws IOException {
        return this.dataConverter.deSerializeArray(originalData, Config[].class);
    }

    @Override
    public Object download() throws IOException {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, Config[].class);
    }
}
