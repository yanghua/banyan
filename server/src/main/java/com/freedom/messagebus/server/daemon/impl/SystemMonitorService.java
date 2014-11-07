package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;

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
