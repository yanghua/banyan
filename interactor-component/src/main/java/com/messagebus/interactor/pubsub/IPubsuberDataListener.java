package com.messagebus.interactor.pubsub;

public interface IPubsuberDataListener {

    public void onChannelDataChanged(String path, Object obj);

}
