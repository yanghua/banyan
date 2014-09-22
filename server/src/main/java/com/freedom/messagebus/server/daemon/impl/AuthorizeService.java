package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;

@DaemonService(value = "authorizeService",policy = RunPolicy.ONCE)
public class AuthorizeService implements Runnable,IService {

    @Override
    public void run() {
        while(true) {

        }
    }
}
