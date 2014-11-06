package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.IAuthorizationMapper;
import com.freedom.managesystem.pojo.Authorization;
import com.freedom.managesystem.service.IAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AuthorizationServiceImpl implements IAuthorizationService {

//    @Autowired
//    private IAuthorizationMapper authorizationMapper;
//
//    @Override
//    public void auth(int nodeId) throws SQLException {
//        Authorization authorization = new Authorization();
//        authorization.setNodeId(nodeId);
//
//        //generate app id
//        authorization.setAppId();
//        authorizationMapper.save(authorization);
//    }
//
//    @Override
//    public void unAuth(int nodeId) throws SQLException {
//        authorizationMapper.delete(nodeId);
//    }
}
