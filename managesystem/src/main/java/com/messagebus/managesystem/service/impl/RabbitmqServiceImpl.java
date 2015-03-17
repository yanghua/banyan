package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.core.ConfigManager;
import com.messagebus.managesystem.service.IRabbitmqService;
import com.messagebus.common.HttpHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("rabbitmqService")
public class RabbitmqServiceImpl implements IRabbitmqService {


    //region http service

    @Override
    public String overview() {
        Map<String, Object> requestParamDic = new HashMap<>(3);
        requestParamDic.put("host", ConfigManager.HOST);
        requestParamDic.put("port", ConfigManager.PORT);
        requestParamDic.put("path", ConfigManager.HTTP_API_OVERVIEW);
        return HttpHelper.syncHTTPGet(requestParamDic, ConfigManager.DEFAULT_AUTH_INFO);
    }


    @Override
    public String nodelistOfcluster() {
        Map<String, Object> requestParamDic = new HashMap<>(3);
        requestParamDic.put("host", ConfigManager.HOST);
        requestParamDic.put("port", ConfigManager.PORT);
        requestParamDic.put("path", ConfigManager.HTTP_API_NODES);
        return HttpHelper.syncHTTPGet(requestParamDic, ConfigManager.DEFAULT_AUTH_INFO);
    }

    //endregion
}
