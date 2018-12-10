package com.girish.spellchecker.hash;

import com.girish.spellchecker.hash.exception.InvalidHashException;
import com.girish.spellchecker.hash.type.Hash128;
import org.apache.hive.common.util.Murmur3;

public class Murmur3HashFunction implements HashFunction {
    public Hash128 hash128(byte[] data) {
        var hash = getMurmur3Hash(data);

        try {
            return Hash128.of(hash[0], hash[1]);
        } catch (Exception e) {
            throw new InvalidHashException("An exception while calculating Murmur3 hash for the data: ", e);
        }
    }

    private long[] getMurmur3Hash(byte[] data) {
        return Murmur3.hash128(data);
    }
}
