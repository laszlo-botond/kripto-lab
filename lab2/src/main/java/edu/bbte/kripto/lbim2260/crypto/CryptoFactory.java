package edu.bbte.kripto.lbim2260.crypto;

import edu.bbte.kripto.lbim2260.crypto.impl.*;
import edu.bbte.kripto.lbim2260.padding.Padder;
import lombok.extern.slf4j.Slf4j;

import static java.lang.System.exit;

@Slf4j
public class CryptoFactory {
    public static Crypto getCrypto(String type, String key, int blockSize, String iv, Padder padder) {
        switch (type) {
            case "ECB": {
                return new ECBCrypto(key, blockSize, iv, padder);
            }
            case "CBC": {
                return new CBCCrypto(key, blockSize, iv, padder);
            }
            case "CFB": {
                return new CFBCrypto(key, blockSize, iv, padder);
            }
            case "OFB": {
                return new OFBCrypto(key, blockSize, iv, padder);
            }
            case "CTR": {
                return new CTRCrypto(key, blockSize, iv, padder);
            }
            default: {
                log.error("Unknown method, exiting...");
                exit(0);
            }
        }
        return new ECBCrypto(key, blockSize, iv, padder);
    }
}
