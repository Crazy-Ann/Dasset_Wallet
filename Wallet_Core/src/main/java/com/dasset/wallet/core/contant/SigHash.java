package com.dasset.wallet.core.contant;

public enum SigHash {

    ALL(0),         // 1
    NONE(1),       // 2
    SINGLE(2),     // 3
    BCCFORK(1 | 0x40 | 0),  // 65
    BTGFORK(1 | 0x40 | (79 << 8)),
    BTWFORK(1 | 0x40 | 87 << 8),
    BTFFORK(1 | 0x40 | 70 << 8),
    BTPFORK(1 | 0x40 | 80 << 8),
    BTNFORK(1 | 0x40 | 88 << 8),
    SBTCFORK(1 | 0x40);  // 65
    
    public int value;

    private SigHash(int value) {
        this.value = value;
    }

}
