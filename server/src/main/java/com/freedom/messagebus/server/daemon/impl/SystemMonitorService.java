package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;

@DaemonService(value = "systemMonitorService", policy = RunPolicy.CYCLE_SCHEDULED)
public class SystemMonitorService implements Runnable, IService {

    @Override
    public void run() {

    }
}
