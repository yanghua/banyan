package com.messagebus.business.exchanger.impl;

import com.messagebus.business.exchanger.Exchanger;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSuber;

@Exchanger(table = "EVENT", path = Constants.PUBSUB_EVENT_CHANNEL)
public class EventExchanger extends AbstractExchanger {

    public EventExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload() {
        //it will be call when message bus initialization,
        //so here just init with "stoped"
        this.upload(Constants.MESSAGEBUS_SERVER_EVENT_STOPPED.getBytes(Constants.CHARSET_OF_UTF8));
    }

    @Override
    public void upload(byte[] originalData) {
        this.pubsuber.publish(this.channel, originalData);
    }

    @Override
    public Object download() {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeObject(originalData, String.class);
    }

    @Override
    public Object download(byte[] originalData) {
        return dataConverter.deSerializeObject(originalData, String.class);
    }


}
