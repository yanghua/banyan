package com.messagebus.managesystem.dao;

import com.messagebus.business.model.ReceivePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IReceivePermissionMapper {

    @Insert("INSERT INTO RECEIVE_PERMISSION VALUES(#{targetId},#{grantId})")
    public void create(ReceivePermission permission);

    @Select("SELECT * FROM VIEW_RECEIVE_PERMISSION")
    public List<ReceivePermission> findAll();

    @Select("SELECT * FROM VIEW_RECEIVE_PERMISSION WHERE targetId = #{targetId}")
    public List<ReceivePermission> findWithTargetId(@Param("targetId") int targetId);

    @Delete("DELETE FROM RECEIVE_PERMISSION WHERE targetId = #{targetId} and grantId = #{grantId}")
    public void delete(@Param("targetId") int targetId, @Param("grantId") int grantId);

}
