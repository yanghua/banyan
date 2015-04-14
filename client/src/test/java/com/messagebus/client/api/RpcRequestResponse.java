package com.messagebus.client.api;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.core.BaseTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 4/8/15.
 */
public class RpcRequestResponse extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(RpcRequestResponse.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleRpc() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String secret = "mshdfjbqwejhfgasdfbjqkygaksdfa";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
                final Messagebus client = singlePool.getResource();

                client.callback(secret, TestInterface.class, new HelloServiceProvider(), 10, TimeUnit.SECONDS);

                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        String secret = "kliwhiduhaiucvarkjajksdbfkjabw";
        String targetQueue = "emapDemoRpcResponse";
        String token = "klasehnfkljashdnflhkjahwlekdjf";
        String methodName = "sayHello";
        Object responseObj = client.call(secret, targetQueue, methodName, new Object[0], token, 10000);

        assertNull(responseObj);

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    public void testReturnValueRpc() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String secret = "mshdfjbqwejhfgasdfbjqkygaksdfa";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
                final Messagebus client = singlePool.getResource();

                client.callback(secret, TestInterface.class, new HelloServiceProvider(), 10, TimeUnit.SECONDS);

                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        String secret = "kliwhiduhaiucvarkjajksdbfkjabw";
        String targetQueue = "emapDemoRpcResponse";
        String token = "klasehnfkljashdnflhkjahwlekdjf";
        String methodName = "returnValueMethod";
        Object responseObj = client.call(secret, targetQueue, methodName, new Object[0], token, 10000);

        assertNotNull(responseObj);
        assertEquals("hello world", responseObj.toString());

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    public void testParamNoReturnValueRpc() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String secret = "mshdfjbqwejhfgasdfbjqkygaksdfa";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
                final Messagebus client = singlePool.getResource();

                client.callback(secret, TestInterface.class, new HelloServiceProvider(), 10, TimeUnit.SECONDS);

                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        String secret = "kliwhiduhaiucvarkjajksdbfkjabw";
        String targetQueue = "emapDemoRpcResponse";
        String token = "klasehnfkljashdnflhkjahwlekdjf";
        String methodName = "printParam";
        Object responseObj = client.call(secret, targetQueue, methodName, new Object[]{"hello world"}, token, 10000);

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    public void testReturnParamValueRpc() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String secret = "mshdfjbqwejhfgasdfbjqkygaksdfa";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
                final Messagebus client = singlePool.getResource();

                client.callback(secret, TestInterface.class, new HelloServiceProvider(), 10, TimeUnit.SECONDS);

                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        String secret = "kliwhiduhaiucvarkjajksdbfkjabw";
        String targetQueue = "emapDemoRpcResponse";
        String token = "klasehnfkljashdnflhkjahwlekdjf";
        String methodName = "returnParam";
        Object responseObj = client.call(secret, targetQueue, methodName, new Object[]{"hello world"}, token, 10000);

        assertNotNull(responseObj);
        assertEquals("hello world", responseObj.toString());

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    public static interface TestInterface {

        public void sayHello();

        public String returnValueMethod();

        public void printParam(String arg1);

        public String returnParam(String arg1);

    }

    public static class HelloServiceProvider implements TestInterface {

        @Override
        public void sayHello() {
            logger.info("hello...");
        }

        @Override
        public String returnValueMethod() {
            return "hello world";
        }

        @Override
        public void printParam(String arg1) {
            System.out.println(arg1);
        }

        @Override
        public String returnParam(String arg1) {
            return arg1;
        }
    }
}
