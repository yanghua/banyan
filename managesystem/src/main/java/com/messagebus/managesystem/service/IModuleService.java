package com.messagebus.managesystem.service;

import com.messagebus.managesystem.pojo.Module;

import java.util.List;

public interface IModuleService {

    public Module get(String moduleCode);

    public List<Module> getAll();

    public List<Module> getWithPaging(int offset, int pageSize);

    public Module getWithModuleValue(String moduleValue);

    public List<Module> getParentModule();

    public List<Module> getAllSubModules();

    public void create(Module module);

    public void modify(Module module);

    public int countAll();

    public void remove(String moduleCode);

}
