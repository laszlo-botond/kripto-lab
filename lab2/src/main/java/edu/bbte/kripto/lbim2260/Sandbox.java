package edu.bbte.kripto.lbim2260;

import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import java.util.Arrays;

@Slf4j
public class Sandbox {
    public static void main(String[] args) throws BadPaddingException {
        byte[] original = new byte[]{0, 11, -22, 33};
        byte[] plain = new byte[]{12, 22, 24, -5};
        byte[] encrypted = encrypt(original);

        try {
            byte[] cipher = ArrayUtils.xorEach(encrypted, plain);

            System.out.println(Arrays.toString(ArrayUtils.xorEach(encrypted, cipher)));
        } catch (Exception e) {
            System.out.println("Error");
        }

    }

    private static byte[] encrypt(byte[] b) {
        byte[] ec = new byte[b.length];
        for (int i=0; i<b.length; i++) {
            ec[i] = (byte) (b[i] - 1);
        }
        return ec;
    }
}