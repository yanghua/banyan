package com.messagebus.httpbridge.controller;

import com.google.common.base.Strings;
import com.messagebus.client.*;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageJSONSerializer;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.httpbridge.util.Constants;
import com.messagebus.httpbridge.util.ResponseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpBridge extends HttpServlet {

    private static final Log logger = LogFactory.getLog(HttpBridge.class);

    private static final String CONSUME_MODE_PULL = "pull";
    private static final String CONSUME_MODE_PUSH = "push";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        logger.info("[service] url is : " + req.getRequestURI());
        String apiType = req.getParameter("type");

        if (Strings.isNullOrEmpty(apiType)) {
            ResponseUtil.response(resp, Constants.HTTP_FAILED_CODE,
                                  "the query string : type can not be null or empty",
                                  "", "''");
            return;
        }

        String secret = req.getParameter("secret");
        if (Strings.isNullOrEmpty(secret)) {
            ResponseUtil.response(resp, Constants.HTTP_FAILED_CODE,
                                  "param : secret can not be null or empty", "", "''");
            return;
        }

        MessageCarryType msgCarryType = MessageCarryType.lookup(apiType);

        switch (msgCarryType) {
            case PRODUCE:
                this.produce(req, resp);
                break;

            case CONSUME:
                this.consume(req, resp);
                break;

            case PUBLISH:
                this.publish(req, resp);
                break;

            case SUBSCRIBE:
                this.subscribe(req, resp);
                break;

            case REQUEST:
                this.request(req, resp);
                break;

            case RESPONSE:
                ResponseUtil.response(resp, Constants.HTTP_FAILED_CODE,
                                      "unsupported type : " + msgCarryType.toString(), "", "''");
                break;

            default:
                ResponseUtil.response(resp, Constants.HTTP_FAILED_CODE,
                                      "invalidate type", "", "''");
        }
    }

    private void produce(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!request.getMethod().toLowerCase().equals("post")) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "error http request method", "", "");
            return;
        }

        String queueName = request.getRequestURI().split("/")[3];
        String token = request.getParameter("token");
        String msgArrStr = request.getParameter("messages");

        if (Strings.isNullOrEmpty(queueName)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "param : qname can not be null or empty", "", "''");
            return;
        }

        if (Strings.isNullOrEmpty(token)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "param : token can not be null or empty", "", "''");
            return;
        }

        if (Strings.isNullOrEmpty(msgArrStr)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "param : messages can not be null or empty", "", "''");
            return;
        }

        Message[] msgArr = MessageJSONSerializer.deSerializeMessages(msgArrStr, MessageType.QueueMessage);

        MessagebusPool pool = (MessagebusPool) (getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ));
        Messagebus messagebus = pool.getResource();

        try {
            messagebus.batchProduce(request.getParameter("secret"), queueName, msgArr, token);
            ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", "''");
        } catch (Exception e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "[produce] occurs a exception : " + e.getMessage(), "", "''");
        } finally {
            pool.returnResource(messagebus);
        }
    }

    private void consume(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!request.getMethod().toLowerCase().equals("get")) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "error http request method", "", "''");
            return;
        }

        String mode = request.getParameter("mode");
        if (Strings.isNullOrEmpty(mode)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "the param : mode can not be null or empty", "", "''");
            return;
        }

        switch (mode.toLowerCase()) {
            case CONSUME_MODE_PULL:
                this.consumeWithPull(request, response);
                break;

            case CONSUME_MODE_PUSH:
                this.consumeWithPush(request, response);
                break;

            default:
                logger.error("[consume] invalidate param : mode with value - " + mode);
        }
    }

    private void publish(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!request.getMethod().toLowerCase().equals("post")) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "error http request method", "", "");
            return;
        }

        String token = request.getParameter("token");
        if (Strings.isNullOrEmpty(token)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "param : token can not be null or empty", "", "''");
            return;
        }

        String msgArrStr = request.getParameter("messages");

        Message[] msgArr = MessageJSONSerializer.deSerializeMessages(msgArrStr, MessageType.QueueMessage);

        MessagebusPool pool = (MessagebusPool) (getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ));
        Messagebus messagebus = pool.getResource();

        try {
            messagebus.publish(request.getParameter("secret"), msgArr, token);
            ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", "''");
        } catch (Exception e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "[produce] occurs a exception : " + e.getMessage(), "", "''");
        } finally {
            pool.returnResource(messagebus);
        }

    }

    private void subscribe(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        if (!request.getMethod().toLowerCase().equals("get")) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "error http request method", "", "''");
            return;
        }

        final Continuation continuation = ContinuationSupport.getContinuation(request);

        if (continuation.isExpired()) {
            ResponseUtil.response(response, Constants.HTTP_TIMEOUT_CODE, "timeout",
                                  "there is no message could be consumed in " +
                                      Constants.MAX_CONSUME_CONTINUATION_TIMEOUT + " ms", "\"\"");
            return;
        }

        continuation.setTimeout(Constants.MAX_CONSUME_CONTINUATION_TIMEOUT);
        continuation.suspend(response);

        final MessagebusPool pool = (MessagebusPool) (getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ));
        final Messagebus messagebus = pool.getResource();
        final List<Message> receivedMsgs = new ArrayList<>();

        continuation.addContinuationListener(new ContinuationListener() {
            @Override
            public void onComplete(Continuation continuation) {
                try {
                    if (receivedMsgs.size() != 0) {
                        String msgStr = MessageJSONSerializer.serializeMessages(receivedMsgs);
                        ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", msgStr);
                    }
                } catch (IOException e) {
                    logger.error("[onComplete] occurs a IOException : " + e.getMessage());
                }
            }

            @Override
            public void onTimeout(Continuation continuation) {
                try {
                    if (receivedMsgs.size() == 0) {
                        ResponseUtil.response(response, Constants.HTTP_TIMEOUT_CODE, "", "", "''");
                    } else {
                        String msgStr = MessageJSONSerializer.serializeMessages(receivedMsgs);
                        ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", msgStr);
                    }
                } catch (IOException e) {
                    logger.error("[onTimeout] occurs a IOException : " + e.getMessage());
                }
            }
        });

        try {
            messagebus.subscribe(request.getParameter("secret"), new IMessageReceiveListener() {

                @Override
                public void onMessage(Message message) {
                    receivedMsgs.add(message);
                }

            }, Constants.MAX_CONSUME_CONTINUATION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("[consumeWithPush] occurs a Exception : " + e.getMessage());
            continuation.undispatch();
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "[consumeWithPush] occurs a Exception : " + e.getMessage(), "", "''");
        } finally {
            pool.returnResource(messagebus);
            continuation.complete();
        }
    }

    private void request(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        if (!request.getMethod().toLowerCase().equals("post")) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "error request method !", "", "''");
            return;
        }


        String timeoutStr = request.getParameter("timeout");
        if (Strings.isNullOrEmpty(timeoutStr)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "param : timeout can not be null or empty", "", "''");
            return;
        }

        long timeout;
        try {
            timeout = Long.parseLong(timeoutStr);
        } catch (NumberFormatException e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "illegal param : timeout ", "", "''");
            return;
        }

        if (timeout < Constants.MIN_REQUEST_TIMEOUT || timeout > Constants.MAX_REQUEST_TIMEOUT) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "invalid param : timeout it should be greater than :" + Constants.MIN_REQUEST_TIMEOUT +
                                      "and less than : " + Constants.MAX_REQUEST_TIMEOUT, "", "''");
            return;
        }

        String token = request.getParameter("token");
        if (Strings.isNullOrEmpty(token)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "param : token can not be null or empty", "", "''");
            return;
        }

        String queueName = request.getRequestURI().split("/")[3];
        String msgStr = request.getParameter("message");

        if (Strings.isNullOrEmpty(msgStr)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "param : msgStr can not be null or empty", "", "''");
            return;
        }

        Message msg = MessageJSONSerializer.deSerialize(msgStr, MessageType.QueueMessage);

        MessagebusPool pool = (MessagebusPool) (getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ));
        Messagebus messagebus = pool.getResource();
        try {
            Message responseMsg = messagebus.request(request.getParameter("secret"), queueName, msg, token, timeout);

            String respMsgStr = MessageJSONSerializer.serialize(responseMsg);
            ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", respMsgStr);
        } catch (MessagebusUnOpenException e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "occurs a messagebus unopen exception", "", "''");
        } catch (MessageResponseTimeoutException e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "occurs a response timeout exception", "", "''");
        } catch (Exception e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "occurs a exception : " + e.getMessage(), "", "''");
        } finally {
            pool.returnResource(messagebus);
        }
    }

    @Deprecated
    private void response(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        throw new UnsupportedOperationException("unsupported operation!");
    }

    private void consumeWithPull(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String numStr = request.getParameter("num");
        if (Strings.isNullOrEmpty(numStr)) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "when consume with pull mode the param :  num can not be null or empty", "", "''");
            return;
        }
        int num = 0;

        try {
            num = Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "invalidate param : num, it must be a integer!", "", "''");
            return;
        }

        if (num < Constants.MIN_CONSUME_NUM || num > Constants.MAX_CONSUME_NUM) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  " invalidate param : num , it should be less than "
                                      + Constants.MAX_CONSUME_NUM + " and greater than " + Constants.MIN_CONSUME_NUM, "", "''");
            return;
        }

        MessagebusPool pool = (MessagebusPool) (getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ));
        Messagebus messagebus = pool.getResource();

        List<Message> messages = null;
        try {
            messages = messagebus.consume(request.getParameter("secret"), num);
        } catch (Exception e) {
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE, "", "", "'[']");
            return;
        } finally {
            pool.returnResource(messagebus);
        }

        if (messages == null) {
            ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", "\"[\"]");
        } else {
            String msgsStr = MessageJSONSerializer.serializeMessages(messages);
            ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", msgsStr);
        }
    }

    private void consumeWithPush(HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        final Continuation continuation = ContinuationSupport.getContinuation(request);

        if (continuation.isExpired()) {
            ResponseUtil.response(response, Constants.HTTP_TIMEOUT_CODE, "timeout",
                                  "there is no message could be consumed in " +
                                      Constants.MAX_CONSUME_CONTINUATION_TIMEOUT + " ms", "\"\"");
            return;
        }

        continuation.setTimeout(Constants.MAX_CONSUME_CONTINUATION_TIMEOUT);
        continuation.suspend(response);

        final MessagebusPool pool = (MessagebusPool) (getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ));
        final Messagebus messagebus = pool.getResource();

        final List<Message> receivedMsgs = new ArrayList<>();

        continuation.addContinuationListener(new ContinuationListener() {
            @Override
            public void onComplete(Continuation continuation) {
                try {
                    if (receivedMsgs.size() != 0) {
                        String msgStr = MessageJSONSerializer.serializeMessages(receivedMsgs);
                        ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", msgStr);
                    }
                } catch (IOException e) {
                    logger.error("[onComplete] occurs a IOException : " + e.getMessage());
                }
            }

            @Override
            public void onTimeout(Continuation continuation) {
                try {
                    if (receivedMsgs.size() == 0) {
                        ResponseUtil.response(response, Constants.HTTP_TIMEOUT_CODE, "", "", "''");
                    } else {
                        String msgStr = MessageJSONSerializer.serializeMessages(receivedMsgs);
                        ResponseUtil.response(response, Constants.HTTP_SUCCESS_CODE, "", "", msgStr);
                    }
                } catch (IOException e) {
                    logger.error("[onTimeout] occurs a IOException : " + e.getMessage());
                }
            }
        });

        try {
            messagebus.consume(request.getParameter("secret"),
                               Constants.MAX_CONSUME_CONTINUATION_TIMEOUT,
                               TimeUnit.MILLISECONDS,
                               new IMessageReceiveListener() {
                                   @Override
                                   public void onMessage(Message message) {
                                       receivedMsgs.add(message);
                                   }
                               });
        } catch (Exception e) {
            logger.error("[consumeWithPush] occurs a Exception : " + e.getMessage());
            continuation.undispatch();
            ResponseUtil.response(response, Constants.HTTP_FAILED_CODE,
                                  "[consumeWithPush] occurs a Exception : " + e.getMessage(), "", "''");
        } finally {
            pool.returnResource(messagebus);
            continuation.complete();
        }
    }

}
