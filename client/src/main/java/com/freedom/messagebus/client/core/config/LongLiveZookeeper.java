package com.freedom.messagebus.client.core.config;

import com.freedom.messagebus.common.CONSTS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * warped the zookeeper and make it has long-life cycle
 * used session timeout-check and reconnect
 */
public class LongLiveZookeeper {

    private static final Log logger = LogFactory.getLog(LongLiveZookeeper.class);

    private static volatile LongLiveZookeeper longLiveZookeeper = null;
    private static          CountDownLatch    latch             = new CountDownLatch(1);

    private ZooKeeper zooKeeper;
    private String    host;
    private int       port;

    private LongLiveZookeeper(String host, int port) {
        this.host = host;
        this.port = port;
        this.init();
    }

    private void init() {
        try {
            zooKeeper = new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
            this.fetchNewZookeeperData();
        } catch (IOException e) {
            throw new RuntimeException("[createZKClient] occurs a IOException : " + e.getMessage());
        }
    }

    public static LongLiveZookeeper getZKInstance(String h, int p) {
        if (longLiveZookeeper == null) {
            synchronized (LongLiveZookeeper.class) {
                if (longLiveZookeeper == null) {

                    latch = new CountDownLatch(1);
                    longLiveZookeeper = new LongLiveZookeeper(h, p);

                    try {
                        latch.await(30, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.error("[getZKInstance] occurs a InterruptedException : " + e.getMessage());
                    } finally {
                        latch = null;
                    }

                }
            }
        }

        return longLiveZookeeper;
    }

    public void close() {
        if (zooKeeper != null) {
            synchronized (LongLiveZookeeper.class) {
                if (zooKeeper != null) {
                    try {
                        zooKeeper.close();
                        zooKeeper = null;
                    } catch (InterruptedException e) {
                        logger.error("[close] occurs a InterruptedException : " + e.getMessage());
                    }
                }
            }
        }
    }

    public void watchPaths(String[] paths, IConfigChangedListener listener) {
        try {
            PathWatcher watcher = new PathWatcher(zooKeeper, listener);
            for (String path : paths) {
                zooKeeper.exists(path, watcher);
            }
        } catch (KeeperException e) {
            logger.error("[KeeperException] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[InterruptedException] occurs a InterruptedException : " + e.getMessage());
        }
    }

    public boolean isAlive() {
        return this.zooKeeper.getState().isAlive();
    }

    /**
     * session watcher for watching zookeeper's session timeout
     */
    private class SessionWatcher implements Watcher {

        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                if (latch != null) {
                    latch.countDown();
                }
            } else if (watchedEvent.getState() == Event.KeeperState.Expired) {
                close();
                init();
            }
        }
    }

    /**
     * path watcher for watching znode's change
     */
    private class PathWatcher implements Watcher {

        private ZooKeeper              zooKeeper;
        private IConfigChangedListener listener;

        public PathWatcher(ZooKeeper zooKeeper, IConfigChangedListener listener) {
            this.zooKeeper = zooKeeper;
            this.listener = listener;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            String path = watchedEvent.getPath();
            logger.debug("[process] path : " + path + "changed");

            try {
                switch (watchedEvent.getType()) {
                    case NodeDataChanged:
                    case NodeCreated:
                    case NodeDeleted:
                        byte[] data = this.zooKeeper.getData(path, false, null);
                        this.processPathChange(path, data);
                        this.listener.onChanged(path, data, watchedEvent.getType());
                        break;

                }
            } catch (KeeperException | IOException | InterruptedException e) {
                logger.error("[process] occurs a Exception : " + e.getMessage());
            } finally {
                try {
                    this.zooKeeper.exists(path, this);
                } catch (KeeperException e) {
                    logger.error("[process] finally occurs a KeeperException : " + e.getMessage());
                } catch (InterruptedException e) {
                    logger.error("[process] finally occurs a InterruptedException : " + e.getMessage());
                }
            }
        }

        private void processPathChange(String path, byte[] newData) throws IOException {
            refreshLocalCachedFile(path, newData);
        }

    }

    @NotNull
    public byte[] getConfig(@NotNull String path) {
        try {
            Stat stat = this.zooKeeper.exists(path, false);
            if (stat == null)
                throw new IllegalStateException("the path : " + path + " is not exists!");

            return this.zooKeeper.getData(path, null, null);
        } catch (KeeperException e) {
            logger.error("[getConfig] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[getConfig] occurs a InterruptedException : " + e.getMessage());
        }

        return new byte[0];
    }

    private void fetchNewZookeeperData() throws IOException {
        //get new config info
        byte[] routerData = this.getConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        byte[] configData = this.getConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
        //refresh local
        this.refreshLocalCachedFile(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER, routerData);
        this.refreshLocalCachedFile(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG, configData);
    }

    private void refreshLocalCachedFile(String path, byte[] newData) throws IOException {
        String filePath;
        if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER)) {
            filePath = CONSTS.EXPORTED_NODE_FILE_PATH;
        } else if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG)) {
            filePath = CONSTS.EXPORTED_CONFIG_FILE_PATH;
        } else {
            return;
        }

        Path routerFilePath = FileSystems.getDefault().getPath(filePath);
        FileOutputStream fos = null;
        try {
            if (!Files.exists(routerFilePath)) { //override
                Files.createFile(routerFilePath);
            }

            fos = new FileOutputStream(filePath);
            fos.write(newData);
        } catch (IOException e) {
            logger.error("[refreshLocalCachedRouterFile] occurs a IOException : " + e.getMessage());
            throw new IOException(e);
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                logger.error("[refreshLocalCachedRouterFile] finally block occurs a IOException : " + e.getMessage());
            }
        }
    }

}
