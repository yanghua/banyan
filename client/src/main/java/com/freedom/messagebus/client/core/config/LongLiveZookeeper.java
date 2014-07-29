package com.freedom.messagebus.client.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * warped the zookeeper and make it has long-life cycle
 * used session timeout-check and reconnect
 */
public class LongLiveZookeeper {

    private static final Log logger = LogFactory.getLog(LongLiveZookeeper.class);

    private static volatile ZooKeeper      zooKeeper = null;
    private static          CountDownLatch latch     = new CountDownLatch(1);
    private static String host;
    private static int    port;

    private static ZooKeeper createZKClient() {
        try {
            return new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
        } catch (IOException e) {
            throw new RuntimeException("[createZKClient] occurs a IOException : " + e.getMessage());
        }
    }

    public static ZooKeeper getZKInstance(String h, int p) {
        if (zooKeeper == null) {
            synchronized (LongLiveZookeeper.class) {
                if (zooKeeper == null) {
                    host = h;
                    port = p;

                    latch = new CountDownLatch(1);
                    zooKeeper = createZKClient();

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

        return zooKeeper;
    }

    public static void close() {
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

    public static void watchPaths(String[] paths, IConfigChangedListener listener) {
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

    /**
     * session watcher for watching zookeeper's session timeout
     */
    private static class SessionWatcher implements Watcher {

        @Override
        public void process(WatchedEvent watchedEvent) {
            logger.debug("loop");
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                if (latch != null) {
                    latch.countDown();
                }
            } else if (watchedEvent.getState() == Event.KeeperState.Expired) {
                close();
                getZKInstance(host, port);

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
            logger.debug("------->watching");

            String path = watchedEvent.getPath();

            try {
                switch (watchedEvent.getType()) {

                    case NodeDataChanged:
                        logger.debug("NodeDataChanged : " + path);
                        byte[] data = this.zooKeeper.getData(path, false, null);
                        logger.info("data is : " + new String(data));
                        this.listener.onChanged(path, data, watchedEvent.getType());
                        break;

                    case NodeCreated:
                        //TODO:
                        break;

                    case NodeDeleted:
                        //TODO:
                        break;

                }
            } catch (KeeperException e) {
                logger.error("[process] occurs a KeeperException : " + e.getMessage());
            } catch (InterruptedException e) {
                logger.error("[process] occurs a InterruptedException : " + e.getMessage());
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

}
