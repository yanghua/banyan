package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.Benchmark;
import com.messagebus.benchmark.client.IFetcher;
import com.messagebus.benchmark.client.ILifeCycle;
import com.messagebus.benchmark.client.TestConfigConstant;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.extension.thrift.TAMQPClientTransport;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.common.TestVariableInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * Created by yanghua on 4/17/15.
 */
public class MessagebusJSONRpcTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(MessagebusJSONRpcTestCase.class);

    public static class OriginalJSONRpc implements Runnable, ILifeCycle, IFetcher {

        private MessagebusSinglePool singlePool;
        private Messagebus           client;
        private boolean flag    = true;
        private long    counter = 0;
        private Thread currentThread;

        String secret      = "kliwhiduhaiucvarkjajksdbfkjabw";
        String targetQueue = "emapDemoRpcResponse";
        String token       = "klasehnfkljashdnflhkjahwlekdjf";
        String methodName  = "calcSum";

        public OriginalJSONRpc() {
            singlePool = new MessagebusSinglePool(TestVariableInfo.PUBSUBER_HOST, TestVariableInfo.PUBSUBER_PORT);
            client = singlePool.getResource();
            currentThread = new Thread(this);
            currentThread.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (flag) {
                    Object responseObj = client.call(secret, targetQueue, methodName, new Object[0], token, 10000);
                    if (Integer.parseInt(responseObj.toString()) == 5050) ++counter;
                }
            } catch (MessagebusUnOpenException e) {
                ExceptionHelper.logException(logger, e, "[JSONRpc#run]");
            } finally {
                singlePool.returnResource(client);
                singlePool.destroy();
            }
        }

        @Override
        public void start() {
            this.currentThread.start();
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
            this.flag = false;
        }

        @Override
        public long fetch() {
            return this.counter;
        }
    }

    public static class ThriftJSONRpc implements Runnable, ILifeCycle, IFetcher {

        private MessagebusSinglePool singlePool;
        private Messagebus           client;
        private Thread               currentThread;
        private TTransport           transport;
        private boolean flag    = true;
        private long    counter = 0;

        String secret      = "kliwhiduhaiucvarkjajksdbfkjabw";
        String targetQueue = "emapDemoRpcResponse";
        String token       = "klasehnfkljashdnflhkjahwlekdjf";
        String methodName  = "calcSum";

        public ThriftJSONRpc() {
            singlePool = new MessagebusSinglePool(TestVariableInfo.PUBSUBER_HOST, TestVariableInfo.PUBSUBER_PORT);
            client = singlePool.getResource();
            currentThread = new Thread(this);
            currentThread.setDaemon(true);
        }

        @Override
        public long fetch() {
            return this.counter;
        }

        @Override
        public void start() {
            this.currentThread.start();
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
            this.flag = false;
        }

        @Override
        public void run() {
            //client code
            transport = new TAMQPClientTransport(this.client,
                                                 "kliwhiduhaiucvarkjajksdbfkjabw",
                                                 "emapDemoRpcResponse",
                                                 "klasehnfkljashdnflhkjahwlekdjf",
                                                 10000);
            int result = 0;
            try {
                transport.open();
                TProtocol protocol = new TJSONProtocol(transport);
                CalcService.Client client = new CalcService.Client(protocol);
                while (flag) {
                    result = client.calcSum();
                    if (result == 5050) ++counter;
                }
            } catch (TException e) {
                e.printStackTrace();
            } finally {
                transport.close();
                singlePool.returnResource(client);
                singlePool.destroy();
            }
        }
    }

    public static void main(String[] args) {
//        MessagebusJSONRpcTestCase testCase = new MessagebusJSONRpcTestCase();
//        Runnable task = new OriginalJSONRpc();
//
//        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
//                      TestConfigConstant.FETCH_NUM, "messagebus_original_json_rpc_noHA");

        MessagebusJSONRpcTestCase testCase = new MessagebusJSONRpcTestCase();
        Runnable task = new ThriftJSONRpc();

        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
                      TestConfigConstant.FETCH_NUM, "messagebus_thrift_json_rpc_noHA");
    }

}
