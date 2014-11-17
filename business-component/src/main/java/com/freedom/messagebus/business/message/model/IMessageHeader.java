package com.freedom.messagebus.business.message.model;


import java.util.Date;
import java.util.Map;

public interface IMessageHeader {

    public long getMessageId();

    public void setMessageId(long messageId);

    public String getType();

//    public void setType(String type);

    public Date getTimestamp();

    public void setTimestamp(Date timestamp);

    public short getPriority();

    public void setPriority(short priority);

    public String getExpiration();

//    public void setExpiration(String expiration);

    public short getDeliveryMode();

//    public void setDeliveryMode(short deliveryMode);

    public Map<String, Object> getHeaders();

    public void setHeaders(Map<String, Object> headers);

    public String getContentEncoding();

    public void setContentEncoding(String contentEncoding);

    public String getContentType();

    public void setContentType(String contentType);

    public String getReplyTo();

    public void setReplyTo(String replyTo);

    public String getAppId();

    public void setAppId(String appId);

    public String getUserId();

//    public void setUserId(String userId);

    public String getClusterId();

//    public void setClusterId(String clusterId);

    public String getCorrelationId();

    public void setCorrelationId(String correlationId);

}
