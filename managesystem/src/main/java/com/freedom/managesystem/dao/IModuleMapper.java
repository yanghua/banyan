package com.freedom.managesystem.dao;

import com.freedom.managesystem.pojo.Module;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IModuleMapper {

    @Select("SELECT * FROM MODULE ORDER BY parentModule,sortIndex")
    public List<Module> findAll();

    @Select("SELECT * FROM MODULE WHERE moduleValue = #{moduleCode}")
    public Module find(String moduleCode);

    @Select("SELECT * FROM MODULE WHERE moduleValue = #{moduleValue}")
    public Module findWithValue(String moduleValue);

    @Select("SELECT * FROM MODULE ORDER BY parentModule,sortIndex LIMIT #{offset}, #{pageSize} ")
    public List<Module> findWithPaging(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT * FROM MODULE WHERE parentModule = -1 ORDER BY sortIndex")
    public List<Module> findParentModule();

    @Select("SELECT * FROM MODULE WHERE parentModule != -1 ORDER BY sortIndex")
    public List<Module> findSubModules();

    @Select("SELECT COUNT(1) FROM MODULE")
    public int countAll();

    @Insert("INSERT INTO MODULE VALUES(rand_string(4), #{moduleName}, " +
                "#{moduleValue}, #{linkUrl}, #{parentModule}, #{sortIndex}, #{remark})")
    public void save(Module module);

    @Update("UPDATE MODULE SET moduleName = #{moduleName}, moduleValue = #{moduleValue}," +
                " linkUrl = #{linkUrl}, parentModule = #{parentModule}, sortIndex = #{sortIndex}, remark = #{remark} " +
                " WHERE moduleCode = #{moduleCode} ")
    public void update(Module module);

    @Delete("DELETE FROM MODULE WHERE moduleCode = #{moduleCode}")
    public void delete(String moduleCode);

}
