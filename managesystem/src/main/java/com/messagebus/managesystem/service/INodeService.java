package com.messagebus.managesystem.service;

import com.messagebus.business.model.Node;

import java.sql.SQLException;
import java.util.List;

public interface INodeService {

    public void save(Node node) throws SQLException;

    public List<Node> getAll();

    public List<Node> getWithPaging(int offset, int pageSize);

    public Node get(int id);

    public List<Node> getWithType(int type, int offset, int pageSize);

    public List<Node> getQueues(int nodeId, boolean isPubsub);

    public void modify(Node node) throws SQLException;

    public void remove(int id) throws SQLException;

    public int countAll();

    public int countAvailableQueues();

    public int countSpecialAvailableQueues(int targetId, boolean isPubsub);

    public String generateNodeValue(Node node) throws IllegalStateException;

    public String generateRoutingKey(Node node) throws IllegalStateException;

    public void activate(int nodeId) throws SQLException;

    public void unactivate(int nodeId) throws SQLException;

    public String resetAppId(int nodeId) throws SQLException;
}
