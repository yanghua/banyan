package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.Exchanger;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.pubsub.IPubSuber;

import java.io.IOException;

@Exchanger(table = "EVENT", path = CONSTS.PUBSUB_EVENT_CHANNEL)
public class EventExchanger extends AbstractExchanger {

    public EventExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload() throws IOException {
        //it will be call when message bus initialization,
        //so here just init with "stoped"
        this.upload(CONSTS.MESSAGEBUS_SERVER_EVENT_STOPPED.getBytes());
    }

    @Override
    public void upload(byte[] originalData) throws IOException {
        this.pubsuber.publish(this.channel, originalData);
    }

    @Override
    public Object download() throws IOException {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeObject(originalData, String.class);
    }

    @Override
    public Object download(byte[] originalData) throws IOException {
        return dataConverter.deSerializeObject(originalData, String.class);
    }


}
