package org.ofbiz.banyan.service;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.util.Locale;
import java.util.Map;

/**
 * Created by yanghua on 3/12/15.
 */
public class AppService {

    public static final String module = AppService.class.getName();

    public static Map<String, Object> createApp(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String appName = (String) context.get("name");

        GenericValue app = delegator.makeValue("App");
        app.setString("appId", delegator.getNextSeqId("App"));
        app.setString("name", appName);
        app.setString("creator", userLogin.getString("userLoginId"));
        app.setString("fromDate", UtilDateTime.nowTimestamp().toString());

        try {
            delegator.create(app);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> updateApp(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String appName = (String) context.get("name");
        String appId = (String) context.get("appId");

        try {
            GenericValue app = delegator.findOne("App", UtilMisc.toMap("appId", appId), false);
            app.setString("name", appName);
            delegator.store(app);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> deleteApp(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String appId = (String) context.get("appId");

        EntityCondition where = EntityCondition.makeConditionWhere(" APP_ID = " + appId);
        try {
            delegator.removeByCondition("App", where);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

}
