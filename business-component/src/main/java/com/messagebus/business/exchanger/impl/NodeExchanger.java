package com.messagebus.business.exchanger.impl;

import com.messagebus.business.exchanger.Exchanger;
import com.messagebus.business.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSuber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Exchanger(table = "NODE", path = Constants.PUBSUB_ROUTER_CHANNEL)
public class NodeExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(NodeExchanger.class);

    public NodeExchanger(IPubSuber pubsuber, String channel) {
        super(pubsuber, channel);
    }

    @Override
    public void upload(byte[] obj) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Object download(byte[] originalData) {
        return this.dataConverter.deSerializeArray(originalData, Node[].class);
    }

    @Override
    public Object download() {
        byte[] originalData = this.pubsuber.get(channel);
        return this.dataConverter.deSerializeArray(originalData, Node[].class);
    }
}
