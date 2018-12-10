package com.girish.spellchecker.impl;

import com.girish.spellchecker.SpellChecker;
import com.girish.spellchecker.hash.HashFunction;
import com.girish.spellchecker.hash.Murmur3HashFunction;
import com.girish.spellchecker.hash.type.HashN;

import java.util.BitSet;

public class BloomFilter implements SpellChecker {
    private static final double DEFAULT_FALSE_POSITIVE_PROBABILITY = 0.1;
    private static final int DEFAULT_EXPECTED_NUM_OF_INSERTED_ELEMENTS = 100000;
    private static final HashFunction HASH_FUNCTION = new Murmur3HashFunction();

    private final BitSet bitmap;

    private final double falsePositiveProbability;
    private final int expectedNumOfInsertedElements;
    private final int optimalNumOfHashFunctions;
    private final int optimalNumOfBits;

    public BloomFilter() {
        this(DEFAULT_FALSE_POSITIVE_PROBABILITY, DEFAULT_EXPECTED_NUM_OF_INSERTED_ELEMENTS);
    }

    public BloomFilter(final double falsePositiveProbability) {
        this(falsePositiveProbability, DEFAULT_EXPECTED_NUM_OF_INSERTED_ELEMENTS);
    }

    public BloomFilter(final int expectedNumOfInsertedElements) {
        this(DEFAULT_FALSE_POSITIVE_PROBABILITY, expectedNumOfInsertedElements);
    }

    public BloomFilter(final double falsePositiveProbability, final int expectedNumOfInsertedElements) {
        this.falsePositiveProbability = falsePositiveProbability;
        this.expectedNumOfInsertedElements = expectedNumOfInsertedElements;
        this.optimalNumOfBits = optimalNumOfBits(falsePositiveProbability, expectedNumOfInsertedElements);
        this.optimalNumOfHashFunctions = optimalNumOfHashFunctions(expectedNumOfInsertedElements, optimalNumOfBits);
        this.bitmap = new BitSet(optimalNumOfBits);
    }

    public void addToDictionary(String word) {
        validate(word);
        var hashes = generateHashes(word.getBytes());

        for (long hash : hashes.list()) {
            bitmap.set(bitIndex(hash));
        }
    }

    public boolean mightContains(String word) {
        validate(word);
        var hashes = generateHashes(word.getBytes());

        for (long hash : hashes.list()) {
            if (!bitmap.get(bitIndex(hash))) {
                return false;
            }
        }

        return true;
    }

    public double getFalsePositiveProbability() {
        return this.falsePositiveProbability;
    }

    private void validate(String word) {
        if (null == word) {
            throw new IllegalArgumentException("Input word cannot be null");
        }
    }

    private int bitIndex(final long hash) {
        return (int) (hash % bitmap.size());
    }

    private int optimalNumOfBits(final double falsePositiveProbability, final int numOfInsertedElements) {
        return (int) (-numOfInsertedElements * Math.log(falsePositiveProbability) / (Math.log(2) * Math.log(2)));
    }

    private int optimalNumOfHashFunctions(final int numOfInsertedElements, final int numOfBits) {
        return Math.max(1, (int) ((double) numOfBits / numOfInsertedElements * Math.log(2)));
    }

    private HashN generateHashes(byte[] data) {
        var hash = HASH_FUNCTION.hash128(data);
        var hashes = new HashN();

        // Generate k hashes from only 2 hashes
        for (var i = 1; i <= optimalNumOfHashFunctions; i++) {
            var combinedHash = hash.getFirst64Bits() + (i * hash.getLast64Bits());

            // sum of the hashes above may result in overflow.
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            hashes.add(combinedHash);
        }

        return hashes;
    }
}
