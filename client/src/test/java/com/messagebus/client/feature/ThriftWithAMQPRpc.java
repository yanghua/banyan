package com.messagebus.client.feature;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.WrappedRpcServer;
import com.messagebus.client.core.BaseTestCase;
import com.messagebus.client.extension.thrift.TAMQPClientTransport;
import com.messagebus.client.extension.thrift.ThriftMessageHandler;
import com.messagebus.common.TestVariableInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * Created by yanghua on 4/17/15.
 */
public class ThriftWithAMQPRpc extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(ThriftWithAMQPRpc.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testThriftRpc() throws Exception {
        TTransport transport = new TAMQPClientTransport(this.client,
                "kliwhiduhaiucvarkjajksdbfkjabw",
                "emapDemoRpcResponse",
                "klasehnfkljashdnflhkjahwlekdjf",
                10000);
        transport.open();
        TProtocol          protocol = new TJSONProtocol(transport);
        CalcService.Client client   = new CalcService.Client(protocol);
        int                result   = client.calcSum();
        logger.info(result);
        transport.close();
    }

    public static void main(String[] args) {
        String zkConnectionStr = TestVariableInfo.ZK_CONNECTION_STRING;

        MessagebusSinglePool singlePool = new MessagebusSinglePool(zkConnectionStr);

        Messagebus client = singlePool.getResource();

        //server code
        WrappedRpcServer rpcServer = null;
        try {
            TProcessor       processor          = new CalcService.Processor(new CalcServiceImpl());
            TProtocolFactory inProtocolFactory  = new TJSONProtocol.Factory();
            TProtocolFactory outProtocolFactory = new TJSONProtocol.Factory();
            rpcServer = client.buildRpcServer("mshdfjbqwejhfgasdfbjqkygaksdfa",
                    new ThriftMessageHandler(processor, inProtocolFactory, outProtocolFactory));

            rpcServer.mainLoop();
        } finally {
            rpcServer.close();
            singlePool.returnResource(client);
            singlePool.destroy();
        }
    }
}
