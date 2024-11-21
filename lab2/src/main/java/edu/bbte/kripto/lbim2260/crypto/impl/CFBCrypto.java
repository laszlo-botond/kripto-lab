package edu.bbte.kripto.lbim2260.crypto.impl;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.padding.Padder;
import edu.bbte.kripto.lbim2260.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static java.lang.System.exit;

@Slf4j
public class CFBCrypto extends Crypto {
    byte[] nonce = new byte[12];

    public CFBCrypto(String key, int blockSize, String iv, Padder padder) {
        super(key, blockSize, iv, padder);
    }

    @Override
    public byte[] encrypt(byte[] plain) {
        int blockNr = plain.length / blockSize;

        byte[] result = new byte[]{};

        byte[] toEncrypt = iv;
        try {
            for (int i = 0; i < blockNr; i++) {
                // ENCRYPT IV or CIPHER BLOCK
                byte[] encryptedByteExtendedBlock = aead.seal(nonce, toEncrypt, data);
                byte[] encryptedByteBlock = ArrayUtils.subArray(encryptedByteExtendedBlock, 0, blockSize);

                // GET PLAINTEXT BLOCK
                byte[] plainByteBlock = ArrayUtils.subArray(plain, i * blockSize, (i + 1) * blockSize);

                // XOR the ENCRYPTED BLOCK with the PLAINTEXT BLOCK
                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        plainByteBlock,
                        encryptedByteBlock
                );

                // ADD TO RESULT
                result = ArrayUtils.concatBytes(result, xorAppliedByteBlock);

                // SET AS NEXT TO BE ENCRYPTED
                toEncrypt = xorAppliedByteBlock.clone();
            }
        } catch (IOException e) {
            log.error("Error concatenating encryption results. Exiting...", e);
            exit(0);
        }

        return result;
    }

    @Override
    public byte[] decrypt(byte[] cipher) {
        int blockNr = cipher.length / blockSize;

        byte[] result = new byte[]{};

        byte[] toEncrypt = iv;
        try {
            for (int i = 0; i < blockNr; i++) {
                // ENCRYPT IV or CIPHER BLOCK
                byte[] encryptedByteExtendedBlock = aead.seal(nonce, toEncrypt, data);
                byte[] encryptedByteBlock = ArrayUtils.subArray(encryptedByteExtendedBlock, 0, blockSize);

                // GET CIPHER BLOCK
                byte[] cipherByteBlock = ArrayUtils.subArray(cipher, i * blockSize, (i + 1) * blockSize);

                // XOR the ENCRYPTED BLOCK with CIPHER BLOCK
                byte[] xorAppliedByteBlock = ArrayUtils.xorEach(
                        encryptedByteBlock,
                        cipherByteBlock
                );

                // ADD TO RESULT
                result = ArrayUtils.concatBytes(
                        result,
                        xorAppliedByteBlock
                );

                // SET AS NEXT TO BE ENCRYPTED
                toEncrypt = cipherByteBlock.clone();
            }
        } catch (IOException e) {
            log.error("Error concatenating decryption results. Exiting...");
            exit(0);
        }

        return result;
    }
}
