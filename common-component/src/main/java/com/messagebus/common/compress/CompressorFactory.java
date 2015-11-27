package com.messagebus.common.compress;

/**
 * Created by yanghua on 6/18/15.
 */
public final class CompressorFactory {

    public static ICompressor createCompressor(String algorName) {
        CompressEnum compressEnum = CompressEnum.lookup(algorName);

        switch (compressEnum) {

            case SNAPPY:
                return new SnappyCompressor();

            case LZF:
                return new LZFCompressor();

            default:
                throw new UnsupportedOperationException("unsupported compress mode : " + compressEnum.name());
        }

    }

}
