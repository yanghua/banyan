package com.freedom.managesystem.action.permission;

import com.freedom.managesystem.action.other.BaseAction;
import com.freedom.managesystem.action.other.DropdownlistModel;
import com.freedom.managesystem.pojo.Module;
import com.freedom.managesystem.service.IModuleService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ModuleAction extends BaseAction {

    @Autowired
    private IModuleService moduleService;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "permission/module");
        return "index";
    }

    public void list() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        int offset, pageSize;
        List<Module> moduleList = null;

        try {
            offset = Integer.parseInt(req.getParameter("jtStartIndex"));
            pageSize = Integer.parseInt(req.getParameter("jtPageSize"));

            moduleList = moduleService.getWithPaging(offset, pageSize);
            String jsonArr = gson.toJson(moduleList);
            jsonArr = generateListWithPagingSuccessJSONStr(jsonArr, moduleService.countAll());
            responseJTableData(resp, jsonArr);
        } catch (NumberFormatException e) {
            moduleList = moduleService.getAll();
            String jsonArr = gson.toJson(moduleList);
            jsonArr = generateListSuccessJSONStr(jsonArr);
            responseJTableData(resp, jsonArr);
        }
    }

    public void create() throws IOException {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        Module newModule = new Module();
        newModule.setModuleName(request.getParameter("moduleName"));
        newModule.setModuleValue(request.getParameter("moduleValue"));
        newModule.setLinkUrl(request.getParameter("linkUrl"));
        newModule.setParentModule(request.getParameter("parentModule"));

        String sortIndexStr = request.getParameter("sortIndex");
        if (sortIndexStr == null || sortIndexStr.isEmpty())
            sortIndexStr = "0";

        newModule.setSortIndex(Integer.parseInt(sortIndexStr));

        Module existsModule = moduleService.getWithModuleValue(newModule.getModuleValue());
        if (existsModule == null) {
            moduleService.create(newModule);

            existsModule = moduleService.getWithModuleValue(newModule.getModuleValue());

            String jsonStr = gson.toJson(existsModule);
            jsonStr = generateCreateSuccessJSONStr(jsonStr);

            super.refreshMenu(ServletActionContext.getRequest().getSession());
            responseJTableData(resp, jsonStr);
        } else {
            responseJTableData(resp, generateErrorJSONStr("the module has exists."));
        }
    }

    public void update() throws IOException {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        Module module = new Module();
        module.setModuleCode(request.getParameter("moduleCode"));
        module.setModuleName(request.getParameter("moduleName"));
        module.setModuleValue(request.getParameter("moduleValue"));
        module.setLinkUrl(request.getParameter("linkUrl"));
        module.setParentModule(request.getParameter("parentModule"));

        String sortIndexStr = request.getParameter("sortIndex");
        if (sortIndexStr == null || sortIndexStr.isEmpty())
            sortIndexStr = "0";

        module.setSortIndex(Integer.parseInt(sortIndexStr));
        moduleService.modify(module);
        super.refreshMenu(ServletActionContext.getRequest().getSession());
        responseJTableData(resp, generateUpdateSuccessJSONStr());
    }

    public void delete() throws IOException {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        String moduleCode = request.getParameter("moduleCode");
        if (moduleCode == null || moduleCode.isEmpty())
            throw new IOException("moduleCode can not be null or empty");

        moduleService.remove(moduleCode);
        responseJTableData(resp, generateUpdateSuccessJSONStr());
    }

    public void parentModuleInfo() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        List<Module> moduleList = moduleService.getParentModule();
        DropdownlistModel[] parentModuleList = new DropdownlistModel[moduleList.size()];

        for (int i = 0; i < moduleList.size(); i++) {
            DropdownlistModel parentModule = new DropdownlistModel();
            parentModule.setValue(moduleList.get(i).getModuleCode());
            parentModule.setDisplayText(moduleList.get(i).getModuleName());

            parentModuleList[i] = parentModule;
        }

        String jsonArr = gson.toJson(parentModuleList);
        jsonArr = generateListWithOptionSuccessJSONStr(jsonArr);
        responseJTableData(resp, jsonArr);
    }

}
