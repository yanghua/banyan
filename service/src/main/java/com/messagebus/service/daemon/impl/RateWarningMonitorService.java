package com.messagebus.service.daemon.impl;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.messagebus.common.GsonUtil;
import com.messagebus.common.HttpHelper;
import com.messagebus.interactor.pubsub.LongLiveZookeeper;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.IServiceCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RateWarningMonitorService extends AbstractService {

    private static final     Log    logger                    = LogFactory.getLog(RateWarningMonitorService.class);
    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";
    private static final     Gson   GSON                      = new Gson();

    private IServiceCallback callback;
    private Map mbHostAndPortObj;

    public RateWarningMonitorService(Map<String, Object> context) {
        super(context);

        String zkHost = context.get(Constants.ZK_HOST_KEY).toString();
        int zkPort = Integer.parseInt(context.get(Constants.ZK_PORT_KEY).toString());

        LongLiveZookeeper zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        try {
            zookeeper.open();

            mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);
        } catch (JsonSyntaxException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            if (zookeeper.isAlive()) {
                zookeeper.close();
            }
        }
    }

    public IServiceCallback getCallback() {
        return callback;
    }

    public void setCallback(IServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        String jsonData = getQueuesInfo();
        if (Strings.isNullOrEmpty(jsonData)) {
            return;
        }

        List<Object> remoteObjs = GsonUtil.jsonStrToList(jsonData);
        Map<String, Object> ctx = new HashMap<String, Object>(1);
        ctx.put("queueInfoList", remoteObjs);

        if (this.callback != null) {
            this.callback.callback(ctx);
        }

    }

    private String getQueuesInfo() {
        Map<String, Object> requestParamDic = new HashMap<String, Object>(3);
        requestParamDic.put("host", mbHostAndPortObj.get("mqHost").toString());
        requestParamDic.put("port", new Float(mbHostAndPortObj.get("mqPort").toString()).intValue());
        requestParamDic.put("path", Constants.HTTP_API_QUEUES);
        return HttpHelper.syncHTTPGet(requestParamDic, Constants.DEFAULT_AUTH_INFO);
    }
}
