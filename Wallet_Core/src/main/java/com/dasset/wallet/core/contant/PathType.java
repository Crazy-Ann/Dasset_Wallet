package com.dasset.wallet.core.contant;

public enum PathType {

    EXTERNAL_ROOT_PATH(0), INTERNAL_ROOT_PATH(1);

    private int type;

    PathType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
