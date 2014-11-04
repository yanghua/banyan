package com.freedom.managesystem.action.monitor;

import com.freedom.managesystem.action.BaseAction;
import com.freedom.managesystem.pojo.rabbitHTTP.Queue;
import com.freedom.managesystem.service.IQueueService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * monitor for queue
 */
public class QueueAction extends BaseAction {

    @Autowired
    private IQueueService queueService;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "monitor/queue");
        return "index";
    }

    public void list() {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        Queue[] queues = queueService.list();

        String jsonArr = gson.toJson(queues);
        jsonArr = generateListSuccessJSONStr(jsonArr);
        responseJTableData(resp, jsonArr);
    }

}
