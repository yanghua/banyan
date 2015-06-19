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

        String appId = null;
        Object optionalAppId = context.get("appId");
        if (optionalAppId != null) {
            appId = optionalAppId.toString();
        }

        try {
            long countWithAppName = delegator.findCountByCondition("App", EntityCondition.makeCondition(
                UtilMisc.toMap("name", appName)), null, null);

            if (countWithAppName != 0) ServiceUtil.returnError("the app with name : " + appName + " exists");

            if (appId != null) {
                long countWithAppId = delegator.findCountByCondition("App", EntityCondition.makeCondition(
                    UtilMisc.toMap("appId", appId)), null, null);

                if (countWithAppId != 0) ServiceUtil.returnError("the app with id : " + appId + " exists");
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        if (innerCreateApp(delegator, userLogin, appId, appName)) {
            return ServiceUtil.returnSuccess();
        } else {
            return ServiceUtil.returnError("create app error.");
        }
    }

    public static Map<String, Object> batchCreateApp(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        Map<String, ? extends Object> apps = (Map) context.get("apps");
        try {
            for (Map.Entry<String, ? extends Object> app : apps.entrySet()) {
                String appId = app.getKey();
                String appName = app.getValue() == null ? null : (String) app.getValue();
                long countWithAppName = delegator.findCountByCondition("App", EntityCondition.makeCondition(
                    UtilMisc.toMap("name", appName)), null, null);

                if (countWithAppName != 0) continue;

                if (appId != null) {
                    long countWithAppId = delegator.findCountByCondition("App", EntityCondition.makeCondition(
                        UtilMisc.toMap("appId", appId)), null, null);

                    if (countWithAppId != 0) continue;
                }

                innerCreateApp(delegator, userLogin, appId, appName);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    private static boolean innerCreateApp(Delegator delegator, GenericValue userLogin, String appId, String appName) {
        try {
            GenericValue app = delegator.makeValue("App");
            app.setString("appId", appId == null ? delegator.getNextSeqId("App") : appId);
            app.setString("name", appName);
            app.setString("creator", userLogin.getString("userLoginId"));
            app.setString("fromDate", UtilDateTime.nowTimestamp().toString());


            delegator.create(app);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return false;
        }

        return true;
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
