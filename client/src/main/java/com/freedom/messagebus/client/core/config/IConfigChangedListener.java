package com.freedom.messagebus.client.core.config;

import org.apache.zookeeper.Watcher;

/**
 * the interface of config changed listener
 */
public interface IConfigChangedListener {

    /**
     * after listened the change of the config from server will trigger this "event"
     *
     * @param path          the changed znode's name
     * @param newData       new data
     * @param ConfigManager the config manager
     */
    public void onChanged(String path,
                          byte[] newData,
                          Watcher.Event.EventType eventType,
                          ConfigManager ConfigManager);

}
