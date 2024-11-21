package edu.bbte.kripto.lbim2260.crypto.impl;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static java.lang.System.exit;

@Slf4j
public class CTRCrypto extends Crypto {
    byte[] nonce = new byte[12];

    public CTRCrypto(String key, int blockSize, String iv, String padding, String algorithm) {
        super(key, blockSize, iv, padding, algorithm);
    }

    @Override
    public byte[] encryptAes(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};

        byte[] toEncrypt;
        try {
            for (int i = 0; i < blockNr; i++) {
                // APPLY COUNTER
                toEncrypt = ArrayUtils.concatBytes(
                        ArrayUtils.subArray(iv, 0, blockSize - 4),
                        new byte[]{(byte) (i & 0xF000), (byte) (i & 0x0F00), (byte) (i & 0x00F0), (byte) (i & 0x000F)}
                );

                // ENCRYPTION
                byte[] encryptedBlock = ArrayUtils.subArray(aead.seal(nonce, toEncrypt, data), 0, blockSize);

                // GET PLAINTEXT BLOCK
                byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);

                // XOR the ENCRYPTED BLOCK with the PLAINTEXT BLOCK
                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        plainByteBlock,
                        encryptedBlock
                );

                // ADD TO RESULT
                result = ArrayUtils.concatBytes(result, xorAppliedByteBlock);
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }

        return result;
    }

    @Override
    public byte[] decryptAes(byte[] cipher) {
        return encryptAes(cipher);
    }

    @Override
    public byte[] encryptVigenere(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};

        byte[] toEncrypt;
        try {
            for (int i = 0; i < blockNr; i++) {
                // APPLY COUNTER
                toEncrypt = ArrayUtils.concatBytes(
                        ArrayUtils.subArray(iv, 0, blockSize - 4),
                        new byte[]{(byte) (i & 0xF000), (byte) (i & 0x0F00), (byte) (i & 0x00F0), (byte) (i & 0x000F)}
                );

                // ENCRYPTION
                byte[] encryptedBlock = vcip.seal(toEncrypt);

                // GET PLAINTEXT BLOCK
                byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);

                // XOR the ENCRYPTED BLOCK with the PLAINTEXT BLOCK
                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        plainByteBlock,
                        encryptedBlock
                );

                // ADD TO RESULT
                result = ArrayUtils.concatBytes(result, xorAppliedByteBlock);
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }

        return result;
    }

    @Override
    public byte[] decryptVigenere(byte[] cipher) {
        return encryptVigenere(cipher);
    }
}
