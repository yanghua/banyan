package com.freedom.messagebus.server.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by yanghua on 9/19/14.
 */
public class ServiceLoader {

    private static final Log logger = LogFactory.getLog(ServiceLoader.class);
    private static volatile ServiceLoader instance;
    private Map<String, IService> longtimeliveServiceMap;

    private ServiceLoader() {
    }

    public static ServiceLoader getInstance() {
        if (instance == null) {
            synchronized (ServiceLoader.class) {
                if (instance == null) {
                    instance = new ServiceLoader();
                }
            }
        }

        return instance;
    }

    private void scan() {
        //scan annotation for load service
    }

    private void load() {
        if (longtimeliveServiceMap.size() != 0) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(longtimeliveServiceMap.size());

    }
}
