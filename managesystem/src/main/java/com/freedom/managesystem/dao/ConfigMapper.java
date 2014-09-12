package com.freedom.managesystem.dao;

import com.freedom.managesystem.pojo.Config;

public interface ConfigMapper {

    public void save(Config configItem);

    public void update(Config config);

    public Config find(long id);

    public void delete(long id);

}
