package com.freedom.messagebus.common.model;

import java.util.Map;

/**
 * Desc: the entity of message. encapsulated message-protocol
 * User: yanghua
 * Date: 6/27/14
 * Time: 12:03 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class Message extends BaseModel {

    private           String              appKeyFrom;                  //message come from
    private           String              appKeyTo;                    //message arrive in
    private           Map<String, Object> originInfo;     //sender info
    private           Map<String, Object> targetInfo;     //receiver info
    private           String              messageType;                 //message type
    private           String              sendType;                    //send type
    private           String              messageBody;                 //message body
    private           short               needReceipt;                  //need receipt ?
    private           Map<String, Object> receiptInfo;    //receipt info
    private           String              sendStrategy;                //send strategy (right now or delay)
    private           String              sendMode;                    //send mode (single or broadcast)
    private transient Map<String, Object> extraParams;    //a context of extra params

    public Message() {
    }

    public String getAppKeyFrom() {
        return appKeyFrom;
    }

    public void setAppKeyFrom(String appKeyFrom) {
        this.appKeyFrom = appKeyFrom;
    }

    public String getAppKeyTo() {
        return appKeyTo;
    }

    public void setAppKeyTo(String appKeyTo) {
        this.appKeyTo = appKeyTo;
    }

    public Map<String, Object> getOriginInfo() {
        return originInfo;
    }

    public void setOriginInfo(Map<String, Object> originInfo) {
        this.originInfo = originInfo;
    }

    public Map<String, Object> getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(Map<String, Object> targetInfo) {
        this.targetInfo = targetInfo;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public short getNeedReceipt() {
        return needReceipt;
    }

    public void setNeedReceipt(short needReceipt) {
        this.needReceipt = needReceipt;
    }

    public Map<String, Object> getReceiptInfo() {
        return receiptInfo;
    }

    public void setReceiptInfo(Map<String, Object> receiptInfo) {
        this.receiptInfo = receiptInfo;
    }

    public String getSendStrategy() {
        return sendStrategy;
    }

    public void setSendStrategy(String sendStrategy) {
        this.sendStrategy = sendStrategy;
    }

    public String getSendMode() {
        return sendMode;
    }

    public void setSendMode(String sendMode) {
        this.sendMode = sendMode;
    }

    public Map<String, Object> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }

    @Override
    public String toString() {
        return "Message{" +
            "appKeyFrom='" + appKeyFrom + '\'' +
            ", appKeyTo='" + appKeyTo + '\'' +
            ", originInfo=" + originInfo +
            ", targetInfo=" + targetInfo +
            ", messageType='" + messageType + '\'' +
            ", sendType='" + sendType + '\'' +
            ", messageBody='" + messageBody + '\'' +
            ", needReceipt=" + needReceipt +
            ", receiptInfo=" + receiptInfo +
            ", sendStrategy='" + sendStrategy + '\'' +
            ", sendMode='" + sendMode + '\'' +
            "} " + super.toString();
    }
}
