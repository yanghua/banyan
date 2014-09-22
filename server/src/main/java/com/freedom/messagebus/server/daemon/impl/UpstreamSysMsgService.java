package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;

@DaemonService(value = "upstreamSysMsgService", policy = RunPolicy.ONCE)
public class UpstreamSysMsgService implements IService,Runnable {

    @Override
    public void run() {

    }

}
