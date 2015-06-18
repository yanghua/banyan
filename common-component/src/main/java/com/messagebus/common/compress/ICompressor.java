package com.messagebus.common.compress;

import java.nio.charset.Charset;

/**
 * Created by yanghua on 6/18/15.
 */
public interface ICompressor {

    public byte[] compress(byte[] source);

    public byte[] uncompress(byte[] target);

    public byte[] compressString(String source);

    public byte[] compressString(String source, Charset charset);

    public String uncompressString(byte[] target);

    public String uncompressString(byte[] target, Charset charset);

    public byte[] compressStream(byte[] source);

    public byte[] uncompressStream(byte[] target);

}
