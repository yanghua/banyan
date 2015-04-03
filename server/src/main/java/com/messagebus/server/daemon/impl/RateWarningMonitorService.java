package com.messagebus.server.daemon.impl;

import com.google.common.base.Strings;
import com.messagebus.business.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.common.GsonUtil;
import com.messagebus.common.HttpHelper;
import com.messagebus.common.RandomHelper;
import com.messagebus.server.daemon.DaemonService;
import com.messagebus.server.daemon.RunPolicy;
import com.messagebus.server.dataaccess.BusinessDataAccessor;
import com.messagebus.server.dataaccess.DBAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@DaemonService(value = "rateWarningService", policy = RunPolicy.CYCLE_SCHEDULED)
public class RateWarningMonitorService extends AbstractService {

    private static final Log logger = LogFactory.getLog(RateWarningMonitorService.class);

    private Properties serverConfig;

    public RateWarningMonitorService(Map<String, Object> context) {
        super(context);

        this.serverConfig = (Properties) context.get(com.messagebus.server.Constants.KEY_SERVER_CONFIG);
    }

    @Override
    public void run() {
        DBAccessor dbAccessor = new DBAccessor(this.serverConfig);
        List<Node> rateLimitedQueues = BusinessDataAccessor.filterRateLimitedQueues(dbAccessor);

        String jsonData = getQueuesInfo();
        if (Strings.isNullOrEmpty(jsonData)) {
            return;
        }

        List<Object> remoteObjs = GsonUtil.jsonStrToList(jsonData);

        for (Node queue : rateLimitedQueues) {
            String queueName = queue.getValue();
            for (Object queueInfoObj : remoteObjs) {
                Map<String, Object> queueInfo = (Map) queueInfoObj;
                if (queueInfo.get("name").equals(queueName)) {
                    Map<String, Object> msgStatsInfo = (Map) queueInfoObj;
                    Map<String, Object> publishDetailMap = (Map) msgStatsInfo.get("publish_details");
                    int benchmark = Integer.parseInt(queue.getRateLimit());
                    int realRate = Integer.parseInt(publishDetailMap.get("rate").toString());
                    //log to rate limit
                    if (realRate > benchmark) {
                        Map<String, Object> rateWarningObj = new HashMap<>(1);
                        rateWarningObj.put("WARNING_ID", RandomHelper.randomNumberAndCharacter(12));
                        rateWarningObj.put("NODE_ID", queue.getNodeId());
                        rateWarningObj.put("RATE_LIMIT", queue.getRateLimit());
                        rateWarningObj.put("REAL_RATE", publishDetailMap.get("rate").toString());
                        rateWarningObj.put("FROM_DATE",  new Date(new java.util.Date().getTime()));
                        BusinessDataAccessor.addRateWarning(rateWarningObj, dbAccessor);
                    }
                }
            }
        }
    }

    private String getQueuesInfo() {
        Map<String, Object> requestParamDic = new HashMap<>(3);
        requestParamDic.put("host", com.messagebus.server.Constants.HOST);
        requestParamDic.put("port", com.messagebus.server.Constants.PORT);
        requestParamDic.put("path", com.messagebus.server.Constants.HTTP_API_QUEUES);
        return HttpHelper.syncHTTPGet(requestParamDic, com.messagebus.server.Constants.DEFAULT_AUTH_INFO);
    }
}
