package edu.bbte.kripto.lbim2260.crypto;

import com.codahale.aesgcmsiv.AEAD;
import edu.bbte.kripto.lbim2260.padding.Padder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static java.lang.System.exit;

@Slf4j
public abstract class Crypto {
    protected final Charset charset = StandardCharsets.ISO_8859_1;
    protected final AEAD aead;
    protected final byte[] data;
    protected final int blockSize;
    protected final byte[] iv;
    @Getter
    protected final Padder padder;

    public Crypto(String key, int blockSize, String iv, Padder padder) {
        String localKey;
        if (key.length() <= 16) {
            localKey = key.repeat(16).substring(0, 16);
        } else {
            localKey = key.repeat(2).substring(0, 32);
        }
        this.aead = new AEAD(localKey.getBytes(charset));
        this.data = new byte[]{};
        this.blockSize = blockSize;
        this.padder = padder;
        if (!this.getClass().getSimpleName().startsWith("ECB")) {
            this.iv = iv
                    .repeat((blockSize / iv.length()) + 1)
                    .substring(0, blockSize)
                    .getBytes(charset);
            if (this.iv.length != this.blockSize) {
                log.error("Length of IV must be equal to blockSize! (Required: {}, Provided: {})", blockSize, iv.length());
                exit(0);
            }
        } else {
            this.iv = new byte[0];
        }
    }


    public abstract byte[] encrypt(byte[] plain);

    public abstract byte[] decrypt(byte[] cipher);

    protected String bytesToString(byte[] bytes) {
        return new String(bytes, charset);
    }

    protected byte[] stringToBytes(String string) {
        return string.getBytes(charset);
    }

    public byte[] encryptBlock(byte[] plainBlock, String key) {
        return aead.seal(plainBlock, data);
    }

    public byte[] decryptBlock(byte[] cipherBlock, String key) {
        final Optional<byte[]> result = aead.open(cipherBlock, data);
        return result.get();
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    protected byte[] buildSimpleByteArray(List<Byte> list) {
        byte[] converted = new byte[list.size()];
        for (int i=0; i<list.size(); i++) {
            converted[i] = list.get(i);
        }
        return converted;
    }
}
