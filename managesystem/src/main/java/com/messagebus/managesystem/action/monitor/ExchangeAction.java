package com.messagebus.managesystem.action.monitor;

import com.messagebus.managesystem.action.other.BaseAction;
import com.messagebus.managesystem.pojo.rabbitHTTP.Exchange;
import com.messagebus.managesystem.service.IExchangeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class ExchangeAction extends BaseAction {

    private static final Log logger = LogFactory.getLog(ExchangeAction.class);

    @Autowired
    private IExchangeService exchangeService;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "monitor/exchange");
        return "index";
    }

    public void list() {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        Exchange[] exchanges = exchangeService.list();

        String jsonArr = gson.toJson(exchanges);
        jsonArr = generateListSuccessJSONStr(jsonArr);
        responseJTableData(resp, jsonArr);
    }

    public void test() {
        HttpServletResponse response = ServletActionContext.getResponse();
        try (PrintWriter writer = response.getWriter();) {
            writer.print(exchangeService.list());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
