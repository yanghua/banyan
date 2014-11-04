package com.freedom.managesystem.service;

import com.freedom.messagebus.common.model.Node;

import java.sql.SQLException;
import java.util.List;

public interface INodeService {

    public void create(Node node) throws SQLException;

    public List<Node> getAll();

    public List<Node> getWithPaging(int offset, int pageSize);

    public Node get(int id);

    public void modify(Node node) throws SQLException;

    public void remove(int id) throws SQLException;

    public int countAll();

    public String generateNodeValue(Node node) throws IllegalStateException;

    public String generateRoutingKey(Node node) throws IllegalStateException;

}
