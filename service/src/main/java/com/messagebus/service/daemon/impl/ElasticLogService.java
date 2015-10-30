package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 10/14/15.
 */
@DaemonService(value = "elasticLogService", policy = RunPolicy.ONCE)
public class ElasticLogService extends AbstractService {

    private static final Log logger = LogFactory.getLog(ElasticLogService.class);

    private static final Gson   GSON                   = new Gson();
    private static final String ELASTIC_SHIPPER_SECRET = "ioqweniuqnweuifnakjwe";

    private MessagebusPool messagebusPool;
    private String         mqHost;
    private int            mqPort;
    private Client         esClient;

    public ElasticLogService(Map<String, Object> context) {
        super(context);

        mqHost = this.context.get(com.messagebus.service.Constants.MQ_HOST_KEY).toString();
        mqPort = Integer.parseInt(this.context.get(com.messagebus.service.Constants.MQ_PORT_KEY).toString());

        //TODO
        Settings settings = Settings.builder()
                                    .put("cluster.name", "elasticsearch_yanghua")
                                    .put("node.name", "vino")
                                    .build();
        TransportAddress transportAddress = null;
        try {
            transportAddress = new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300);
        } catch (UnknownHostException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        TransportClient transportClient = TransportClient.builder().settings(settings).build();
        esClient = transportClient.addTransportAddress(transportAddress);
    }

    @Override
    public void run() {
        messagebusPool = new MessagebusSinglePool(mqHost, mqPort);
        final Messagebus client = messagebusPool.getResource();
        try {
            client.consume(ELASTIC_SHIPPER_SECRET, Integer.MAX_VALUE, TimeUnit.SECONDS,
                           new IMessageReceiveListener() {
                               @Override
                               public void onMessage(Message message) {
                                   String msgJsonStr = GSON.toJson(message);
                                   IndexResponse response = esClient.prepareIndex("idx_20151030", "message")
                                                                    .setSource(msgJsonStr)
                                                                    .execute()
                                                                    .actionGet();

                                   logger.info(response);
                               }
                           });
        } finally {
            messagebusPool.returnResource(client);
        }
    }

}
