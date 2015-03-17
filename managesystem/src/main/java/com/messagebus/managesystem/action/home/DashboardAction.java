package com.messagebus.managesystem.action.home;

import com.messagebus.managesystem.action.other.BaseAction;
import org.apache.struts2.ServletActionContext;

public class DashboardAction extends BaseAction {

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "index");
        return "index";
    }

}
