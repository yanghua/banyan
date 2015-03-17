package com.messagebus.managesystem.dao;

import com.messagebus.managesystem.pojo.Authorization;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IAuthorizationMapper {

    @Insert("INSERT INTO AUTHORIZATION (nodeId, appId) VALUES (#{nodeId}, " +
        "#{appId})")
    public void save(Authorization authorization);

    @Select("SELECT * FROM AUTHORIZATION")
    public List<Authorization> findAll();

    @Select("SELECT * FROM AUTHORIZATION WHERE nodeId = #{nodeId}")
    public Authorization find(int id);

    @Select("SELECT * FROM AUTHORIZATION LIMIT #{offset}, #{pageSize}")
    public List<Authorization> findWithPaging(@Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    @Delete("DELETE FROM AUTHORIZATION WHERE nodeId = #{nodeId}")
    public void delete(int id);

    @Select("SELECT COUNT(1) FROM AUTHORIZATION")
    public int countAll();

    @Update("UPDATE AUTHORIZATION SET appId = #{appId} WHERE nodeId = #{nodeId}")
    public void update(Authorization authorization);
}
