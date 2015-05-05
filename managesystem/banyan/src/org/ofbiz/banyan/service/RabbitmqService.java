package org.ofbiz.banyan.service;

import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import org.ofbiz.banyan.common.Constants;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.util.Map;

/**
 * Created by yanghua on 4/17/15.
 */
public class RabbitmqService {

    public static final String module = RabbitmqService.class.getName();

    public static Map<String, Object> getRabbitmqServerOverview(DispatchContext ctx, Map<String, ? extends Object> context) {
        UtilCache<String, Object> poolUtilCache = UtilCache.findCache(Constants.KEY_OF_BANYAN_GLOBAL_CACHE);
        MessagebusPool pool = (MessagebusPool) poolUtilCache.get(Constants.KEY_OF_MESSAGEBUS_POOL);
        Debug.logInfo("is pool null : " + (pool == null), module);
        String token = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.sink.serverInfoRequestResponse.token");
        String secret = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.queue.serverInfoRequest.secret");
        Messagebus client = pool.getResource();
        Message requestMsg = MessageFactory.createMessage(MessageType.QueueMessage);
        Message respMsg = null;
        String jsonStr;
        try {
            respMsg = client.request(secret, "serverInfoResponse", requestMsg, token, Constants.REQUEST_DEFAULT_TIMEOUT);
            jsonStr = new String(respMsg.getContent());
            Debug.logInfo(jsonStr, module);
            Map<String, Object> resultMap = ServiceUtil.returnSuccess();
            resultMap.put("result", jsonStr);

            return resultMap;
        } catch (MessageResponseTimeoutException e) {
            Debug.logError("occured message response timeout exception", module);
            Map<String, Object> resultMap = ServiceUtil.returnError(e.getMessage());
            resultMap.put("result", "");

            return resultMap;
        } catch (Exception e) {
            Debug.logError(e, module);
            Map<String, Object> resultMap = ServiceUtil.returnError(e.getMessage());
            resultMap.put("result", "");

            return resultMap;
        } finally {
            pool.returnResource(client);
        }
    }

}
