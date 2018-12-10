package com.girish.spellchecker.hash.type;

public class Hash128 {
    private final long first64Bits;
    private final long last64Bits;

    private Hash128() {
        this(0, 0);
    }

    private Hash128(final long first64Bits, final long last64Bits) {
        this.first64Bits = first64Bits;
        this.last64Bits = last64Bits;
    }

    public static Hash128 of(final long first64Bits, final long last64Bits) {
        return new Hash128(first64Bits, last64Bits);
    }

    public long getFirst64Bits() {
        return this.first64Bits;
    }

    public long getLast64Bits() {
        return this.last64Bits;
    }
}
