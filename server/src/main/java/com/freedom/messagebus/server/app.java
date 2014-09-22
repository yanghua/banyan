package com.freedom.messagebus.server;

import com.freedom.messagebus.server.bootstrap.RabbitmqInitializer;
import com.freedom.messagebus.server.bootstrap.ZookeeperInitializer;
import com.freedom.messagebus.server.daemon.ServiceLoader;

public class app {

    public static void main(String[] args) {
        //invoke bootstrap service
        RabbitmqInitializer rabbitmqInitializer = RabbitmqInitializer.getInstance();
        rabbitmqInitializer.launch();

        ZookeeperInitializer zookeeperInitializer = ZookeeperInitializer.getInstance();
        zookeeperInitializer.launch();


        //load and start daemon service
        ServiceLoader serviceLoader = ServiceLoader.getInstance();
        serviceLoader.launch();


    }

}
