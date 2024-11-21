package edu.bbte.kripto.lbim2260;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.AssetLoader;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import java.io.FileNotFoundException;

import static java.lang.System.exit;

@Slf4j
public class Encrypt {
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

        // ENCRYPT FILE
        byte[] enc = crypto.encrypt(paddedFileBytes);
        log.info("Encryption done!");

        // WRITE TO DISK
        loader.writeToDisk("encrypted.lbim2260-crypt", enc);
    }
}
