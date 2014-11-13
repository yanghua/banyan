package com.freedom.managesystem.dao.sqlprovider;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class NodeProvider {

    public String dynamicQuery(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("n.*").FROM(" NODE n");

            if (params.containsKey("type")) {
                WHERE(" `type` = " + params.get("type").toString());
            }

        }}.toString();
    }

    public String queryQueue(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("n.*").FROM(" NODE n");

            WHERE(" `type` != 0 AND `inner` = 0 ");

            boolean isPubsub = Boolean.valueOf(params.get("isPubsub").toString());

            //reverse
            if (isPubsub) {
                WHERE(" `name` LIKE '%-pubsub' ");
            } else {
                WHERE(" `name` NOT LIKE '%-pubsub' ");
            }

            String nodeId = params.get("targetId").toString();
            WHERE(" nodeId != " + nodeId);

        }}.toString();
    }

    public String countQueue(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("COUNT(1)").FROM(" NODE n");

            WHERE(" `type` != 0 AND `inner` = 0 ");

            boolean isPubsub = Boolean.valueOf(params.get("isPubsub").toString());

            //reverse
            if (isPubsub) {
                WHERE(" `name` LIKE '%-pubsub' ");
            } else {
                WHERE(" `name` NOT LIKE '%-pubsub' ");
            }

            String nodeId = params.get("targetId").toString();
            WHERE(" nodeId != " + nodeId);

        }}.toString();
    }

}
