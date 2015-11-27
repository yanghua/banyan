package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.Benchmark;
import com.messagebus.benchmark.client.IFetcher;
import com.messagebus.benchmark.client.ILifeCycle;
import com.messagebus.benchmark.client.TestConfigConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Created by yanghua on 4/17/15.
 */
public class ThriftJSONRpcTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ThriftJSONRpcTestCase.class);

    public static class JSONRpc implements Runnable, ILifeCycle, IFetcher {

        private Thread currentThread;
        private long    counter = 0;
        private boolean flag    = true;

        public JSONRpc() {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(true);
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
            TTransport transport = new TSocket("172.16.206.29", 7911);
            int result = 0;

            try {
                transport.open();
                TProtocol protocol = new TJSONProtocol(transport);
                CalcService.Client client = new CalcService.Client(protocol);
                while (flag) {
                    result = client.calcSum();
                    if (result == 5050) ++counter;
                }
                logger.info(result);
            } catch (TException e) {
                e.printStackTrace();
            } finally {
                transport.close();
            }
        }
    }

    public static class BinaryRpc implements Runnable, ILifeCycle, IFetcher {

        private Thread currentThread;
        private long    counter = 0;
        private boolean flag    = true;

        public BinaryRpc() {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(true);
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
            TTransport transport = new TSocket("172.16.206.29", 7911);
            int result = 0;
            try {
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                CalcService.Client client = new CalcService.Client(protocol);

                while (flag) {
                    result = client.calcSum();
                    if (result == 5050) ++counter;
                }
            } catch (TException e) {
                e.printStackTrace();
            } finally {
                transport.close();
            }
        }
    }

    public static void main(String[] args) {
//        ThriftJSONRpcTestCase testCase = new ThriftJSONRpcTestCase();
//
//        Runnable task = new JSONRpc();
//        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
//                      TestConfigConstant.FETCH_NUM, "thrift_original_json_rpc");

        ThriftJSONRpcTestCase testCase = new ThriftJSONRpcTestCase();

        Runnable task = new BinaryRpc();
        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
                      TestConfigConstant.FETCH_NUM, "thrift_original_binary_rpc");
    }
}
