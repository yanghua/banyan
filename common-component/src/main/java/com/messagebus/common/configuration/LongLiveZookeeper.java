package com.messagebus.common.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * warped the zookeeper and make it has long-life cycle
 * used session timeout-check and reconnect
 */
public class LongLiveZookeeper {

    private static final Log            logger        = LogFactory.getLog(LongLiveZookeeper.class);
    private static final DataSerializer dataConverter = new DataSerializer();

    private CountDownLatch latch = new CountDownLatch(1);

    private ZooKeeper zooKeeper;
    private String    host;
    private int       port;

    public LongLiveZookeeper(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void init() {
        try {
            zooKeeper = new ZooKeeper(host + ":" + port, 30000, new SessionWatcher());
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public synchronized void open() {
        latch = new CountDownLatch(1);

        this.init();

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info(e);
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

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public void watch(String path, IPubSubListener listener) {
        try {
            PathWatcher watcher = new PathWatcher(zooKeeper, listener);

            zooKeeper.exists(path, watcher);
            zooKeeper.getChildren(path, watcher);
            zooKeeper.getData(path, watcher, null);
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public void watchMulti(String[] paths, IPubSubListener listener) {
        for (String path : paths) {
            this.watch(path, listener);
        }
    }

    public boolean isAlive() {
        return this.zooKeeper != null
            && this.zooKeeper.getState().equals(ZooKeeper.States.CONNECTED);
    }

    public byte[] get(String path) {
        path = preProcess(path);
        try {
            Stat stat = this.zooKeeper.exists(path, true);
            if (stat == null)
                throw new IllegalStateException("the path : " + path + " is not exists!");

            return this.zooKeeper.getData(path, true, null);
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
        }

        return new byte[0];
    }

    public <T> T get(String key, Class<T> clazz) {
        byte[] originalData = this.get(key);
        return dataConverter.deSerializeObject(originalData, clazz);
    }

    public boolean exists(String key) {
        key = preProcess(key);
        try {
            return this.zooKeeper.exists(key, true) != null;
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void set(String key, Serializable data, Class<?> clazz) {
        byte[] serializedData = dataConverter.serialize(data, clazz);
        this.set(key, serializedData);
    }

    public void set(String key, Serializable data) {
        byte[] serializedData = dataConverter.serialize(data);
        this.set(key, serializedData);
    }

    public void set(String path, byte[] data) {
        path = preProcess(path);
        try {
            logger.info("[setConfig] path is : " + path);
            Stat stat = this.zooKeeper.exists(path, true);
            if (stat == null) {

                //if path has multi level, first should be check parent path
                String[] pathSegments = path.substring(1).split("/");
                StringBuilder sb = new StringBuilder();
                for (String segment : pathSegments) {
                    sb.append("/");
                    sb.append(segment);
                    String aPath = sb.toString();

                    Stat innerStat = this.zooKeeper.exists(aPath, true);
                    if (innerStat == null) {
                        this.zooKeeper.create(aPath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                }

            } else {
                int version = stat.getVersion();
                this.zooKeeper.setData(path, data, version);
            }
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.info("occurs a InterruptedException ");
        }
    }

    public void delete(String path) {
        try {
            Stat stat = this.zooKeeper.exists(path, true);
            if (stat != null) {
                this.zooKeeper.delete(path, stat.getVersion());
            }
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {

        }
    }

    public void create(String path) {
        try {
            path = preProcess(path);
            Stat stat = this.zooKeeper.exists(path, true);
            if (stat == null)
                this.zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {

        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * just for test
     */
    @Deprecated
    public void clear(String path) {
        try {
            List<String> allPaths = this.zooKeeper.getChildren(path, false);
            for (String p : allPaths) {
                if (p.equals("zookeeper"))
                    continue;

                if (path.equals("/")) {
                    p = path + p;
                } else {
                    p = path + "/" + p;
                }

                if (this.zooKeeper.getChildren(p, false).size() > 0) {
                    clear(p);
                }
                Stat stat = this.zooKeeper.exists(p, true);
                zooKeeper.delete(p, stat.getVersion());
            }
        } catch (KeeperException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * session watcher for watching zookeeper's session timeout
     */
    private class SessionWatcher implements Watcher {

        @Override
        public synchronized void process(WatchedEvent watchedEvent) {
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

        private ZooKeeper       zooKeeper;
        private IPubSubListener listener;

        public PathWatcher(ZooKeeper zooKeeper, IPubSubListener listener) {
            this.zooKeeper = zooKeeper;
            this.listener = listener;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            String path = watchedEvent.getPath();
            if (path == null)
                return;

            logger.debug("[process] path : " + path + " changed");

            try {
                switch (watchedEvent.getType()) {
                    case NodeDataChanged:
                    case NodeChildrenChanged:
                    case NodeCreated:
                    case NodeDeleted:
                        ZKEventType eventType = new ZKEventType(watchedEvent.getType());
                        this.listener.onChange(path, eventType);
                        break;

                    default:
                        break;

                }
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            } finally {
                try {
                    this.zooKeeper.exists(path, this);
                    this.zooKeeper.getData(path, this, null);
                    this.zooKeeper.getChildren(path, this);
                } catch (KeeperException e) {
                    logger.error(e);
                } catch (InterruptedException e) {

                }
            }
        }

    }

    private String preProcess(String path) {
        if (!path.startsWith("/")) {
            return "/" + path;
        }

        return path;
    }

    /**
     * just for clear all path
     *
     * @param args
     */
    @Deprecated
    public static void main(String[] args) {
        LongLiveZookeeper zookeeper = new LongLiveZookeeper("127.0.0.1", 2181);
        zookeeper.open();
        zookeeper.clear("/");
        zookeeper.close();
    }

}
