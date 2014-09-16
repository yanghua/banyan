package com.freedom.managesystem.service;

import com.freedom.messagebus.common.model.Node;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface INodeService {

    public void create(@NotNull Node node);

    @NotNull
    public List<Node> getAll();

    @NotNull
    public Node get(int id);

    public void remove(int id);

}
