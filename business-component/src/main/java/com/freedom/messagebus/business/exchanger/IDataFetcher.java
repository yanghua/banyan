package com.freedom.messagebus.business.exchanger;

import com.freedom.messagebus.interactor.pubsub.IDataConverter;


public interface IDataFetcher {

    public byte[] fetchData(IDataConverter converter);

}
