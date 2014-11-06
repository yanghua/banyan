package com.freedom.managesystem.action.other;

import com.freedom.managesystem.pojo.Module;
import com.freedom.managesystem.service.IModuleService;
import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class BaseAction extends ActionSupport {

    @Autowired
    private IModuleService moduleService;

    protected static final Gson gson = new Gson();

    protected String generateListSuccessJSONStr(String recordsJsonStr) {
        return "{\"Result\":\"OK\",\"Records\":" + recordsJsonStr + "}";
    }

    protected String generateListSuccessJSONStr(String recordsJsonStr, String otherParams) {
        return "{\"Result\":\"OK\",\"Records\":" + recordsJsonStr + ", \"Others\":" + otherParams + "}";
    }

    protected String generateListWithPagingSuccessJSONStr(String recordsJsonStr, int totalNum) {
        return "{\"Result\":\"OK\",\"Records\":" + recordsJsonStr + ", \"TotalRecordCount\":" + totalNum + "}";
    }

    protected String generateListWithPagingSuccessJSONStr(String recordsJsonStr, int totalNum, String otherParams) {
        return "{\"Result\":\"OK\",\"Records\":" + recordsJsonStr
            + ", \"TotalRecordCount\":" + totalNum
            + ", \"Others\":" + otherParams + "}";
    }

    protected String generateListWithOptionSuccessJSONStr(String recordsJsonStr) {
        return "{\"Result\":\"OK\",\"Options\":" + recordsJsonStr + "}";
    }

    protected String generateCreateSuccessJSONStr(String recordsJsonStr) {
        return "{\"Result\":\"OK\",\"Record\":\"" + recordsJsonStr + "\"}";
    }

    protected String generateUpdateSuccessJSONStr() {
        return "{\"Result\":\"OK\"}";
    }

    protected String generateErrorJSONStr(String errorMsgStr) {
        return "{\"Result\":\"ERROR\",\"Message\":\"" + errorMsgStr + "\"}";
    }

    protected void responseJTableData(HttpServletResponse resp, String data) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        try {
            resp.getWriter().print(data);
        } catch (Exception e) {
            String error = generateErrorJSONStr(e.getMessage());
            try {
                resp.getWriter().print(error);
            } catch (IOException ex) {

            }
        }
    }

    protected String index() {
        HttpSession session = ServletActionContext.getRequest().getSession();

        if (session.getAttribute("firstLevelModules") == null
            || session.getAttribute("secondLevelModules") == null)
            this.init(session);

        return "index";
    }

    protected void refreshMenu(HttpSession session) {
        //first level
        List<Module> modules = moduleService.getParentModule();
        session.setAttribute("firstLevelModules", modules);
        //second level
        List<Module> subModules = moduleService.getAllSubModules();
        session.setAttribute("secondLevelModules", subModules);
    }

    private void init(HttpSession session) {
        refreshMenu(session);
    }


}
