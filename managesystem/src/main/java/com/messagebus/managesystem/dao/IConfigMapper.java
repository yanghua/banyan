package com.messagebus.managesystem.dao;

import com.messagebus.business.model.Config;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface IConfigMapper {

    @Insert("INSERT INTO CONFIG(KEY, VALUE) values (#{key}, #{value})")
    public void save(Config configItem);

    @Update("UPDATE CONFIG SET VALUE=#{value} WHERE KEY=#{key}")
    public void update(Config config);

    @Select("SELECT * FROM CONFIG WHERE GENERATED_ID = #{id}")
    public Config find(long id);

    @Delete("DELETE FROM CONFIG WHERE GENERATED_ID = #{id}")
    public void delete(long id);

}
