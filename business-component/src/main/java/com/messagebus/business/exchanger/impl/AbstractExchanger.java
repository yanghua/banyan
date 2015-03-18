package com.messagebus.business.exchanger.impl;

import com.google.gson.Gson;
import com.messagebus.business.exchanger.IDataExchanger;
import com.messagebus.business.exchanger.IDataFetcher;
import com.messagebus.interactor.pubsub.IDataConverter;
import com.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractExchanger implements IDataExchanger {

    private static final Log logger = LogFactory.getLog(AbstractExchanger.class);

    public static final Gson gson = new Gson();

    protected IPubSuber pubsuber;
    protected String    channel;

    public IDataFetcher   dataFetcher;
    public IDataConverter dataConverter;

    public AbstractExchanger(IPubSuber pubsuber, String channel) {
        this.pubsuber = pubsuber;
        this.channel = channel;
    }

    @Override
    public void upload() {
        byte[] datas = this.dataFetcher.fetchData(dataConverter);
        this.pubsuber.publish(this.channel, datas);
    }


}
