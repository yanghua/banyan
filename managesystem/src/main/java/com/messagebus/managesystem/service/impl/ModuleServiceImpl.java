package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.dao.IModuleMapper;
import com.messagebus.managesystem.pojo.Module;
import com.messagebus.managesystem.service.IModuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ModuleServiceImpl implements IModuleService {

    @Resource
    private IModuleMapper moduleMapper;

    @Override
    public Module get(String moduleCode) {
        return moduleMapper.find(moduleCode);
    }

    @Override
    public List<Module> getAll() {
        return moduleMapper.findAll();
    }

    @Override
    public List<Module> getWithPaging(int offset, int pageSize) {
        return moduleMapper.findWithPaging(offset, pageSize);
    }

    @Override
    public List<Module> getParentModule() {
        return moduleMapper.findParentModule();
    }

    @Override
    public List<Module> getAllSubModules() {
        return moduleMapper.findSubModules();
    }

    @Override
    public Module getWithModuleValue(String moduleValue) {
        return moduleMapper.findWithValue(moduleValue);
    }

    @Override
    public void create(Module module) {
        moduleMapper.save(module);
    }

    @Override
    public void modify(Module module) {
        moduleMapper.update(module);
    }

    @Override
    public int countAll() {
        return moduleMapper.countAll();
    }

    @Override
    public void remove(String moduleCode) {
        moduleMapper.delete(moduleCode);
    }
}
