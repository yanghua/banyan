package com.freedom.managesystem.dao;

import com.freedom.messagebus.business.model.SendPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ISendPermissionMapper {

    @Insert("INSERT INTO SEND_PERMISSION VALUES(#{targetId},#{grantId})")
    public void create(SendPermission permission);

    @Select("SELECT * FROM VIEW_SEND_PERMISSION")
    public List<SendPermission> findAll();

    @Select("SELECT * FROM VIEW_SEND_PERMISSION WHERE targetId = #{targetId}")
    public List<SendPermission> findWithTargetId(@Param("targetId") int targetId);

    @Delete("DELETE FROM SEND_PERMISSION WHERE targetId = #{targetId} and grantId = #{grantId}")
    public void delete(@Param("targetId") int targetId, @Param("grantId") int grantId);

}
