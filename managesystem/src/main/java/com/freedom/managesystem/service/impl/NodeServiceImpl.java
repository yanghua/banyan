package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.INodeMapper;
import com.freedom.managesystem.pojo.Node;
import com.freedom.managesystem.service.INodeService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class NodeServiceImpl implements INodeService {

    @Resource
    private INodeMapper nodeMapper;

    @Override
    public void create(@NotNull Node node) {
        nodeMapper.save(node);
    }

    @NotNull
    @Override
    public List<Node> getAll() {
        return nodeMapper.findAll();
    }

    @NotNull
    @Override
    public Node get(int id) {
        return nodeMapper.find(id);
    }

    @Override
    public void remove(int id) {
        nodeMapper.delete(id);
    }
}
