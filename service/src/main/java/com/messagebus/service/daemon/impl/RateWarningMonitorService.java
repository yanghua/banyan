package com.messagebus.service.daemon.impl;

import com.google.common.base.Strings;
import com.messagebus.common.GsonUtil;
import com.messagebus.common.HttpHelper;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.IServiceCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RateWarningMonitorService extends AbstractService {

    private static final Log logger = LogFactory.getLog(RateWarningMonitorService.class);
    private IServiceCallback callback;

    public RateWarningMonitorService(Map<String, Object> context) {
        super(context);
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
        requestParamDic.put("host", context.get(Constants.MQ_HOST_KEY).toString());
        requestParamDic.put("port", Constants.PORT);
        requestParamDic.put("path", Constants.HTTP_API_QUEUES);
        return HttpHelper.syncHTTPGet(requestParamDic, Constants.DEFAULT_AUTH_INFO);
    }
}
