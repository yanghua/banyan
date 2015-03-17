package com.messagebus.business.exchanger;

public interface IDataExchanger {

    public void upload();

    public void upload(byte[] originalData);

    public Object download();

    public Object download(byte[] originalData);

}
