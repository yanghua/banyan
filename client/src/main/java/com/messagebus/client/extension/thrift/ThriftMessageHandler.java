package com.messagebus.client.extension.thrift;

import com.messagebus.client.IRpcMessageProcessor;
import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yanghua on 4/17/15.
 */
public class ThriftMessageHandler implements IRpcMessageProcessor {

    private static final Log logger = LogFactory.getLog(ThriftMessageHandler.class);

    private TProcessor       processor;
    private TProtocolFactory inProtocolFactory;
    private TProtocolFactory outProtocolFactory;

    public ThriftMessageHandler(TProcessor processor,
                                TProtocolFactory inProtocolFactory,
                                TProtocolFactory outProtocolFactory) {
        this.processor = processor;
        this.inProtocolFactory = inProtocolFactory;
        this.outProtocolFactory = outProtocolFactory;
    }

    @Override
    public byte[] onRpcMessage(byte[] inMsg) {
        InputStream  in          = new ByteArrayInputStream(inMsg);
        OutputStream out         = new ByteArrayOutputStream();
        TTransport   transport   = new TIOStreamTransport(in, out);
        TProtocol    inProtocol  = inProtocolFactory.getProtocol(transport);
        TProtocol    outProtocol = outProtocolFactory.getProtocol(transport);

        try {
            processor.process(inProtocol, outProtocol);
            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (TException e) {
            ExceptionHelper.logException(logger, e, "onRpcMessage");
            throw new RuntimeException(e);
        } finally {
            transport.close();
        }
    }
}
