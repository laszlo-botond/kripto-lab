package edu.bbte.kripto.lbim2260;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.AssetLoader;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.lang.System.exit;

@Slf4j
public class RoundTripTest {
    public static void main(String[] args) {
        // INIT
        byte[] fileBytes = {};
        byte[] paddedFileBytes = {};
        AssetLoader loader = new AssetLoader();

        // SETUP
        Crypto crypto = AssetLoader.setupCrypto();

        // READ FILE + ADD PADDING
        try {
            fileBytes = loader.loadBytes("testFile.png");
            paddedFileBytes = crypto.getPadder().padByteArray(fileBytes);
            log.info("Added padding!");
        } catch (FileNotFoundException | BadPaddingException e) {
            log.error("File not found, exiting...");
            exit(0);
        }

        // ENCRYPT + DECRYPT
        try {
            byte[] enc = crypto.encrypt(paddedFileBytes);
            log.info("Encryption done!");
            byte[] dec = crypto.decrypt(enc);
            log.info("Decryption done!");
            byte[] decNoPad = crypto.getPadder().removePadding(dec);
            log.info("Removed padding!");

            log.debug("Original file length in bytes: {}", fileBytes.length);
            log.debug("Original file length after padding: {}", paddedFileBytes.length);
            log.debug("Decrypted file length in bytes, before removing padding: {}", dec.length);
            log.debug("Decrypted file length in bytes, after removing padding: {}", decNoPad.length);

            // TESTS
            if (fileBytes.length != decNoPad.length) {
                throw new IOException("File size mismatch!");
            }
            for (int i=0; i<fileBytes.length; i++) {
                if (paddedFileBytes[i] != decNoPad[i]) {
                    throw new IOException("File data mismatch at " + i + "!");
                }
            }

            log.info("Round-trip successful!");
        } catch (Exception e) {
            log.error("Encrypt-Decrypt error", e);
            exit(0);
        }
    }
}