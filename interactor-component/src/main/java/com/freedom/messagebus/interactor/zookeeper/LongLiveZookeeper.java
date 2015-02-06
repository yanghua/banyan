package com.freedom.messagebus.interactor.zookeeper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * warped the zookeeper and make it has long-life cycle
 * used session timeout-check and reconnect
 */
public class LongLiveZookeeper {

    private static final Log logger = LogFactory.getLog(LongLiveZookeeper.class);

    private static CountDownLatch latch = new CountDownLatch(1);

    private ZooKeeper zooKeeper;
    private String    host;
    private int       port;

    private List<String> watchedPaths;

    public LongLiveZookeeper(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void init() {
        try {
            zooKeeper = new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
            watchedPaths = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("[createZKClient] occurs a IOException : " + e.getMessage());
        }
    }

    public synchronized void open() {
        latch = new CountDownLatch(1);

        this.init();

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("[getZKInstance] occurs a InterruptedException : " + e.getMessage());
        } finally {
            latch = null;
        }
    }

    public synchronized void close() {
        try {
            if (this.zooKeeper != null) {
                zooKeeper.close();
                zooKeeper = null;
            }
        } catch (InterruptedException e) {
            logger.error("[close] occurs a InterruptedException : " + e.getMessage());
        }
    }

    public void watchPaths(String[] paths, IConfigChangedListener listener) {
        try {
            PathWatcher watcher = new PathWatcher(zooKeeper, listener);
            logger.debug("paths :" + paths);
            logger.debug("zooKeeper : " + zooKeeper);

            for (String path : paths) {
                if (!watchedPaths.contains(path)) {
                    zooKeeper.exists(path, watcher);
                    watchedPaths.add(path);
                }
            }
        } catch (KeeperException e) {
            logger.error("[KeeperException] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[InterruptedException] occurs a InterruptedException : " + e.getMessage());
        } catch (Exception e) {
            logger.error("[watchPaths] occurs a Exception : " + e.getMessage());
        }
    }

    public boolean isAlive() {
        return this.zooKeeper != null
            && this.zooKeeper.getState().equals(ZooKeeper.States.CONNECTED);
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
    private static class PathWatcher implements Watcher {

        private ZooKeeper              zooKeeper;
        private IConfigChangedListener listener;

        public PathWatcher(ZooKeeper zooKeeper, IConfigChangedListener listener) {
            this.zooKeeper = zooKeeper;
            this.listener = listener;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            String path = watchedEvent.getPath();
            if (path == null)
                return;

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


    public byte[] getConfig(String path) {
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

    public void setConfig(String path, byte[] newData, boolean ifNotThenCreate) {
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

    public void createNode(String path) throws Exception {
        Stat stat = this.zooKeeper.exists(path, false);
        if (stat == null)
            this.zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

}
