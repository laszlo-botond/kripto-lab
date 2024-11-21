package edu.bbte.kripto.lbim2260.padding;

import javax.crypto.BadPaddingException;

public abstract class Padder {
    protected final int blockSize;

    public Padder(int blockSize) {
        this.blockSize = blockSize;
    }

    public abstract byte[] padByteArray(byte[] arr) throws BadPaddingException;

    public abstract byte[] removePadding(byte[] arr) throws BadPaddingException;
}
