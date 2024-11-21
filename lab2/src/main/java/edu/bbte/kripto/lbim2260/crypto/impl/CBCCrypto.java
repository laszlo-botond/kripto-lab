package edu.bbte.kripto.lbim2260.crypto.impl;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.System.exit;

@Slf4j
public class CBCCrypto extends Crypto {
    public CBCCrypto(String key, int blockSize, String iv, String padding, String algorithm) {
        super(key, blockSize, iv, padding, algorithm);
    }

    @Override
    public byte[] encryptAes(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};
        byte[] filler = new byte[28];

        byte[] lastEncrypted = iv;
        try {
            for (int i = 0; i < blockNr; i++) {
                byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);

                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        lastEncrypted,
                        ArrayUtils.concatBytes(plainByteBlock, filler)
                );

                byte[] encryptedByteBlock = aead.seal(
                        ArrayUtils.subArray(xorAppliedByteBlock, 0, blockSize),
                        data);

                result = ArrayUtils.concatBytes(result, encryptedByteBlock);
                lastEncrypted = encryptedByteBlock;
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }

        return result;
    }

    @Override
    public byte[] decryptAes(byte[] cipher) {
        int localBlockSize = blockSize + 28;
        int blockNr = cipher.length / localBlockSize;

        byte[] result = new byte[]{};
        for (int i = blockNr - 1; i >= 0; i--) {
            byte[] nextEncrypted = (i > 0
                    ? ArrayUtils.subArray(cipher, (i - 1) * localBlockSize, (i - 1) * localBlockSize + blockSize)
                    : iv
            );

            byte[] cipherByteBlock = ArrayUtils.subArray(cipher, i * localBlockSize, (i + 1) * localBlockSize);
            Optional<byte[]> optional = aead.open(cipherByteBlock, data);
            try {
                byte[] decryptedByteBlock = optional.get();

                byte[] resultPart = ArrayUtils.xorEach(decryptedByteBlock, nextEncrypted);

                result = ArrayUtils.concatBytes(resultPart, result);
            } catch (IOException | NoSuchElementException e) {
                log.error("Error concatenating decryption results. Exiting...", e);
                exit(0);
            }
        }

        return result;
    }

    @Override
    public byte[] encryptVigenere(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};

        byte[] lastEncrypted = iv;
        try {
            for (int i = 0; i < blockNr; i++) {
                byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);

                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        lastEncrypted,
                        plainByteBlock
                );

                byte[] encryptedByteBlock = vcip.seal(xorAppliedByteBlock);

                result = ArrayUtils.concatBytes(result, encryptedByteBlock);
                lastEncrypted = encryptedByteBlock;
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

        byte[] lastDecrypted = iv;
        try {
            for (int i = 0; i < blockNr; i++) {
                byte[] cipherByteBlock = ArrayUtils.subArray(cipher, i * blockSize, (i + 1) * blockSize);

                byte[] decryptedByteBlock = vcip.open(cipherByteBlock);

                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        lastDecrypted,
                        decryptedByteBlock
                );

                result = ArrayUtils.concatBytes(result, xorAppliedByteBlock);
                lastDecrypted = cipherByteBlock;
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }

        return result;
    }
}
