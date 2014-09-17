package com.freedom.messagebus.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.jetbrains.annotations.NotNull;

/**
 * User: yanghua
 * Date: 7/31/14
 * Time: 7:55 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class ConfigManager {

    private static final Log logger = LogFactory.getLog(ConfigManager.class);

    private static volatile ConfigManager instance;

    private ZooKeeper zooKeeper;

    private ConfigManager(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public static ConfigManager getInstance(ZooKeeper zooKeeper) {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager(zooKeeper);
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
        }
    }

    public void setConfig(@NotNull String path, @NotNull byte[] newData, boolean ifNotThenCreate) {
        try {
            Stat stat = this.zooKeeper.exists(path, false);
            if (stat == null) {
                if (ifNotThenCreate)
                    this.zooKeeper.create(path, newData, null, CreateMode.PERSISTENT);
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
}
