package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.common.GsonUtil;
import com.messagebus.interactor.pubsub.LongLiveZookeeper;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 10/14/15.
 */
@DaemonService(value = "elasticLogService", policy = RunPolicy.ONCE)
public class ElasticLogService extends AbstractService {

    private static final Log    logger                    = LogFactory.getLog(ElasticLogService.class);
    private static final Gson   GSON                      = new Gson();
    private static final String ELASTIC_SHIPPER_SECRET    = "ioqweniuqnweuifnakjwe";
    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";
    private static final String COMPONENT_SEARCH_ZK_PATH  = "/component/search";

    private Map             mbHostAndPortObj;
    private Map             esHostAndPortObj;
    private TransportClient esClient;

    public ElasticLogService(Map<String, Object> context) {
        super(context);

        String zkHost = context.get(Constants.ZK_HOST_KEY).toString();
        int zkPort = Integer.parseInt(context.get(Constants.ZK_PORT_KEY).toString());

        LongLiveZookeeper zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        try {
            zookeeper.open();

            mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);
            esHostAndPortObj = zookeeper.get(COMPONENT_SEARCH_ZK_PATH, Map.class);
        } catch (JsonSyntaxException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            if (zookeeper.isAlive()) {
                zookeeper.close();
            }
        }
    }


    public void run() {
        try {
            Settings settings = ImmutableSettings.settingsBuilder()
                                                 .put("cluster.name", esHostAndPortObj.get("esClusterName"))
    //                                             .put("node.name", "vino")
                                                 .put("client.transport.sniff", true).build();
            esClient = new TransportClient(settings);

            String esHost = esHostAndPortObj.get("esHost").toString();
            int esPort = new Float(esHostAndPortObj.get("esPort").toString()).intValue();

            TransportAddress transportAddress = new InetSocketTransportAddress(esHost, esPort);

            esClient.addTransportAddress(transportAddress);
        } catch (NumberFormatException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }

        String mqHost = mbHostAndPortObj.get("mqHost").toString();
        int mqPort = new Float(mbHostAndPortObj.get("mqPort").toString()).intValue();

        MessagebusPool messagebusPool = new MessagebusSinglePool(mqHost, mqPort);
        final Messagebus client = messagebusPool.getResource();
        try {
            client.consume(ELASTIC_SHIPPER_SECRET, Integer.MAX_VALUE, TimeUnit.SECONDS,
                           new IMessageReceiveListener() {

                               public void onMessage(Message message) {
                                   String msgJsonStr = GSON.toJson(message);
                                   try {
                                       IndexResponse response = esClient.prepareIndex("idx_20151030", "message")
                                                                        .setSource(msgJsonStr)
                                                                        .execute()
                                                                        .actionGet();
                                   } catch (ElasticsearchException e) {
                                       logger.error(e);
                                   }

                               }
                           });
        } finally {
            messagebusPool.returnResource(client);
        }
    }

}
