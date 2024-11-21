package edu.bbte.kripto.lbim2260.padding.impl;

import edu.bbte.kripto.lbim2260.padding.Padder;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import java.io.IOException;

@Slf4j
public class ZeroPadder extends Padder {
    public ZeroPadder(int blockSize) {
        super(blockSize);
    }

    @Override
    public byte[] padByteArray(byte[] arr) throws BadPaddingException {
        try {
            int paddingLength = blockSize - (arr.length % blockSize);
            byte[] padding = new byte[paddingLength];
            return ArrayUtils.concatBytes(arr, padding);
        } catch (IOException e) {
            throw new BadPaddingException();
        }
    }

    @Override
    public byte[] removePadding(byte[] arr) throws BadPaddingException {
        try {
            int index = arr.length - 1;
            while (arr[index] == (byte) 0b00000000) {
                index--;
            }
            return ArrayUtils.subArray(arr, 0, index + 1);
        } catch (Exception e) {
            throw new BadPaddingException();
        }
    }
}
