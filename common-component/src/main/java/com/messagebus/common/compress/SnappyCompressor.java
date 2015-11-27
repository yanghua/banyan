package com.messagebus.common.compress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.*;
import java.nio.charset.Charset;

/**
 * snappy compressor
 * more detail : https://github.com/xerial/snappy-java
 */
class SnappyCompressor implements ICompressor {

    private static final Log logger = LogFactory.getLog(SnappyCompressor.class);

    public byte[] compress(byte[] source) {
        try {
            return Snappy.compress(source);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public byte[] uncompress(byte[] target) {
        try {
            return Snappy.uncompress(target);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public byte[] compressString(String source) {
        try {
            return Snappy.compress(source);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public byte[] compressString(String source, Charset charset) {
        try {
            return Snappy.compress(source, charset);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public String uncompressString(byte[] target) {
        try {
            return Snappy.uncompressString(target);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public String uncompressString(byte[] target, Charset charset) {
        try {
            return Snappy.uncompressString(target, charset);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public byte[] compressStream(byte[] source) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream stream = new SnappyOutputStream(baos);
        byte[] compressed;
        try {
            stream.write(source);
            stream.flush();

            compressed = baos.toByteArray();
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {

            }
        }

        return compressed;
    }

    public byte[] uncompressStream(byte[] target) {
        ByteArrayOutputStream baos = null;
        InputStream inputStream = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(target);
            inputStream = new SnappyInputStream(bais);

            byte[] buf = new byte[1024];
            int num = -1;
            baos = new ByteArrayOutputStream();
            while ((num = inputStream.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {

            }
        }
    }

}
