package com.freedom.messagebus.business.exchanger;

import java.io.IOException;

public interface IDataExchanger {

    public void upload() throws IOException;

    public void upload(byte[] originalData) throws IOException;

    public Object download() throws IOException;

    public Object download(byte[] originalData) throws IOException;

}
