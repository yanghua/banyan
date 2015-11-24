package com.messagebus.service.bootstrap;


import com.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.messagebus.service.core.Exchange;
import com.messagebus.service.core.Queue;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MQDataInitializer extends AbstractInitializer {

    private static          Log               logger   = LogFactory.getLog(MQDataInitializer.class);
    private static volatile MQDataInitializer instance = null;

    private MQDataInitializer(String host) {
        super(host);
    }

    public static MQDataInitializer getInstance(String mqHost) {
        if (instance == null) {
            synchronized (MQDataInitializer.class) {
                if (instance == null) {
                    instance = new MQDataInitializer(mqHost);
                }
            }
        }

        return instance;
    }

    public void deleteQueueNoWait(String queueName) {
        try {
            super.init();
            AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(queueName);
            if (deleteOk == null) {
                throw new RuntimeException("delete queue with name : " + queueName + " failed.");
            }
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            super.close();
        }
    }

    public void initExchange(List<Exchange> sortedExchanges, Map<Integer, Exchange> exchangeMap) {
        try {
            super.init();

            //declare exchange
            for (Exchange exchange : sortedExchanges) {
                channel.exchangeDeclare(exchange.getExchangeName(), exchange.getRouterType(), true);
            }

            //bind exchange
            for (Exchange exchange : sortedExchanges) {
                if (exchange.getParentId() == -1)
                    continue;

                channel.exchangeBind(exchange.getExchangeName(),
                                     exchangeMap.get(exchange.getParentId()).getExchangeName(),
                                     exchange.getRoutingKey());
            }
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            super.close();
        }
    }

    public void initQueue(List<Queue> queues) {

        try {
            super.init();

            //declare queue
            for (Queue queue : queues) {
                Map<String, Object> queueConfig = new HashMap<String, Object>(2);

                if (queue.getThreshold() != -1) {
                    queueConfig.put("x-max-length", queue.getThreshold());
                }

                if (queue.getThreshold() != -1 && queue.getMsgBodySize() != -1) {
                    int allMsgSize = queue.getThreshold() * queue.getMsgBodySize();
                    queueConfig.put("x-max-length-bytes", allMsgSize);
                }

                if (queue.getTtl() != -1) {
                    channel.queueDelete(queue.getQueueName());
                    queueConfig.put("x-expires", queue.getTtl());
                }

                if (queue.getTtlPerMsg() != -1) {
                    channel.queueDelete(queue.getQueueName());
                    queueConfig.put("x-message-ttl", queue.getTtlPerMsg());
                }

                channel.queueDeclare(queue.getQueueName(), true, false, false, queueConfig);
            }

            //bind queue
            for (Queue queue : queues) {
                channel.queueBind(queue.getQueueName(),
                                  queue.getBindExchange(),
                                  queue.getRoutingKey());
            }
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            super.close();
        }
    }

    private void destroyTopologyComponent() throws IOException {
        //call reset-app
    }

    private boolean exchangeExists(String exchangeName) throws IOException {
        boolean result = true;
        try {
            channel.exchangeDeclarePassive(exchangeName);
        } catch (IOException e) {
            result = false;
            if (!channel.isOpen()) {
                super.init();
            }
        }

        return result;
    }

    private boolean queueExists(String queueName) throws IOException {
        boolean result = true;
        try {
            channel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            result = false;
            if (!channel.isOpen()) {
                super.init();
            }
        }

        return result;
    }


}
