package com.messagebus.server.daemon.impl;

import com.messagebus.server.daemon.DaemonService;
import com.messagebus.server.daemon.RunPolicy;

import java.util.Map;

@DaemonService(value = "upstreamSysMsgService", policy = RunPolicy.ONCE)
public class UpstreamSysMsgService extends AbstractService {

    public UpstreamSysMsgService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {

    }

}
