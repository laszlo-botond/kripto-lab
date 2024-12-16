package edu.bbte.kripto.lbim2260.crypto.impl;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.System.exit;

@Slf4j
public class ECBCrypto extends Crypto {

    public ECBCrypto(String key, int blockSize, String iv, String padding, String algorithm) {
        super(key, blockSize, iv, padding, algorithm);
    }

    @Override
    public byte[] encryptAes(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};
        for (int i = 0; i < blockNr; i++) {
            byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);
            byte[] encryptedByteBlock = aead.seal(plainByteBlock, data);
            try {
                result = ArrayUtils.concatBytes(result, encryptedByteBlock);
            } catch (IOException e) {
                log.error("Error concatenating encryption results. Exiting...");
                exit(0);
            }
        }

        return result;
    }

    @Override
    public byte[] decryptAes(byte[] cipher) {
        int localBlockSize = blockSize + 28;
        int blockNr = cipher.length / localBlockSize;

        byte[] result = new byte[]{};
        for (int i = 0; i < blockNr; i++) {
            byte[] cipherByteBlock = ArrayUtils.subArray(cipher, i * localBlockSize, (i + 1) * localBlockSize);
            Optional<byte[]> optional = aead.open(cipherByteBlock, data);
            try {
                byte[] decryptedByteBlock = optional.get();
                result = ArrayUtils.concatBytes(result, decryptedByteBlock);
            } catch (IOException | NoSuchElementException e) {
                log.error("Error concatenating decryption results. Exiting...");
                exit(0);
            }
        }

        return result;
    }

    @Override
    public byte[] encryptVigenere(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};
        try {
            for (int i = 0; i < blockNr; i++) {
                byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);

                result = ArrayUtils.concatBytes(
                        result,
                        vcip.seal(plainByteBlock)
                );
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }
        return result;
    }

    @Override
    public byte[] decryptVigenere(byte[] cipher) {
        int blockNr = cipher.length / blockSize;

        byte[] result = new byte[]{};
        try {
            for (int i = 0; i < blockNr; i++) {
                byte[] cipherByteBlock = ArrayUtils.subArray(cipher, i * blockSize, (i + 1) * blockSize);

                result = ArrayUtils.concatBytes(
                        result,
                        vcip.open(cipherByteBlock)
                );
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }
        return result;
    }
}
