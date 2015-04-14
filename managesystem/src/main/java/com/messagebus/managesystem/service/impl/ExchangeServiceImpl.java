package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.core.ConfigManager;
import com.messagebus.managesystem.pojo.rabbitHTTP.Exchange;
import com.messagebus.managesystem.service.IExchangeService;
import com.messagebus.common.HttpHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeServiceImpl implements IExchangeService {

    @Override
    public Exchange[] list() {
//        Map<String, Object> requestParamDic = new HashMap<>(3);
//        requestParamDic.put("host", ConfigManager.HOST);
//        requestParamDic.put("port", ConfigManager.PORT);
//        requestParamDic.put("path", ConfigManager.HTTP_API_EXCHANGE);
//        String remoteData = HttpHelper.syncHTTPGet(requestParamDic, ConfigManager.DEFAULT_AUTH_INFO);
//
//        if (remoteData.isEmpty())
//            return new Exchange[0];
//        else {
//            Exchange[] exchanges;
//            JsonParser parser = new JsonParser();
//            JsonElement element = parser.parse(remoteData);
//
//            if (element.isJsonArray()) {
//                JsonArray exchangeJsonArr = element.getAsJsonArray();
//                exchanges = new Exchange[exchangeJsonArr.size()];
//                for (int i = 0; i < exchanges.length; i++) {
//                    Exchange exchange = Exchange.parse(exchangeJsonArr.get(i));
//                    exchanges[i] = exchange;
//                }
//            } else {
//                exchanges = new Exchange[0];
//            }
//
//            return exchanges;
//        }

        return null;
    }
}
