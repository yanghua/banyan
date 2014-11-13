package com.freedom.messagebus.server.bootstrap;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.dataaccess.DBAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ZookeeperInitializer {

    private static          Log                  logger   = LogFactory.getLog(ZookeeperInitializer.class);
    private static volatile ZookeeperInitializer instance = null;

    private LongLiveZookeeper zookeeper;
    private boolean isInited = false;
    private Properties          config;
    private Map<String, Object> context;

    public static ZookeeperInitializer getInstance(Map<String, Object> context) {
        if (instance == null) {
            synchronized (ZookeeperInitializer.class) {
                if (instance == null) {
                    instance = new ZookeeperInitializer(context);
                }
            }
        }

        return instance;
    }

    private ZookeeperInitializer(Map<String, Object> context) {
        this.context = context;
        this.config = (Properties) this.context.get(Constants.KEY_SERVER_CONFIG);
        this.zookeeper = (LongLiveZookeeper) this.context.get(Constants.GLOBAL_ZOOKEEPER_OBJECT);
        this.init();
    }

    public void launch() throws IOException, InterruptedException {
        if (!this.isInited)
            throw new RuntimeException("the inner component initialize failed");

        this.dumpDbForZookeeper();
        this.loadSettingToZookeeper();
    }

    private void init() {
        try {
            this.initNodes();
            this.isInited = true;
        } catch (Exception e) {
            logger.error("[init] initialized failed : " + e.getMessage());
            this.isInited = false;
        }
    }

    private void initNodes() throws Exception {
        //1th
        this.zookeeper.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        this.zookeeper.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
        this.zookeeper.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT);
        this.zookeeper.createNode(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_AUTH);

        //2th
        this.zookeeper.createNode(CONSTS.ZOOKEEPER_PATH_FOR_AUTH_SEND_PERMISSION);
        this.zookeeper.createNode(CONSTS.ZOOKEEPER_PATH_FOR_AUTH_RECEIVE_PERMISSION);

    }

    private void dumpDbForZookeeper() throws IOException, InterruptedException {
        DBAccessor dbAccessor = DBAccessor.defaultAccessor(this.config);
        dbAccessor.dumpDbInfo(CONSTS.EXPORTED_TABLE_CMD_FORMAT, CONSTS.EXPORTED_NODE_FILE_PATH, CONSTS.DB_TABLE_OF_NODE);
        dbAccessor.dumpDbInfo(CONSTS.EXPORTED_TABLE_CMD_FORMAT, CONSTS.EXPORTED_CONFIG_FILE_PATH, CONSTS.DB_TABLE_OF_CONFIG);
        dbAccessor.dumpDbInfo(CONSTS.EXPORTED_TABLE_CMD_FORMAT, CONSTS.EXPORTED_SEND_PERMISSION_FILE_PATH,
                              CONSTS.DB_TABLE_OF_SEND_PERMISSION);
        dbAccessor.dumpDbInfo(CONSTS.EXPORTED_TABLE_CMD_FORMAT, CONSTS.EXPORTED_RECEIVE_PERMISSION_FILE_PATH,
                              CONSTS.DB_TABLE_OF_RECEIVE_PERMISSION);
    }

    private void loadSettingToZookeeper() throws IOException {
        setDbInfoToZK(CONSTS.EXPORTED_NODE_FILE_PATH, CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        setDbInfoToZK(CONSTS.EXPORTED_CONFIG_FILE_PATH, CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
        setDbInfoToZK(CONSTS.EXPORTED_SEND_PERMISSION_FILE_PATH, CONSTS.ZOOKEEPER_PATH_FOR_AUTH_SEND_PERMISSION);
        setDbInfoToZK(CONSTS.EXPORTED_RECEIVE_PERMISSION_FILE_PATH, CONSTS.ZOOKEEPER_PATH_FOR_AUTH_RECEIVE_PERMISSION);
    }


    private void setDbInfoToZK(String filePath, String zkNode) throws IOException {
        FileReader reader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();

        String tmp = null;
        while ((tmp = bufferedReader.readLine()) != null) {
            sb.append(tmp);
        }

        String totalStr = sb.toString();
        this.zookeeper.setConfig(zkNode,
                                 totalStr.getBytes(),
                                 true);
    }

}
