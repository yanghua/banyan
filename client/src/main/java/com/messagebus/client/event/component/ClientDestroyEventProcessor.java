package com.messagebus.client.event.component;

import com.google.common.eventbus.Subscribe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 6/25/15.
 */
public class ClientDestroyEventProcessor {

    private static final Log logger = LogFactory.getLog(ClientDestroyEventProcessor.class);

    @Subscribe
    public void onClientDestroy(ClientDestroyEvent event) {
        logger.debug("event : onClientDestroyEvent");
    }

}
