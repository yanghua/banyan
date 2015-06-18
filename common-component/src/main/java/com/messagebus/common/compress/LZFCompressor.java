package com.messagebus.common.compress;

import com.ning.compress.lzf.*;

import java.io.*;
import java.nio.charset.Charset;

/**
 * LZF compressor
 * more detail : https://github.com/ning/compress
 */
 class LZFCompressor implements ICompressor {

    public byte[] compress(byte[] source) {
        return LZFEncoder.encode(source);
    }

    public byte[] uncompress(byte[] target) {
        try {
            return LZFDecoder.decode(target);
        } catch (LZFException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] compressString(String source) {
        byte[] strBytes = source.getBytes();
        return LZFEncoder.encode(strBytes);
    }

    public byte[] compressString(String source, Charset charset) {
        byte[] strBytes = source.getBytes(charset);
        return LZFEncoder.encode(strBytes);
    }

    public String uncompressString(byte[] target) {
        try {
            byte[] sourceBytes = LZFDecoder.decode(target);
            return new String(sourceBytes);
        } catch (LZFException e) {
            throw new RuntimeException(e);
        }
    }

    public String uncompressString(byte[] target, Charset charset) {
        try {
            byte[] sourceBytes = LZFDecoder.decode(target);
            return new String(sourceBytes, charset);
        } catch (LZFException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] compressStream(byte[] source) {
        OutputStream compressStream = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressStream = new LZFOutputStream(baos);
            compressStream.write(source);
            compressStream.flush();

            byte[] compressed = baos.toByteArray();
            return compressed;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (compressStream != null) {
                    compressStream.close();
                }
            } catch (IOException e) {

            }
        }
    }

    public byte[] uncompressStream(byte[] target) {
        byte[] bytes = null;
        InputStream uncompressedStream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(target);
            uncompressedStream = new LZFCompressingInputStream(bais);

            int num = -1;
            byte[] buf = new byte[1024];
            baos = new ByteArrayOutputStream();

            while ((num = uncompressedStream.read(buf, 0, buf.length)) != -1) {
                baos.write(bytes, 0, num);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (uncompressedStream != null) {
                    uncompressedStream.close();
                }

                baos.close();
            } catch (IOException e) {

            }
        }
    }

}
