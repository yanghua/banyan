package com.freedom.messagebus.interactor.rabbitmq;

import com.freedom.messagebus.interactor.util.ShellHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class RabbitmqServerManager {

    private static final    Log                   logger   = LogFactory.getLog(RabbitmqServerManager.class);

    public static void start(String mqHost) {
        if (!comeFromSameHost(mqHost)) {
            throw new RuntimeException("the message bus server's host and the mq server's host" +
                                           " are not the same");
        }

        logger.info("starting mq server...");
        String mqServerStartupCmdStr = "rabbitmq-server start";
        try {
            ShellHelper.exec("/usr/sbin/service " + mqServerStartupCmdStr);
        } catch (IOException | InterruptedException e) {
            logger.error("[start] occurs an IOException : " + e.getMessage());
        }
    }

    public static void stop(String mqHost) {
        if (!comeFromSameHost(mqHost)) {
            throw new RuntimeException("the message bus server's host and the mq server's host" +
                                           " are not the same");
        }

        logger.info("stopping mq server...");
        String mqServerStopCmdStr = "rabbitmq-server stop";
        try {
            ShellHelper.exec("/usr/sbin/service " + mqServerStopCmdStr);
        } catch (IOException | InterruptedException e) {
            logger.error("occurs an IOException : " + e.getMessage());
            throw new RuntimeException("occurs an IOException : " + e.getMessage());
        }
    }

//    public void reset() {
//        String mqServerResetCmdStr = "rabbitmqctl reset";
//        String mqAppStartCmdStr = "rabbitmqctl start_app";
//        String mqAppStopCmdStr = "rabbitmqctl stop_app";
//        String defaultCmdInvoker = "/bin/sh ";
//
//        try {
//            if (!this.isAlive()) {
//                this.start();
//                ShellHelper.exec(defaultCmdInvoker + mqAppStopCmdStr);
//                ShellHelper.exec(defaultCmdInvoker + mqServerResetCmdStr);
//                ShellHelper.exec(defaultCmdInvoker + mqAppStartCmdStr);
//            } else {
//                ShellHelper.exec(defaultCmdInvoker + mqAppStopCmdStr);
//                ShellHelper.exec(defaultCmdInvoker + mqServerResetCmdStr);
//                ShellHelper.exec(defaultCmdInvoker + mqAppStartCmdStr);
//            }
//        } catch (IOException | InterruptedException e) {
//            logger.error("[start] occurs an IOException : " + e.getMessage());
//        }
//    }

    public static boolean isAlive(String mqHost) {
        if (!comeFromSameHost(mqHost)) {
            throw new RuntimeException("the message bus server's host and the mq server's host" +
                                           " are not the same");
        }

        try {
            ShellHelper.ExecResult result = ShellHelper.exec("rabbitmqctl status | grep 'pid'");
            return result.getInfo().contains("pid");
        } catch (IOException e) {
            logger.error("[isAlive] occurs IOException : " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private static boolean comeFromSameHost(String host) {
        try {
            String ipStr = Inet4Address.getLocalHost().getHostAddress();
            return ipStr.equals(host);
        } catch (UnknownHostException e) {
            logger.error("unknown host exception : " + e.toString());
            throw new RuntimeException("unknown host exception : " + e.toString());
        }
    }

}
