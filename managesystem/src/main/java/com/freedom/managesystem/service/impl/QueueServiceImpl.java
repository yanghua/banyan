package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.core.ConfigManager;
import com.freedom.managesystem.service.IQueueService;
import com.freedom.managesystem.util.HttpHelper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("queueService")
public class QueueServiceImpl implements IQueueService {

    //region integrated service
    @Override
    public void load(String appId, Map<String, Object> params) {

    }

    @Override
    public void unLoad(String appId, Map<String, Object> params) {

    }

    @Override
    public void enable(String appId, Map<String, Object> params) {

    }

    @Override
    public void disable(String appId, Map<String, Object> params) {

    }
    //endregion

    //region http service
    @NotNull
    public String listAll() {
        Map<String, Object> requestParamDic = new HashMap<>(3);
        requestParamDic.put("host", ConfigManager.HOST);
        requestParamDic.put("port", ConfigManager.PORT);
        requestParamDic.put("path", ConfigManager.HTTP_API_QUEUES);
        return HttpHelper.syncHTTPGet(requestParamDic, ConfigManager.DEFAULT_AUTH_INFO);
    }

    //endregion


}
