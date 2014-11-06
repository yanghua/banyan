package com.freedom.managesystem.dao;

import com.freedom.messagebus.common.model.Node;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface INodeMapper {

    @Insert("INSERT INTO NODE(name, value, parentId, type, level, routerType, routingKey, available, appId, `inner`) VALUES (#{name}, " +
                "#{value}, #{parentId}, #{type}, #{level}, #{routerType}, #{routingKey}, #{available}, #{appId}, #{inner})")
    public void save(Node node);

    @Select("SELECT * FROM NODE ORDER BY level")
    public List<Node> findAll();

    @Select("SELECT * FROM NODE WHERE nodeId = #{id}")
    public Node find(int id);

    @Select("SELECT * FROM NODE WHERE name = #{name}")
    public Node findWithName(String name);

    @Select("SELECT * FROM NODE ORDER BY `type` LIMIT #{offset}, #{pageSize} ")
    public List<Node> findWithPaging(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Delete("DELETE FROM NODE WHERE nodeId = #{id}")
    public void delete(int id);

    @Select("SELECT COUNT(1) FROM NODE")
    public int countAll();

    @Update("UPDATE NODE SET name = #{name}, value = #{value}, type = #{type}, " +
                "level = #{level}, routerType = #{routerType}, routingKey = #{routingKey}, " +
                "available = #{available}, appId = #{appId}, `inner` = #{inner} " +
                " WHERE nodeId = #{nodeId}")
    public void update(Node node);

}
