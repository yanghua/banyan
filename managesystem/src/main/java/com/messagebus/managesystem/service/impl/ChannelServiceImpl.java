package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.core.ConfigManager;
import com.messagebus.managesystem.pojo.rabbitHTTP.Channel;
import com.messagebus.managesystem.service.IChannelService;
import com.messagebus.common.HttpHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChannelServiceImpl implements IChannelService {

    @Override
    public Channel[] list() {
        Map<String, Object> requestParamDic = new HashMap<>(3);
        requestParamDic.put("host", ConfigManager.HOST);
        requestParamDic.put("port", ConfigManager.PORT);
        requestParamDic.put("path", ConfigManager.HTTP_API_CHANNEL);
        String remoteData = HttpHelper.syncHTTPGet(requestParamDic, ConfigManager.DEFAULT_AUTH_INFO);

        if (remoteData.isEmpty())
            return new Channel[0];
        else {
            Channel[] channels;
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(remoteData);
            if (element.isJsonArray()) {
                JsonArray channelJsonArr = element.getAsJsonArray();
                channels = new Channel[channelJsonArr.size()];
                for (int i = 0; i < channelJsonArr.size(); i++) {
                    Channel channel = Channel.parse(channelJsonArr.get(i));
                    channels[i] = channel;
                }
            } else {
                channels = new Channel[0];
            }

            return channels;
        }
    }
}
