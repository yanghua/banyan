package com.freedom.managesystem.service;

import com.freedom.messagebus.business.model.SendPermission;

import java.sql.SQLException;
import java.util.List;

public interface ISendPermissionService {

    public void save(SendPermission sendPermission) throws SQLException;

    public List<SendPermission> getAll();

    public List<SendPermission> getWithTargetId(int targetId);

    public void remove(int targetId, int grantId) throws SQLException;

}
