package org.ofbiz.banyan.service;

import com.messagebus.common.RandomHelper;
import org.ofbiz.banyan.common.Constants;
import org.ofbiz.banyan.common.MessagebusUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.util.Locale;
import java.util.Map;

/**
 * Created by yanghua on 3/12/15.
 */
public class SinkService {

    public static final String module = SinkService.class.getName();

    public static Map<String, Object> createSink(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        try {
            //check flow from is audited
            String flowFrom = (String) context.get("flowFrom");
            GenericValue fromNode = delegator.findOne("Node", UtilMisc.toMap("nodeId", flowFrom), false);
            boolean isFromNodeAudited = fromNode.getString("auditTypeCode") != null
                && fromNode.getString("auditTypeCode").equals(Constants.CODE_OF_AUDIT_TYPE_SUCCESS);

            if (!isFromNodeAudited) {
                Debug.logError("the from node with id : " + flowFrom + " is unaudited", module);
                return ServiceUtil.returnError("the from node with name : " + fromNode.getString("name") + " is unaudited");
            }

            //check flow to is audited
            String flowTo = (String) context.get("flowTo");
            GenericValue toNode = delegator.findOne("Node", UtilMisc.toMap("nodeId", flowTo), false);
            boolean isToNodeAudited = toNode.getString("auditTypeCode") != null
                && toNode.getString("auditTypeCode").equals(Constants.CODE_OF_AUDIT_TYPE_SUCCESS);

            if (!isToNodeAudited) {
                Debug.logError("the to node with id : " + flowTo + " is unaudited", module);
                return ServiceUtil.returnError("the to node with name : " + toNode.getString("name") + " is unaudited");
            }

            //check flow to is not a virtual node
            boolean isToNodeVirtual = toNode.getString("isVirtual") != null && toNode.getString("isVirtual").equals("1");
            if (isToNodeVirtual) {
                Debug.logError(" the to node with id : " + flowTo + " can not be a virtual node", module);
                return ServiceUtil.returnError(" the to node with name : " + toNode.getString("name") + " can not be a virtual node ");
            }

            //check flow from and to is under one communicate-exchange
            String fromCmutType = fromNode.getString("communicateType");
            String toCmutType = toNode.getString("communicateType");
            boolean isSameCommunicateType = judgeSameKindOfCommunicateType(fromCmutType, toCmutType);
            if (!isSameCommunicateType) {
                Debug.logError("the from node with id : " + flowTo + " and the to node with id : " + flowTo
                                   + " is not the same communicate type", module);
                return ServiceUtil.returnError("can not communicate between two different type!");
            }

            //check the sink exists
            long count = delegator.findCountByCondition("Sink", EntityCondition.makeCondition(UtilMisc.toMap("flowFrom", flowFrom,
                                                                                                             "flowTo", flowTo), EntityOperator.AND), null, null);

            if (count > 0) {
                Debug.logError("there is a sink flow from : " + flowFrom + " and flow to : " + flowTo, module);
                return ServiceUtil.returnError("there is a sink flow from : " + flowFrom + " and flow to : " + flowTo);
            }

            String creator = userLogin.getString("userLoginId");

            GenericValue sink = delegator.makeValue("Sink");
            sink.setString("sinkId", delegator.getNextSeqId("Sink"));
            sink.setString("flowFrom", flowFrom);
            sink.setString("fromCommunicateType", fromNode.getString("communicateType"));
            sink.setString("flowTo", flowTo);
            sink.setString("toCommunicateType", toNode.getString("communicateType"));
            sink.setString("enable", "1");
            sink.setString("auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_UNAUDIT);
            sink.setString("creator", creator);

            delegator.create(sink);

            GenericValue flowFromQ = delegator.findOne("Node",
                                                       UtilMisc.toMap("nodeId", flowFrom), false);

            MessagebusUtil.publishAndCacheForNodeView(flowFromQ, delegator);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> auditSink(DispatchContext ctx, Map<String, ? extends Object> context) {
        String sinkId = (String) context.get("sinkId");
        Delegator delegator = ctx.getDelegator();

        try {
            GenericValue sinkInfo = delegator.findOne("Sink", UtilMisc.toMap("sinkId", sinkId), false);
            sinkInfo.setString("auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_SUCCESS);
            sinkInfo.setString("token", RandomHelper.randomNumberAndCharacter(20));
            delegator.store(sinkInfo);

            GenericValue flowFromQ = delegator.findOne("Node",
                                                       UtilMisc.toMap("nodeId", sinkInfo.getString("flowFrom")), false);

            MessagebusUtil.publishAndCacheForNodeView(flowFromQ, delegator);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> removeSink(DispatchContext ctx, Map<String, ? extends Object> context) {
        String sinkId = (String) context.get("sinkId");
        Delegator delegator = ctx.getDelegator();

        try {
            GenericValue sinkInfo = delegator.findOne("Sink", UtilMisc.toMap("sinkId", sinkId), false);
            delegator.removeByCondition("Sink", EntityCondition.makeCondition(UtilMisc.toMap("sinkId", sinkId)));
            GenericValue flowFromQ = delegator.findOne("Node",
                                                       UtilMisc.toMap("nodeId", sinkInfo.getString("flowFrom")), false);
            MessagebusUtil.publishAndCacheForNodeView(flowFromQ, delegator);

            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError("remove sink item with sink id : " + sinkId);
        }
    }

    public static Map<String, Object> switchSink(DispatchContext ctx,
                                                 Map<String, ? extends Object> context) {
        String sinkId = (String) context.get("sinkId");
        Delegator delegator = ctx.getDelegator();

        try {
            GenericValue sinkInfo = delegator.findOne("Sink", UtilMisc.toMap("sinkId", sinkId), false);
            String isEnable = sinkInfo.getString("enable");
            sinkInfo.set("enable", isEnable.equals("0") ? "1" : "0");
            delegator.store(sinkInfo);

            GenericValue flowFromQ = delegator.findOne("Node",
                                                       UtilMisc.toMap("nodeId", sinkInfo.getString("flowFrom")), false);
            MessagebusUtil.publishAndCacheForNodeView(flowFromQ, delegator);

            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    private static boolean judgeSameKindOfCommunicateType(String fromCommunicateType, String toCommunicateType) {
        int fromCmutTypeCode = getCommunicateTypeCode(fromCommunicateType);
        int toCmutTypeCode = getCommunicateTypeCode(toCommunicateType);

        if (fromCmutTypeCode == -1 || toCmutTypeCode == -1) return false;

        return fromCmutTypeCode == toCmutTypeCode;
    }

    private static int getCommunicateTypeCode(String communicateType) {
        switch (communicateType) {
            case "produce":
            case "consume":
            case "produce-consume":
                return 0;

            case "publish":
            case "subscribe":
            case "publish-subscribe":
                return 1;

            case "request":
            case "response":
            case "request-response":
                return 2;

            case "rpcrequest":
            case "rpcresponse":
            case "rpcrequest-rpcresponse":
                return 3;

        }

        return -1;
    }
}
