package com.freedom.messagebus.business.exchanger;

import java.io.IOException;
import java.io.Serializable;

public interface IDataExchanger {

    public void upload() throws IOException;

    public void upload(Serializable obj) throws IOException;

    public Object download() throws IOException;

    public Object download(byte[] originalData) throws IOException;

}
