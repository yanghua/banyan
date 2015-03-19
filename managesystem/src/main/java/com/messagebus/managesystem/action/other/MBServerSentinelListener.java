package com.messagebus.managesystem.action.other;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

public class MBServerSentinelListener implements ServletContextListener {

    private final static Log logger = LogFactory.getLog(MBServerSentinelListener.class);

    private Sentinel sentinel;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
//        Messagebus messagebus = Messagebus.createClient("5hW0M5wl9H0wO35Eva1tgM9D0p3OL2N8");
//        //TODO:
//        messagebus.setPubsuberHost(Constants.PUBSUBER_HOST);
//        messagebus.setPubsuberPort(Constants.PUBSUBER_PORT);
//        try {
//            messagebus.open();
//            servletContextEvent.getServletContext().setAttribute(Constants.MESSAGEBUS_KEY, messagebus);
//
//            sentinel = new Sentinel(messagebus, servletContextEvent);
//            sentinel.startMonitor();
//        } catch (MessagebusConnectedFailedException e) {
//            logger.error("[contextInitialized] occurs a MessagebusConnectedFailedException : " + e.getMessage());
//        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        sentinel.stopMonitor();
//        Messagebus messagebus = (Messagebus) servletContextEvent.getServletContext().
//            getAttribute(Constants.MESSAGEBUS_KEY);
//        if (messagebus != null && messagebus.isOpen()) {
//            messagebus.close();
//        }
    }

    private static class Sentinel extends Thread {

        private Thread              currentThread;
        private Messagebus          messagebus;
        private Message             pingCmdMsg;
        private ServletContextEvent servletContextEvent;

        private Sentinel(Messagebus mb, ServletContextEvent contextEvent) {
            this.currentThread = new Thread(this);
            this.messagebus = mb;
            this.servletContextEvent = contextEvent;

            Map<String, Object> header = new HashMap<>(1);
            header.put("COMMAND", "PING");
//            pingCmdMsg = MessageFactory.createMessage(MessageType.QueueMessage);
//            pingCmdMsg.getMessageHeader().setHeaders(header);
//
//            Message.MessageBody body = new Message.MessageBody();
//            body.setContent(new byte[0]);
//            pingCmdMsg.setMessageBody(body);
        }

        @Override
        public void run() {
            ServletContext context = servletContextEvent.getServletContext();
            try {
//                while (true) {
//                    try {
//                        QueueMessage responseMsg = (QueueMessage) messagebus.request(, Constants.SERVER_QUEUE_NAME,
//                                                                                       pingCmdMsg, Constants.PING_SERVER_TIME_INTERVAL);
//                        Map<String, Object> msgHeaders = responseMsg.getMessageHeader().getHeaders();
//
//                        if (msgHeaders != null) {
//                            if (msgHeaders.containsKey("COMMAND") && msgHeaders.get("COMMAND").toString().equals("PONG")) {
//                                logger.debug("received pong message.");
//                                context.setAttribute(Constants.IS_SERVER_ONLINE, true);
//                            } else {
//                                context.setAttribute(Constants.IS_SERVER_ONLINE, false);
//                            }
//                        } else {
//                            context.setAttribute(Constants.IS_SERVER_ONLINE, false);
//                        }
//
//                        //sleep every 10 seconds
//                        TimeUnit.MILLISECONDS.sleep(Constants.PING_SERVER_TIME_INTERVAL);
//                    } catch (MessageResponseTimeoutException e) {
//                        logger.info("received server ack timeout!");
//                        context.setAttribute(Constants.IS_SERVER_ONLINE, false);
//                    } catch (InterruptedException e) {
//                        logger.info("sentinel closed");
//                    }
//                }
            } catch (MessagebusUnOpenException e) {
                logger.error("[Sentinel#run] occurs a MessagebusUnOpenException : " + e.getMessage());
            }
        }

        public void startMonitor() {
            this.currentThread.start();
        }

        public void stopMonitor() {
            this.currentThread.interrupt();
        }
    }
}
