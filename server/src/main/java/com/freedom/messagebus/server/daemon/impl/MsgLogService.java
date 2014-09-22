package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;

@DaemonService(value = "msgLogService", policy = RunPolicy.ONCE)
public class MsgLogService implements Runnable,IService {

    @Override
    public void run() {

    }
}
