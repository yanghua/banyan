package com.messagebus.client.extension.thrift;

import com.messagebus.client.Messagebus;
import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yanghua on 4/16/15.
 */
public class TAMQPClientTransport extends TTransport {

    private static final Log logger = LogFactory.getLog(TAMQPClientTransport.class);

    private Messagebus client;
    private String     secret;
    private String     target;
    private String     token;
    private long       timeout;

    private ByteArrayOutputStream reqMsgStream  = new ByteArrayOutputStream();
    private InputStream           respMsgStream = null;

    public TAMQPClientTransport(Messagebus client,
                                String secret,
                                String target,
                                String token,
                                long timeoutOfMilliSecond) {
        this.client = client;
        this.secret = secret;
        this.target = target;
        this.token = token;
        this.timeout = timeoutOfMilliSecond;
    }

    @Override
    public boolean isOpen() {
        return client.isOpen();
    }

    @Override
    public void open() throws TTransportException {

    }

    @Override
    public void close() {

    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws TTransportException {
        if (this.respMsgStream == null) {
            throw new TTransportException("Response buffer is empty, no request.");
        } else {
            try {
                int iox = this.respMsgStream.read(bytes, i, i1);
                if (iox == -1) {
                    throw new TTransportException("No more data available.");
                } else {
                    return iox;
                }
            } catch (IOException var5) {
                throw new TTransportException(var5);
            }
        }
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws TTransportException {
        this.reqMsgStream.write(bytes, i, i1);
    }

    public void flush() throws TTransportException {
        byte[] data = this.reqMsgStream.toByteArray();
        this.reqMsgStream.reset();

        byte[] responseData = new byte[0];
        try {
            responseData = this.client.primitiveRequest(secret, target, data, token, timeout);
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[flush]");
        }
        this.respMsgStream = new ByteArrayInputStream(responseData);
    }
}
