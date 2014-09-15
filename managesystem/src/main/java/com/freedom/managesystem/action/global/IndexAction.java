package com.freedom.managesystem.action.global;

import com.freedom.managesystem.action.BaseAction;
import org.apache.struts2.ServletActionContext;

public class IndexAction extends BaseAction {

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "index");
        return "index";
    }

}
