package com.freedom.messagebus.common;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Desc: a class defined some util methods
 * User: yanghua
 * Date: 7/1/14
 * Time: 3:09 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class CommonUtil {

    /**
     * get a router's name
     *
     * @param appKey     the identifier of a app
     * @param routerType the type of router.
     * @return the generated router name
     */
    @NotNull
    @Deprecated
    public static String getRouterName(@NotNull String appKey, @NotNull RouterType routerType) {
        return String.format(CONSTS.ROUTER_NAME_PATTERN, appKey, routerType.toString());
    }

    @NotNull
    @Deprecated
    public static String getTransferCenterName(@NotNull String appKey, @NotNull String name) {
        return String.format(CONSTS.TRANSFER_CENTER_NAME_PATTERN, appKey, name);
    }

    @NotNull
    public static Byte[] bytesToBytes(@NotNull byte[] original) {
        if (original.length == 0)
            return CONSTS.EMPTY_BYTE_ARRAY;

        Byte[] target = new Byte[original.length];

        for (int i = 0; i < original.length; i++) {
            target[i] = original[i];
        }

        return target;
    }

    @NotNull
    public static byte[] BytesTobytes(@NotNull Byte[] original) {
        if (original.length == 0) {
            return CONSTS.EMPTY_PRIMITIVE_BYTE_ARRAY;
        }

        byte[] target = new byte[original.length];

        for (int i = 0; i < original.length; i++) {
            target[i] = original[i];
        }

        return target;
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("the file's content is too large");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
            && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

}
