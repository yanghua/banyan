package com.messagebus.business.exchanger.impl;

import com.messagebus.business.exchanger.Exchanger;
import com.messagebus.business.model.Config;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Exchanger(table = "CONFIG", path = Constants.PUBSUB_CONFIG_CHANNEL)
public class ConfigExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(ConfigExchanger.class);

    public ConfigExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload(byte[] originalData) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Object download(byte[] originalData) {
        return this.dataConverter.deSerializeArray(originalData, Config[].class);
    }

    @Override
    public Object download() {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, Config[].class);
    }
}
