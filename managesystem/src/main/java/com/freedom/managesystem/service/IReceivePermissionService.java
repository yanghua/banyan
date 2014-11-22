package com.freedom.managesystem.service;

import com.freedom.messagebus.business.model.ReceivePermission;

import java.sql.SQLException;
import java.util.List;

public interface IReceivePermissionService {

    public void save(ReceivePermission receivePermission) throws SQLException;

    public List<ReceivePermission> getAll();

    public List<ReceivePermission> getWithTargetId(int targetId);

    public void remove(int targetId, int grantId) throws SQLException;

}
