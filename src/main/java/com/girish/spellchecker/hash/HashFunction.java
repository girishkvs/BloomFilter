package com.girish.spellchecker.hash;

import com.girish.spellchecker.hash.type.Hash128;

public interface HashFunction {
    Hash128 hash128(byte[] data);
}
