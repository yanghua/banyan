package com.freedom.managesystem.dao;

import com.freedom.messagebus.common.model.Node;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface INodeMapper {

    @Insert("INSERT INTO NODE(name, value, parentId, type, level, routerType, routingKey) VALUES (#{name}, " +
                "#{value}, #{parentId}, #{type}, #{level}, #{routerType}, #{routingKey})")
    public void save(Node node);

    @Select("SELECT * FROM NODE ORDER BY level")
    public List<Node> findAll();

    @Select("SELECT * FROM NODE WHERE generatedId = #{id}")
    public Node find(int id);

    @Select("SELECT * FROM NODE WHERE name = #{name}")
    public Node findWithName(String name);

    @Select("SELECT * FROM NODE ORDER BY `type` LIMIT #{offset}, #{pageSize} ")
    public List<Node> findWithPaging(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Delete("DELETE FROM NODE WHERE generatedId = #{id}")
    public void delete(int id);

    @Select("SELECT COUNT(1) FROM NODE")
    public int countAll();

    @Select("UPDATE NODE SET name = #{name}, value = #{value}, type = #{type}, " +
                "level = #{level}, routerType = #{routerType}, routingKey = #{routingKey}" +
                " WHERE generatedId = #{generatedId}")
    public void update(Node node);

}
