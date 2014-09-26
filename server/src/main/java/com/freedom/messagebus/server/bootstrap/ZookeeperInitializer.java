package com.freedom.messagebus.server.bootstrap;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.ShellHelper;
import com.freedom.messagebus.interactor.zookeeper.ZookeeperManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZookeeperInitializer {

    private static          Log                  logger   = LogFactory.getLog(ZookeeperInitializer.class);
    private static volatile ZookeeperInitializer instance = null;

    private ZookeeperManager zookeeperManager;
    private boolean isInited = false;

    public static ZookeeperInitializer getInstance(String host, int port) {
        if (instance == null) {
            synchronized (ZookeeperInitializer.class) {
                if (instance == null) {
                    instance = new ZookeeperInitializer(host, port);
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

        this.dumpDbConfig();
        this.setRouterNode();
    }

    private void init(String host, int port) {
        try {
            ZooKeeper zookeeper = new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
            this.zookeeperManager = ZookeeperManager.getInstance(zookeeper);
            this.isInited = true;
        } catch (IOException e) {
            logger.error("[init] initialized zookeeper instance failed : " + e.getMessage());
            this.isInited = false;
        }
    }

    private void dumpDbConfig() throws IOException, InterruptedException {
        String cmdStr = CONSTS.EXPORTED_NODE_CMD_STR + CONSTS.EXPORTED_NODE_FILE_PATH;
        ShellHelper.exec(cmdStr);

        Path path = FileSystems.getDefault().getPath(CONSTS.EXPORTED_NODE_FILE_PATH);
        if (!Files.exists(path)) {
            logger.error("the file for initialize zookeeper node at path : " +
                             CONSTS.EXPORTED_NODE_FILE_PATH + " is not exists!");
            throw new RuntimeException("the file for initialize zookeeper node at path : " +
                                           CONSTS.EXPORTED_NODE_FILE_PATH + " is not exists!");
        }
    }

    private void setRouterNode() throws IOException {
        FileReader reader = new FileReader(CONSTS.EXPORTED_NODE_FILE_PATH);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();

        String tmp = null;
        while ((tmp = bufferedReader.readLine()) != null) {
            sb.append(tmp);
        }

        String totalStr = sb.toString();
        this.zookeeperManager.setConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER,
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
