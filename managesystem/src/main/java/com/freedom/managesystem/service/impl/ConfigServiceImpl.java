package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.IConfigMapper;
import com.freedom.managesystem.service.IConfigService;
import com.freedom.messagebus.business.model.Config;
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
