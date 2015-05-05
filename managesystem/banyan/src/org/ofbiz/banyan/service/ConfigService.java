package org.ofbiz.banyan.service;

import com.messagebus.common.Constants;
import org.ofbiz.banyan.common.MessagebusUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.util.Locale;
import java.util.Map;

/**
 * Created by yanghua on 3/10/15.
 */
public class ConfigService {

    public static final String module = ConfigService.class.getName();

    public static Map<String, Object> createConfigItem(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String itemKey = (String) context.get("itemKey");
        String itemValue = (String) context.get("itemValue");
        String itemType = (String) context.get("type");

        GenericValue item = delegator.makeValue("Config");
        item.set("configId", delegator.getNextSeqId("Config"));
        item.set("itemKey", itemKey);
        item.set("itemValue", itemValue);
        item.set("type", itemType);

        try {
            delegator.create(item);
//            MessagebusUtil.sendCmdRequestToServer("INSERT", "CONFIG");
            MessagebusUtil.publishEvent(Constants.PUBSUB_CONFIG_CHANNEL, itemValue, true);
            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> updateConfigItem(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String configId = (String) context.get("configId");
        String itemKey = (String) context.get("itemKey");
        String itemValue = (String) context.get("itemValue");
        String type = (String) context.get("type");

        try {
            GenericValue oldConfigItem = delegator.findOne("Config", UtilMisc.toMap("configId", configId), false);
            oldConfigItem.setString("itemKey", itemKey);
            oldConfigItem.setString("itemValue", itemValue);
            oldConfigItem.setString("type", type);

            delegator.store(oldConfigItem);
            MessagebusUtil.publishEvent(Constants.PUBSUB_CONFIG_CHANNEL, itemValue, true);
            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> removeConfigItem(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        String configId = (String) context.get("configId");

        try {
            GenericValue configItem = delegator.findOne("Config", UtilMisc.toMap("configId", configId), false);
            configItem.remove();
            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

}
