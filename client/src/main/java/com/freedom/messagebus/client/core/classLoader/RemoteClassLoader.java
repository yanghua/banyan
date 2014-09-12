package com.freedom.messagebus.client.core.classLoader;

/**
 * User: yanghua
 * Date: 7/31/14
 * Time: 9:53 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class RemoteClassLoader extends ClassLoader {

    private byte[] classData;

    public RemoteClassLoader(byte[] classData) {
        this.classData = classData;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.defineClass(name, this.classData, 0, classData.length);
    }
}
