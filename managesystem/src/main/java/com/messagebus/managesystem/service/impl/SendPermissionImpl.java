package com.messagebus.managesystem.service.impl;

import com.messagebus.managesystem.dao.ISendPermissionMapper;
import com.messagebus.managesystem.service.ISendPermissionService;
import com.messagebus.managesystem.service.MessagebusService;
import com.messagebus.business.model.SendPermission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

@Service
public class SendPermissionImpl extends MessagebusService implements ISendPermissionService {

    private static final Log logger = LogFactory.getLog(SendPermissionImpl.class);

    @Resource
    private ISendPermissionMapper sendPermissionMapper;

    @Override
    public void save(SendPermission sendPermission) throws SQLException {
        sendPermissionMapper.create(sendPermission);
        produceDBOperate("INSERT", "SEND_PERMISSION");
    }

    @Override
    public List<SendPermission> getAll() {
        return sendPermissionMapper.findAll();
    }

    @Override
    public List<SendPermission> getWithTargetId(int targetId) {
        return sendPermissionMapper.findWithTargetId(targetId);
    }

    @Override
    public void remove(int targetId, int grantId) throws SQLException {
        sendPermissionMapper.delete(targetId, grantId);
        produceDBOperate("DELETE", "SEND_PERMISSION");
    }
}