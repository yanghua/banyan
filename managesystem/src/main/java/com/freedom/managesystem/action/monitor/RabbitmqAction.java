package com.freedom.managesystem.action.monitor;

import com.freedom.managesystem.service.IRabbitmqService;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RabbitmqAction extends ActionSupport {

    @Autowired
    private IRabbitmqService rabbitmqService;

    public String overview() throws IOException{
        String jsonStr = rabbitmqService.overview();
        HttpServletResponse response = ServletActionContext.getResponse();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);

        printWriter.flush();
        printWriter.close();

        return null;
    }

    public String nodelist() throws IOException {
        String jsonStr = rabbitmqService.nodelistOfcluster();
        HttpServletResponse response = ServletActionContext.getResponse();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);

        printWriter.flush();
        printWriter.close();

        return null;
    }


}
