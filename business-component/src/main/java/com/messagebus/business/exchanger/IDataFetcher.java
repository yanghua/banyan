package com.messagebus.business.exchanger;

import com.messagebus.interactor.pubsub.IDataConverter;


public interface IDataFetcher {

    public byte[] fetchData(IDataConverter converter);

}
