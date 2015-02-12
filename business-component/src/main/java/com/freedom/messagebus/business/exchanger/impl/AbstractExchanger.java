package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.IDataExchanger;
import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
import com.freedom.messagebus.interactor.pubsub.IPubSuber;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

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
    public void upload() throws IOException {
        byte[] datas = this.dataFetcher.fetchData(dataConverter);
        this.pubsuber.publish(this.channel, datas);
    }


}
