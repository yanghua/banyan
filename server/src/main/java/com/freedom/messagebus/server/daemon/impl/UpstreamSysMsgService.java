package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;

import java.util.Map;

@DaemonService(value = "upstreamSysMsgService", policy = RunPolicy.ONCE)
public class UpstreamSysMsgService  extends AbstractService {

    public UpstreamSysMsgService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {

    }

}
