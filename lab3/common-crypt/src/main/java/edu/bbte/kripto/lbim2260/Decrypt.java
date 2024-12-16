package edu.bbte.kripto.lbim2260;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.AssetLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

import static java.lang.System.exit;

@Slf4j
public class Decrypt {
    public static void main(String[] args) {
        // INIT
        byte[] fileBytes = {};
        AssetLoader loader = new AssetLoader();

        // SETUP
        Crypto crypto = AssetLoader.setupCrypto();

        // READ FILE + ADD PADDING
        try {
            fileBytes = loader.loadBytes("encrypted.lbim2260-crypt");
            log.info("File loaded!");
        } catch (FileNotFoundException e) {
            log.error("File not found, exiting...");
            exit(0);
        }

        // ENCRYPT + DECRYPT
        try {
            byte[] dec = crypto.decrypt(fileBytes);
            log.info("Decryption done!");
            byte[] decNoPad = crypto.getPadder().removePadding(dec);
            log.info("Removed padding!");

            loader.writeToDisk("decrypted.png", decNoPad);

        } catch (Exception e) {
            log.error("Encrypt-Decrypt error", e);
            exit(0);
        }
    }
}
