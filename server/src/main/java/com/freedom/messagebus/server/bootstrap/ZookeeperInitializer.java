package com.freedom.messagebus.server.bootstrap;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.ShellHelper;
import com.freedom.messagebus.interactor.zookeeper.ZookeeperManager;
import com.freedom.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ZookeeperInitializer {

    private static          Log                  logger   = LogFactory.getLog(ZookeeperInitializer.class);
    private static volatile ZookeeperInitializer instance = null;

    private ZookeeperManager zookeeperManager;
    private ZooKeeper        zookeeper;
    private boolean isInited = false;
    private static Properties config;

    public static ZookeeperInitializer getInstance(@NotNull Properties configProperties) {
        if (instance == null) {
            synchronized (ZookeeperInitializer.class) {
                if (instance == null) {
                    config = configProperties;
                    instance = new ZookeeperInitializer(
                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_HOST),
                        Integer.valueOf(config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_PORT)));
                }
            }
        }

        return instance;
    }

    private ZookeeperInitializer(String host, int port) {
        this.init(host, port);
    }

    public void launch() throws IOException, InterruptedException {
        if (!this.isInited)
            throw new RuntimeException("the inner component initialize failed");

        this.dumpDbForZookeeper();
        this.loadSettingToZookeeper();
        this.zookeeper.close();
    }

    private void init(String host, int port) {
        try {
            this.zookeeper = new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
            this.zookeeperManager = ZookeeperManager.getInstance(zookeeper);
            this.initNodes();
            this.isInited = true;
        } catch (Exception e) {
            logger.error("[init] initialized zookeeper instance failed : " + e.getMessage());
            this.isInited = false;
        }
    }

    private void initNodes() throws Exception {
        this.zookeeperManager.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        this.zookeeperManager.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
        this.zookeeperManager.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT);
    }

    private void dumpDbForZookeeper() throws IOException, InterruptedException {
        this.dumpDbInfo(CONSTS.EXPORTED_NODE_CMD_FORMAT, CONSTS.EXPORTED_NODE_FILE_PATH);
        this.dumpDbInfo(CONSTS.EXPORTED_CONFIG_CMD_FORMAT, CONSTS.EXPORTED_CONFIG_FILE_PATH);
    }

    private void loadSettingToZookeeper() throws IOException {
        this.setDbInfoToZK(CONSTS.EXPORTED_NODE_FILE_PATH, CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        this.setDbInfoToZK(CONSTS.EXPORTED_CONFIG_FILE_PATH, CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
    }

    private void dumpDbInfo(String cmdFormat, String filePath) throws IOException, InterruptedException {
        String partOfcmdStr = String.format(cmdFormat,
                                            config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_HOST),
                                            config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_USER),
                                            config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_PASSWORD));
        String cmdStr = partOfcmdStr + filePath;
        logger.debug("dump database info cmd : " + cmdStr);
        ShellHelper.exec(cmdStr);

        Path path = FileSystems.getDefault().getPath(filePath);
        if (!Files.exists(path)) {
            logger.error("the file for initialize zookeeper node at path : " +
                             filePath + " is not exists!");
            throw new RuntimeException("the file for initialize zookeeper node at path : " +
                                           filePath + " is not exists!");
        }
    }

    private void setDbInfoToZK(String filePath, String zkNode) throws IOException {
        FileReader reader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();

        String tmp = null;
        while ((tmp = bufferedReader.readLine()) != null) {
            sb.append(tmp);
        }

        String totalStr = sb.toString();
        this.zookeeperManager.setConfig(zkNode,
                                        totalStr.getBytes(),
                                        true);
    }

    private static class SessionWatcher implements Watcher {

        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                logger.debug("session connected");
            } else if (watchedEvent.getState() == Event.KeeperState.Expired) {
                logger.debug("session expired");
            }
        }
    }

}
