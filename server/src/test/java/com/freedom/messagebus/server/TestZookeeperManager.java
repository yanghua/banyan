//package com.freedom.messagebus.server;
//
//import com.freedom.messagebus.server.bootstrap.ZookeeperInitializer;
//import junit.framework.TestCase;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.io.IOException;
//
///**
// * Created by yanghua on 9/26/14.
// */
//public class TestZookeeperManager extends TestCase {
//
//    private static final Log logger = LogFactory.getLog(TestZookeeperManager.class);
//
//    public void test() throws Exception {
//        String host = "115.29.96.85";
//        int port = 2181;
//        ZookeeperInitializer zookeeperInitializer = ZookeeperInitializer.getInstance(host);
//        try {
//            zookeeperInitializer.launch();
//        } catch (IOException e) {
//            logger.error("[main] occurs a IOException : " + e.getMessage());
//        } catch (InterruptedException e) {
//            logger.error("[main] occurs a InterruptedException : " + e.getMessage());
//        }
//    }
//}
