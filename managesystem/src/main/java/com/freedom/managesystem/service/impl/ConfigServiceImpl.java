package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.ConfigMapper;
import com.freedom.managesystem.pojo.Config;
import com.freedom.managesystem.service.IConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ConfigServiceImpl implements IConfigService {

    @Resource
    private ConfigMapper mapper;

    @Override
    public Config get(long id) {
        return mapper.find(id);
    }
}
