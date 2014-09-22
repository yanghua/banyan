package com.freedom.messagebus.server.service.impl;

import com.freedom.messagebus.server.service.DaemonService;
import com.freedom.messagebus.server.service.IService;

@DaemonService("authorizeService")
public class AuthorizeService implements Runnable,IService {

    @Override
    public void run() {
        //todo
    }
}
