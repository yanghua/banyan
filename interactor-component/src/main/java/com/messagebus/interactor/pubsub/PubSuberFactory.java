package com.messagebus.interactor.pubsub;

import java.util.ServiceLoader;

/**
 * Created by yanghua on 2/6/15.
 */
public class PubSuberFactory {

    public static IPubSuber createPubSuber() {
        ServiceLoader<IPubSuber> pubSuberServiceLoader = ServiceLoader.load(IPubSuber.class);
        return pubSuberServiceLoader.iterator().next();
    }

    public static IDataConverter createConverter() {
        ServiceLoader<IDataConverter> converterServiceLoader = ServiceLoader.load(IDataConverter.class);
        return converterServiceLoader.iterator().next();
    }

}
