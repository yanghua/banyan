package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.Exchanger;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;

import java.io.IOException;
import java.io.Serializable;

@Exchanger(table = "EVENT", path = CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT)
public class EventZKExchanger extends AbstractExchanger {

    public EventZKExchanger(LongLiveZookeeper zookeeper, String zkPath) {
        super(zookeeper, zkPath);
    }

    @Override
    public void upload() throws IOException {
    }

    @Override
    public void upload(Serializable obj) throws IOException {
        super.upload(obj);
        byte[] data = super.serializedBytes;

        //write to zookeeper
        this.zookeeper.setConfig(this.zkPath, data, true);
    }

    @Override
    public Object download() throws IOException {
        byte[] originalData = zookeeper.getConfig(zkPath);
        return super.deSerialize(originalData);
    }

    @Override
    public Object download(byte[] originalData) throws IOException {
        return super.deSerialize(originalData);
    }


}
