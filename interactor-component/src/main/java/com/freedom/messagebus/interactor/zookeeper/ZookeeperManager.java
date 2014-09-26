package com.freedom.messagebus.interactor.zookeeper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.jetbrains.annotations.NotNull;

public class ZookeeperManager {

    private static final Log logger = LogFactory.getLog(ZookeeperManager.class);

    private static volatile ZookeeperManager instance;

    private ZooKeeper zooKeeper;

    private ZookeeperManager(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public static ZookeeperManager getInstance(ZooKeeper zooKeeper) {
        if (instance == null) {
            synchronized (ZookeeperManager.class) {
                if (instance == null) {
                    instance = new ZookeeperManager(zooKeeper);
                }
            }
        }

        return instance;
    }

    @NotNull
    public byte[] getConfig(@NotNull String path) {
        try {
            Stat stat = this.zooKeeper.exists(path, false);
            if (stat == null)
                throw new IllegalStateException("the path : " + path + " is not exists!");

            byte[] result = this.zooKeeper.getData(path, null, null);

            return result;
        } catch (KeeperException e) {
            logger.error("[getConfig] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[getConfig] occurs a InterruptedException : " + e.getMessage());
        } finally {
            try {
                this.zooKeeper.close();
            } catch (InterruptedException e) {

            }
        }

        return new byte[0];
    }

    public void setConfig(@NotNull String path, @NotNull byte[] newData) {
        try {
            Stat stat = this.zooKeeper.exists(path, false);
            if (stat == null)
                throw new IllegalStateException("the path : " + path + " is not exists!");

            int version = stat.getVersion();
            this.zooKeeper.setData(path, newData, version);
        } catch (KeeperException e) {
            logger.error("[setConfig] occurs a KeeperException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[setConfig] occurs a InterruptedException : " + e.getMessage());
        } finally {
            try {
                this.zooKeeper.close();
            } catch (InterruptedException e) {

            }
        }
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
        } finally {
            try {
                this.zooKeeper.close();
            } catch (InterruptedException e) {

            }
        }
    }

}
