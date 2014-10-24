package com.freedom.messagebus.interactor.zookeeper;

/**
 * the interface of config changed listener
 */
public interface IConfigChangedListener {

    /**
     * after listened the change of the config from server will trigger this "event"
     *
     * @param path    the changed znode's name
     * @param newData new data
     */
    public void onChanged(String path,
                          byte[] newData,
                          ZKEventType eventType);

}
