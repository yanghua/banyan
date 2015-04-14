package com.messagebus.managesystem.pojo.rabbitHTTP;

import com.messagebus.common.NumberHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Queue {

    private String name;
    private String state;
    private String durable;
    private String autoDelete;
    private long   ramMsgCount;
    private double avgEgressRate;
    private double avgIngressRate;
    private double avgAckIngressRate;
    private double avgAckEgressRate;
    private double memSizeOfMB;

    public Queue() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDurable() {
        return durable;
    }

    public void setDurable(String durable) {
        this.durable = durable;
    }

    public String getAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(String autoDelete) {
        this.autoDelete = autoDelete;
    }

    public long getRamMsgCount() {
        return ramMsgCount;
    }

    public void setRamMsgCount(long ramMsgCount) {
        this.ramMsgCount = ramMsgCount;
    }

    public double getAvgEgressRate() {
        return avgEgressRate;
    }

    public void setAvgEgressRate(double avgEgressRate) {
        this.avgEgressRate = avgEgressRate;
    }

    public double getAvgIngressRate() {
        return avgIngressRate;
    }

    public void setAvgIngressRate(double avgIngressRate) {
        this.avgIngressRate = avgIngressRate;
    }

    public double getAvgAckIngressRate() {
        return avgAckIngressRate;
    }

    public void setAvgAckIngressRate(double avgAckIngressRate) {
        this.avgAckIngressRate = avgAckIngressRate;
    }

    public double getAvgAckEgressRate() {
        return avgAckEgressRate;
    }

    public void setAvgAckEgressRate(double avgAckEgressRate) {
        this.avgAckEgressRate = avgAckEgressRate;
    }

    public double getMemSizeOfMB() {
        return memSizeOfMB;
    }

    public void setMemSizeOfMB(double memSizeOfMB) {
        this.memSizeOfMB = memSizeOfMB;
    }

    public static Queue parse(JsonElement queueJsonElement) {
//        JsonObject queueObj = queueJsonElement.getAsJsonObject();
//        Queue queue = new Queue();
//        queue.setName(queueObj.get("name").getAsString());
//        queue.setState(queueObj.get("state").getAsString());
//        queue.setDurable(queueObj.get("durable").getAsString());
//        queue.setAutoDelete(queueObj.get("auto_delete").getAsString());
//        queue.setMemSizeOfMB(queueObj.get("memory").getAsLong() / 1_000_000);
//
//        //inner property
//        JsonObject queueStatusObj = queueObj.getAsJsonObject("backing_queue_status");
//        double air = queueStatusObj.get("avg_ingress_rate").getAsDouble();
//        queue.setAvgIngressRate(NumberHelper.fractionDigits(air, 2));
//
//        double aer = queueStatusObj.get("avg_egress_rate").getAsDouble();
//        queue.setAvgAckEgressRate(NumberHelper.fractionDigits(aer, 2));
//
//        double aair = queueStatusObj.get("avg_ack_ingress_rate").getAsDouble();
//        queue.setAvgAckIngressRate(NumberHelper.fractionDigits(aair, 2));
//
//        double aaer = queueStatusObj.get("avg_ack_egress_rate").getAsDouble();
//        queue.setAvgAckEgressRate(NumberHelper.fractionDigits(aaer, 2));
//
//        return queue;
        return null;
    }
}
