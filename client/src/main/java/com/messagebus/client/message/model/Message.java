package com.messagebus.client.message.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Cloneable, Serializable {

    //header
    private long                messageId;
    private String              type;
    private long                timestamp;
    private short               priority;
    private String              expiration;
    private Map<String, Object> headers;
    private String              contentEncoding;
    private String              contentType;
    private String              replyTo;
    private String              appId;
    private String              userId;
    private String              clusterId;
    private String              correlationId;

    private short deliveryMode = 2;

    private byte[]      content;

    public Message() {}

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    public String getExpiration() {
        return expiration;
    }

    public short getDeliveryMode() {
        return 2;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    protected void setUserId(String userId) {
        this.userId = null;
    }

    public String getClusterId() {
        return clusterId;
    }

    protected void setClusterId(String clusterId) {
        this.clusterId = null;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Message cloneObj = (Message) super.clone();
        if (cloneObj.getHeaders() != null) {
            HashMap<String, Object> header = new HashMap<String, Object>(cloneObj.getHeaders().size());
            for (Map.Entry<String, Object> item : cloneObj.getHeaders().entrySet()) {
                header.put(item.getKey(), item.getValue());
            }
            cloneObj.setHeaders(header);
        } else {
            cloneObj.setHeaders(new HashMap<String, Object>(1));
        }

        return cloneObj;
    }
}
