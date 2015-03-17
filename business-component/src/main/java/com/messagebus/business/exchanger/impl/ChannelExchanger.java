package com.messagebus.business.exchanger.impl;

import com.messagebus.business.exchanger.Exchanger;
import com.messagebus.business.model.Channel;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSuber;

/**
 * Created by yanghua on 3/17/15.
 */
@Exchanger(table = "CHANNEL", path = Constants.PUBSUB_CHANNEL_CHANNEL)
public class ChannelExchanger extends AbstractExchanger {

    public ChannelExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload(byte[] originalData) {
        throw new UnsupportedOperationException("unsupport");
    }

    @Override
    public Object download() {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, Channel[].class);
    }

    @Override
    public Object download(byte[] originalData) {
        return this.dataConverter.deSerializeArray(originalData, Channel[].class);
    }
}
