package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.Exchanger;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

@Exchanger(table = "RECEIVE_PERMISSION", path = CONSTS.ZOOKEEPER_PATH_FOR_AUTH_RECEIVE_PERMISSION)
public class ReceivePermissionZKExchanger extends AbstractExchanger {

    private static final Log logger = LogFactory.getLog(ReceivePermissionZKExchanger.class);

    public ReceivePermissionZKExchanger(LongLiveZookeeper zookeeper, String zkPath) {
        super(zookeeper, zkPath);
    }

    @Override
    public void upload() throws IOException {
        if (this.dataFetcher == null)
            throw new NullPointerException("the field : dataFetcher can not be null.");

        ArrayList datas = this.dataFetcher.fetchData();
        this.upload(datas);
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
