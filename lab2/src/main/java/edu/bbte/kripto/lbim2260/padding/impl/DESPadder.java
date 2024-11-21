package edu.bbte.kripto.lbim2260.padding.impl;

import edu.bbte.kripto.lbim2260.padding.Padder;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import java.io.IOException;

@Slf4j
public class DESPadder extends Padder {
    public DESPadder(int blockSize) {
        super(blockSize);
    }

    @Override
    public byte[] padByteArray(byte[] arr) throws BadPaddingException {
        try {
            int toAppend = blockSize - (arr.length % blockSize);
            byte firstByte = (byte) 0b10000000;
            byte[] tmp = ArrayUtils.concatBytes(arr, new byte[]{firstByte});
            toAppend--;
            return ArrayUtils.concatBytes(tmp, new byte[toAppend]);
        } catch (IOException e) {
            throw new BadPaddingException();
        }
    }

    @Override
    public byte[] removePadding(byte[] arr) throws BadPaddingException {
        try {
            int index = arr.length - 1;
            while (arr[index] != (byte) 0b10000000) {
                index--;
            }
            return ArrayUtils.subArray(arr, 0, index);
        } catch (Exception e) {
            throw new BadPaddingException();
        }
    }
}
