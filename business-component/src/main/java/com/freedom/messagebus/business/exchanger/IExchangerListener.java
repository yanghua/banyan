package com.freedom.messagebus.business.exchanger;

public interface IExchangerListener {

    public void onZKPathChanged(String path, Object obj);

}
