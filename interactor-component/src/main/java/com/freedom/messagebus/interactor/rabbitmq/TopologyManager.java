package com.freedom.messagebus.interactor.rabbitmq;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.TreeSet;

public class TopologyManager extends AbstractInitializer {

    private static final Log logger = LogFactory.getLog(TopologyManager.class);

    private static volatile TopologyManager instance;

    private TopologyManager(String host) {
        super(host);
    }

    public static TopologyManager defaultManager(String host) {
        if (instance == null) {
            synchronized (TopologyManager.class) {
                if (instance == null) {
                    instance=new TopologyManager(host);
                }
            }
        }

        return instance;
    }

    public void init(TreeSet<Node> nodes) {

    }

    public void destroy() {

    }

    public void restart() {

    }

}
