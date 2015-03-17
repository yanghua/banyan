package com.messagebus.server.daemon.impl;

import com.messagebus.server.daemon.DaemonService;
import com.messagebus.server.daemon.RunPolicy;

import java.util.Map;

@DaemonService(value = "systemMonitorService", policy = RunPolicy.CYCLE_SCHEDULED)
public class SystemMonitorService extends AbstractService {

    public SystemMonitorService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {

    }
}
