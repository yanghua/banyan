package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.dao.IConfigMapper;
import com.messagebus.managesystem.service.IConfigService;
import com.messagebus.business.model.Config;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ConfigServiceImpl implements IConfigService {

    @Resource
    private IConfigMapper mapper;

    @Override
    public Config get(long id) {
        return mapper.find(id);
    }
}
