package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.core.ConfigManager;
import com.messagebus.managesystem.pojo.rabbitHTTP.Queue;
import com.messagebus.managesystem.service.IQueueService;
import com.messagebus.common.HttpHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("queueService")
public class QueueServiceImpl implements IQueueService {

    //region http service

    public Queue[] list() {
//        Map<String, Object> requestParamDic = new HashMap<>(3);
//        requestParamDic.put("host", ConfigManager.HOST);
//        requestParamDic.put("port", ConfigManager.PORT);
//        requestParamDic.put("path", ConfigManager.HTTP_API_QUEUES);
//        String remoteData = HttpHelper.syncHTTPGet(requestParamDic, ConfigManager.DEFAULT_AUTH_INFO);
//
//        if (remoteData.isEmpty())
//            return new Queue[0];
//        else {
//            Queue[] queues;
//            com.google.gson.JsonParser parser = new JsonParser();
//            JsonElement element = parser.parse(remoteData);
//            if (element.isJsonArray()) {
//                JsonArray queueJsonArr = element.getAsJsonArray();
//                queues = new Queue[queueJsonArr.size()];
//                for (int i = 0; i < queues.length; i++) {
//                    Queue queue = Queue.parse(queueJsonArr.get(i));
//                    queues[i] = queue;
//                }
//            } else {
//                queues = new Queue[0];
//            }
//
//            return queues;
//        }

        return null;

    }

    //endregion


}
