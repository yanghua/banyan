package com.freedom.managesystem.action.monitor;

import com.freedom.managesystem.action.other.BaseAction;
import com.freedom.managesystem.pojo.rabbitHTTP.Channel;
import com.freedom.managesystem.service.IChannelService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ChannelAction extends BaseAction {

    private static final Log logger = LogFactory.getLog(ChannelAction.class);

    @Autowired
    private IChannelService channelService;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "monitor/channel");
        return "index";
    }

    public void list() {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        Channel[] channels = channelService.list();

        String jsonArr = gson.toJson(channels);
        jsonArr = generateListSuccessJSONStr(jsonArr);
        responseJTableData(resp, jsonArr);
    }

    public void test() {
        HttpServletResponse response = ServletActionContext.getResponse();
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print(channelService.list());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.flush();
            writer.close();
        }
    }

}
