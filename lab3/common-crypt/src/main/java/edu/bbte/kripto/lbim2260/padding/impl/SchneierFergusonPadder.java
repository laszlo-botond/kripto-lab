package edu.bbte.kripto.lbim2260.padding.impl;

import edu.bbte.kripto.lbim2260.padding.Padder;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import java.io.IOException;

@Slf4j
public class SchneierFergusonPadder extends Padder {
    public SchneierFergusonPadder(int blockSize) {
        super(blockSize);
    }

    @Override
    public byte[] padByteArray(byte[] arr) throws BadPaddingException {
        try {
            byte toAppend = (byte) (blockSize - (arr.length % blockSize));
            byte[] tmp = arr.clone();
            for (int i=0; i<toAppend; i++) {
                tmp = ArrayUtils.concatBytes(tmp, new byte[]{toAppend});
            }
            return tmp;
        } catch (IOException e) {
            throw new BadPaddingException();
        }
    }

    @Override
    public byte[] removePadding(byte[] arr) throws BadPaddingException {
        try {
            byte paddingLength = arr[arr.length - 1];
            int times = 0x00FF & paddingLength;
            return ArrayUtils.subArray(arr, 0, arr.length - times);
        } catch (Exception e) {
            throw new BadPaddingException();
        }
    }
}
