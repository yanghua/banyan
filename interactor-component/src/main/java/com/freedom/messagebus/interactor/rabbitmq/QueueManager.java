package com.freedom.messagebus.interactor.rabbitmq;

import com.freedom.messagebus.common.AbstractInitializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class QueueManager extends AbstractInitializer {

    private static final Log logger = LogFactory.getLog(QueueManager.class);

    private static volatile QueueManager instance = null;

    private QueueManager(String host) {
        super(host);
    }

    public static QueueManager defaultQueueManager(String host) {
        if (instance == null) {
            synchronized (QueueManager.class) {
                if (instance == null)
                    instance = new QueueManager(host);
            }
        }

        return instance;
    }

    public void create(@NotNull String queueName) throws IOException {
        super.init();
        if (queueName.isEmpty()) {
            logger.error("[create] queueName param is empty");
            throw new IOException("[create] queueName param is empty");
        }
        this.channel.queueDeclare(queueName, true, false, false, null);
        super.close();
    }

    public void create(@NotNull String queueName, String bindTo, String routingKey) throws IOException {
        super.init();
        if (queueName.isEmpty()) {
            logger.error("[create] queueName param is empty");
            throw new IOException("[create] queueName param is empty");
        }

        this.channel.queueDeclare(queueName, true, false, false, null);
        if (bindTo != null && !bindTo.isEmpty() && this.innerExists(bindTo, this.channel))
            this.channel.queueBind(queueName, bindTo, routingKey);
        super.close();
    }

    public void bind(@NotNull String queueName, @NotNull String bindTo, String routingKey) throws IOException {
        super.init();
        if (!this.innerExists(queueName, this.channel)) {
            logger.error("[bind] queue : " + queueName + " is not exists!");
            throw new IOException("queue : " + queueName + " is not exists!");
        }

        if (!this.innerExists(bindTo, this.channel)) {
            logger.error("[bind] bindTo : " + bindTo + " is not exists!");
            throw new IOException("[bind] bindTo : " + bindTo + " is not exists!");
        }
        this.channel.queueBind(queueName, bindTo, routingKey);

        super.close();
    }

    public void unbind(@NotNull String queueName, @NotNull String unbindTo, String routingKey) throws IOException {
        super.init();
        if (queueName.isEmpty()) {
            logger.error("[unbind] queueName is empty");
            throw new IOException("[unbind] queueName is empty");
        }

        if (unbindTo.isEmpty()) {
            logger.error("[ubind] unbindTo param is empty");
            throw new IOException("[ubind] unbindTo param is empty");
        }

        if (!this.innerExists(queueName, this.channel)) {
            logger.error("[unbind] queue : " + queueName + " is not exists");
        }

        if (!this.innerExists(unbindTo, this.channel)) {
            logger.error("[unbind] unbind queue : " + unbindTo + " is not exists");
        }

        this.channel.queueUnbind(queueName, unbindTo, routingKey);
        super.close();
    }

    public void delete(@NotNull String queueName) throws IOException {
        super.init();
        if (queueName.isEmpty()) {
            logger.error("[delete] queueName is empty");
            throw new IOException("[delete] queueName is empty");
        }

        if (!this.innerExists(queueName, this.channel)) {
            logger.error("[delete] queue : " + queueName + " is not exists");
        }

        this.channel.queueDelete(queueName);
        super.close();
    }

    public boolean exists(String queueName) throws IOException {
        super.init();
        boolean result = true;
        try {
            AMQP.Queue.DeclareOk declareOk = this.channel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            result = false;
        }
        super.close();

        return result;
    }

    /**
     * for other function, it borrow a outer channel
     * @param queueName
     * @param outerChannel
     * @return
     */
    private boolean innerExists(String queueName, Channel outerChannel) {
        boolean result = true;
        try {
            AMQP.Queue.DeclareOk declareOk = outerChannel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            result = false;
        }

        return result;
    }

}
