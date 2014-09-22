package com.freedom.messagebus.server.service.impl;

import com.freedom.messagebus.server.service.DaemonService;
import com.freedom.messagebus.server.service.IService;

import java.util.Map;

@DaemonService("systemMonitorService")
public class SystemMonitorService implements Runnable, IService {

    @Override
    public void run() {

    }
}
