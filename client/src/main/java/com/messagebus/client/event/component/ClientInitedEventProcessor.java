package com.messagebus.client.event.component;

import com.google.common.eventbus.Subscribe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 6/25/15.
 */
public class ClientInitedEventProcessor {

    private static final Log logger = LogFactory.getLog(ClientInitedEventProcessor.class);

    @Subscribe
    public void onClientInited(ClientInitedEvent event) {
        logger.debug("event : onClientInitedEvent");
    }

}
