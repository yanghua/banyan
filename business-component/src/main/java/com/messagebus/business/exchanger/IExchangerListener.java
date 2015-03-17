package com.messagebus.business.exchanger;

public interface IExchangerListener {

    public void onChannelDataChanged(String path, Object obj);

}
