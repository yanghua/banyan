package com.messagebus.managesystem.dao;

import com.messagebus.managesystem.dao.sqlprovider.NodeProvider;
import com.messagebus.business.model.Node;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface INodeMapper {

    @Insert("INSERT INTO NODE(name, value, parentId, type, level, routerType, routingKey, available, appId, `inner`) VALUES (#{name}, " +
        "#{value}, #{parentId}, #{type}, #{level}, #{routerType}, #{routingKey}, #{available}, #{appId}, #{inner})")
    public void create(Node node);

    @Select("SELECT * FROM NODE ORDER BY level")
    public List<Node> findAll();

    @Select("SELECT * FROM NODE WHERE nodeId = #{id}")
    public Node find(int id);

    @Select("SELECT * FROM NODE WHERE name = #{name}")
    public Node findWithName(String name);

    @Select("SELECT * FROM NODE ORDER BY `type` LIMIT #{offset}, #{pageSize} ")
    public List<Node> findWithPaging(@Param("offset") int offset,
                                     @Param("pageSize") int pageSize);

    @Select("SELECT * FROM NODE WHERE `type` = #{type} AND `inner` != 1  ORDER BY `type` LIMIT #{offset}, #{pageSize} ")
    public List<Node> findWithType(@Param("offset") int offset,
                                   @Param("pageSize") int pageSize,
                                   @Param("type") int type);

    @SelectProvider(type = NodeProvider.class, method = "queryQueue")
    public List<Node> findSpecialQueuesExcludeSelf(@Param("targetId") int targetId,
                                                   @Param("isPubsub") boolean isPubsub);

    @Delete("DELETE FROM NODE WHERE nodeId = #{id}")
    public void delete(String id);

    @Select("SELECT COUNT(1) FROM NODE")
    public int countAll();

    @Select("SELECT COUNT(1) FROM NODE WHERE `type` = 1 AND `inner` = 1 ")
    public int countAvailableQueues();

    @SelectProvider(type = NodeProvider.class, method = "countQueue")
    public int countSpecialAvailableQueues(@Param("targetId") int targetId,
                                           @Param("isPubsub") boolean isPubsub);

    @Update("UPDATE NODE SET name = #{name}, value = #{value}, type = #{type}, " +
        "level = #{level}, routerType = #{routerType}, routingKey = #{routingKey}, " +
        "available = #{available}, appId = #{appId}, `inner` = #{inner} " +
        " WHERE nodeId = #{nodeId}")
    public void update(Node node);

}
