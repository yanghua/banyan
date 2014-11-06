package com.freedom.messagebus.interactor.zookeeper;

import com.freedom.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

    public LongLiveZookeeper(String host, int port) {
        if (longLiveZookeeper == null) {
            synchronized (LongLiveZookeeper.class) {
                if (longLiveZookeeper == null) {

                    latch = new CountDownLatch(1);

                    this.host = host;
                    this.port = port;
                    this.init();

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
    }

    private void init() {
        try {
            zooKeeper = new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
        } catch (IOException e) {
            throw new RuntimeException("[createZKClient] occurs a IOException : " + e.getMessage());
        }
    }

    public void close() {
        if (zooKeeper != null) {
            synchronized (LongLiveZookeeper.class) {
                if (zooKeeper != null) {
                    try {
                        zooKeeper.close();
                        zooKeeper = null;
                        longLiveZookeeper = null;
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
            logger.debug("paths :" + paths);
            logger.debug("zooKeeper : " + zooKeeper);
            for (String path : paths) {
                zooKeeper.exists(path, watcher);
            }
        } catch (KeeperException e) {
            logger.error("[KeeperException] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[InterruptedException] occurs a InterruptedException : " + e.getMessage());
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[watchPaths]");
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
                        this.listener.onChanged(path, data, new ZKEventType(watchedEvent.getType()));
                        break;

                }
            } catch (KeeperException | InterruptedException e) {
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

    public void setConfig(@NotNull String path, @NotNull byte[] newData, boolean ifNotThenCreate) {
        try {
            logger.info("[setConfig] path is : " + path);
            Stat stat = this.zooKeeper.exists(path, false);
            if (stat == null) {
                if (ifNotThenCreate)
                    this.zooKeeper.create(path, newData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                else
                    throw new IllegalStateException(" the path : " + path + "is not exists!");
            } else {
                int version = stat.getVersion();
                this.zooKeeper.setData(path, newData, version);
            }
        } catch (KeeperException e) {
            logger.error("[setConfig] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[setConfig] occurs a InterruptedException : " + e.getMessage());
        }
    }

    public void createNode(@NotNull String path) throws Exception {
        Stat stat = this.zooKeeper.exists(path, false);
        if (stat == null)
            this.zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

}
