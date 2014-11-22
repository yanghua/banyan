package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.IReceivePermissionMapper;
import com.freedom.managesystem.service.IReceivePermissionService;
import com.freedom.managesystem.service.MessagebusService;
import com.freedom.messagebus.business.model.ReceivePermission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

@Service
public class ReceivePermissionImpl extends MessagebusService implements IReceivePermissionService {

    private static final Log logger = LogFactory.getLog(ReceivePermissionImpl.class);

    @Resource
    private IReceivePermissionMapper receivePermissionMapper;

    @Override
    public void save(ReceivePermission receivePermission) throws SQLException {
        receivePermissionMapper.create(receivePermission);
        produceDBOperate("INSERT", "RECEIVE_PERMISSION");
    }

    @Override
    public List<ReceivePermission> getAll() {
        return receivePermissionMapper.findAll();
    }

    @Override
    public List<ReceivePermission> getWithTargetId(int targetId) {
        return receivePermissionMapper.findWithTargetId(targetId);
    }

    @Override
    public void remove(int targetId, int grantId) throws SQLException {
        receivePermissionMapper.delete(targetId, grantId);
        produceDBOperate("DELETE", "RECEIVE_PERMISSION");
    }
}
