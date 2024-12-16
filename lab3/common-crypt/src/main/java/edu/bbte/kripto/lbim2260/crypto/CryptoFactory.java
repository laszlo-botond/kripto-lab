package edu.bbte.kripto.lbim2260.crypto;

import edu.bbte.kripto.lbim2260.crypto.impl.*;
import lombok.extern.slf4j.Slf4j;

import static java.lang.System.exit;

@Slf4j
public class CryptoFactory {
    public static Crypto getCrypto(String type, String key, int blockSize, String iv, String padding, String algorithm) {
        switch (type.toUpperCase()) {
            case "ECB": {
                return new ECBCrypto(key, blockSize, iv, padding, algorithm);
            }
            case "CBC": {
                return new CBCCrypto(key, blockSize, iv, padding, algorithm);
            }
            case "CFB": {
                return new CFBCrypto(key, blockSize, iv, padding, algorithm);
            }
            case "OFB": {
                return new OFBCrypto(key, blockSize, iv, padding, algorithm);
            }
            case "CTR": {
                return new CTRCrypto(key, blockSize, iv, padding, algorithm);
            }
            default: {
                log.error("Unknown method, exiting...");
                exit(0);
            }
        }
        return new ECBCrypto(key, blockSize, iv, padding, algorithm);
    }
}
