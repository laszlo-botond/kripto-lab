package edu.bbte.kripto.lbim2260.crypto;

import com.codahale.aesgcmsiv.AEAD;
import edu.bbte.kripto.lbim2260.padding.Padder;
import edu.bbte.kripto.lbim2260.padding.PadderFactory;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.lang.System.exit;

@Slf4j
public abstract class Crypto {
    protected final Charset charset = StandardCharsets.ISO_8859_1;
    protected final AEAD aead;
    protected final VigenereCipherer vcip;
    protected final byte[] data;
    protected final int blockSize;
    protected final byte[] iv;
    protected final byte[] key;
    protected final String algorithm;
    @Getter
    protected final Padder padder;

    public Crypto(String key, int blockSize, String iv, String padding, String algorithm) {
        String localKey;
        if (key.length() <= 16) {
            localKey = key.repeat(16).substring(0, 16);
        } else {
            localKey = key.repeat(2).substring(0, 32);
        }

        this.key = key.getBytes(charset);
        this.aead = new AEAD(localKey.getBytes(charset));
        this.vcip = new VigenereCipherer(this.key);
        this.data = new byte[]{};
        this.blockSize = blockSize;
        this.padder = PadderFactory.getPadder(padding, blockSize);
        this.algorithm = algorithm;

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


    public byte[] encrypt(byte[] plain) {
        switch (algorithm.toLowerCase()) {
            case "aes": {
                return encryptAes(plain);
            }
            case "vigenere": {
                return encryptVigenere(plain);
            }
            default: {
                log.error("Unknown encryption algorithm specified! Exiting...");
                exit(0);
            }
        }
        return new byte[0];
    }

    public byte[] decrypt(byte[] cipher) {
        switch (algorithm.toLowerCase()) {
            case "aes": {
                return decryptAes(cipher);
            }
            case "vigenere": {
                return decryptVigenere(cipher);
            }
            default: {
                log.error("Unknown decryption algorithm specified! Exiting...");
                exit(0);
            }
        }
        return new byte[0];
    }

    public abstract byte[] encryptAes(byte[] plain);

    public abstract byte[] decryptAes(byte[] cipher);

    public abstract byte[] encryptVigenere(byte[] plain);

    public abstract byte[] decryptVigenere(byte[] cipher);

    protected static class VigenereCipherer {
        byte[] key;
        public VigenereCipherer(byte[] key) {
            VigenereCipherer.this.key = key;
        }

        public byte[] seal(byte[] plain) {
            return ArrayUtils.addArrays(plain, key);
        }

        public byte[] open(byte[] cipher) {
            return ArrayUtils.subtractArrays(cipher, key);
        }
    }
}
