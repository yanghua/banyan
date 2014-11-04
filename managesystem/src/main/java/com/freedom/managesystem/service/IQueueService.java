package com.freedom.managesystem.service;

import com.freedom.managesystem.pojo.rabbitHTTP.Queue;
import org.jetbrains.annotations.NotNull;

public interface IQueueService {

//    public void load(String appId, Map<String, Object> params);
//
//    public void unLoad(String appId, Map<String, Object> params);
//
//    public void enable(String appId, Map<String, Object> params);
//
//    public void disable(String appId, Map<String, Object> params);

    @NotNull
    public Queue[] list();
}
