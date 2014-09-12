package com.freedom.managesystem.service;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IQueueService {

    public void load(String appId, Map<String, Object> params);

    public void unLoad(String appId, Map<String, Object> params);

    public void enable(String appId, Map<String, Object> params);

    public void disable(String appId, Map<String, Object> params);

    @NotNull
    public String listAll();
}
