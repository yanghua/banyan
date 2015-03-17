package com.messagebus.business.exchanger.impl;

import com.messagebus.business.exchanger.Exchanger;
import com.messagebus.business.model.Sink;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 3/16/15.
 */
@Exchanger(table = "SINK", path = Constants.PUBSUB_SINK_CHANNEL)
public class SinkExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(SinkExchanger.class);

    public SinkExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload(byte[] originalData) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Object download() {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, Sink[].class);
    }

    @Override
    public Object download(byte[] originalData) {
        return this.dataConverter.deSerializeArray(originalData, Sink[].class);
    }
}
