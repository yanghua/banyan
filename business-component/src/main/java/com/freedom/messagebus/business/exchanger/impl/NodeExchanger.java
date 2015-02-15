package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.Exchanger;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

@Exchanger(table = "NODE", path = Constants.PUBSUB_ROUTER_CHANNEL)
public class NodeExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(NodeExchanger.class);

    public NodeExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload(byte[] obj) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Object download(byte[] originalData) throws IOException {
        return this.dataConverter.deSerializeArray(originalData, Node[].class);
    }

    @Override
    public Object download() throws IOException {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, Node[].class);
    }
}
