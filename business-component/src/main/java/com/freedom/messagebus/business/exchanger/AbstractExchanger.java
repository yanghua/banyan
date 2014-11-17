package com.freedom.messagebus.business.exchanger;

import java.io.*;

abstract class AbstractExchanger implements IZKDataExchanger {

    protected Object object;
    protected byte[] serializedData;

    public void upload(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();

        byte[] bytes = baos.toByteArray();
        baos.close();
        serializedData = bytes;
    }

    public Object download(byte[] originalData) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(originalData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        bais.close();
        ois.close();

        return obj;
    }


}
