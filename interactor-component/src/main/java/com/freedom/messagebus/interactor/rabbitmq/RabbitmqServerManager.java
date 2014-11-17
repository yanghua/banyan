package com.freedom.messagebus.interactor.rabbitmq;

import com.freedom.messagebus.interactor.util.ShellHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

public class RabbitmqServerManager {

    private static final    Log                   logger   = LogFactory.getLog(RabbitmqServerManager.class);
    private static volatile RabbitmqServerManager instance = null;

    private Properties properties;

    private RabbitmqServerManager(Properties config) {
        this.properties = config;
    }

    public static RabbitmqServerManager defaultManager(Properties config) {
        if (instance == null) {
            synchronized (RabbitmqServerManager.class) {
                if (instance == null) {
                    instance = new RabbitmqServerManager(config);
                }
            }
        }

        return instance;
    }

    public void start() {
        logger.info("starting mq server...");
        String mqServerStartupCmdStr = "rabbitmq-server start";
        try {
            ShellHelper.exec("/usr/sbin/service " + mqServerStartupCmdStr);
        } catch (IOException | InterruptedException e) {
            logger.error("[start] occurs an IOException : " + e.getMessage());
        }
    }

    public void stop() {
        logger.info("stopping mq server...");
        String mqServerStopCmdStr = "rabbitmq-server stop";
        try {
            ShellHelper.exec("/usr/sbin/service " + mqServerStopCmdStr);
        } catch (IOException | InterruptedException e) {
            logger.error("[start] occurs an IOException : " + e.getMessage());
        }
    }

    public void reset() {
        String mqServerResetCmdStr = "rabbitmqctl reset";
        String mqAppStartCmdStr = "rabbitmqctl start_app";
        String mqAppStopCmdStr = "rabbitmqctl stop_app";
        String defaultCmdInvoker = "/bin/sh ";

        try {
            if (!this.isAlive()) {
                this.start();
                ShellHelper.exec(defaultCmdInvoker + mqAppStopCmdStr);
                ShellHelper.exec(defaultCmdInvoker + mqServerResetCmdStr);
                ShellHelper.exec(defaultCmdInvoker + mqAppStartCmdStr);
            } else {
                ShellHelper.exec(defaultCmdInvoker + mqAppStopCmdStr);
                ShellHelper.exec(defaultCmdInvoker + mqServerResetCmdStr);
                ShellHelper.exec(defaultCmdInvoker + mqAppStartCmdStr);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("[start] occurs an IOException : " + e.getMessage());
        }
    }

    public synchronized boolean isAlive() {
        try {
            ShellHelper.ExecResult result = ShellHelper.exec("/bin/sh rabbitmqctl status | grep 'pid'");
            return result.getInfo().contains("pid");
        } catch (IOException e) {
            logger.error("[isAlive] occurs IOException : " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

}
