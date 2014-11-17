package com.freedom.messagebus.business.exchanger;

import java.io.IOException;
import java.io.Serializable;

public interface IZKDataExchanger {

    public void upload(Serializable obj) throws IOException;

    public Object download(byte[] originalData) throws IOException, ClassNotFoundException;

}
