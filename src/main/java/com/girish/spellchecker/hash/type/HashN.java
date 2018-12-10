package com.girish.spellchecker.hash.type;

import java.util.ArrayList;
import java.util.List;

public class HashN {
    private final List<Long> hashes;

    public HashN() {
        this.hashes = new ArrayList<Long>();
    }

    public void add(final long hash) {
        this.hashes.add(hash);
    }

    public List<Long> list() {
        return new ArrayList<>(this.hashes);
    }

    public int getN() {
        return this.hashes.size();
    }
}
