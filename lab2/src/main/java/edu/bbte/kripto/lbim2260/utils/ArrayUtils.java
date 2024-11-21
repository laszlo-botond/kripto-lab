package edu.bbte.kripto.lbim2260.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class ArrayUtils {

    public static byte[] subArray(byte[] arr, int startIncl, int endExcl) {
        byte[] result = new byte[endExcl-startIncl];
        int index = 0;
        for (int i=startIncl; i<endExcl; i++) {
            result[index++] = arr[i];
        }
        return result;
    }

    public static byte[] concatBytes(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( a );
        outputStream.write( b );

        byte[] c = outputStream.toByteArray( );
        outputStream.close();
        return c;
    }

    public static byte[] xorEach(byte[] a, byte[] b) throws IOException {
        if (a.length != b.length) {
//            throw new IOException("Block size mismatch! " + a.length + " and " + b.length);
            log.warn("Uneven size XOR! {} and {}", a.length, b.length);
        }
        byte[] longer = a.length > b.length ? a : b;
        byte[] result = new byte[longer.length];
        int common = Math.min(a.length, b.length);
        for (int i=0; i<common; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        for (int i=common; i<longer.length; i++) {
            result[i] = longer[i];
        }
        return result;
    }
}
