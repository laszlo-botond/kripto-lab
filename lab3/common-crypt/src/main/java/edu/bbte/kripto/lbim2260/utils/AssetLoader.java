package edu.bbte.kripto.lbim2260.utils;

import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.crypto.CryptoFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import static java.lang.System.exit;

@Slf4j
public class AssetLoader {
    public byte[] loadBytes(String path) throws FileNotFoundException{
        try {
            return Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(path).toURI()));
        } catch (IOException | NullPointerException | URISyntaxException e) {
            throw new FileNotFoundException(e.getClass().getSimpleName() + ": Can't load " + path);
        }
    }

    public void writeToDisk(String name, byte[] data) {
        try {
            String currentDir = System.getProperty("user.dir");

            String path = currentDir + "\\src\\main\\resources\\" + name;

            File outputFile = new File(path);
            Files.write(outputFile.toPath(), data);
            log.info("File written successfully!");
        } catch (IOException | NullPointerException e) {
            log.error("Can't write to target file. Exiting...");
            exit(0);
        }
    }

    public static Crypto setupCrypto() {
        Config.Profile profile = ConfigProvider.getProfile(System.getenv("CRYPT_PROFILE"));
        String key = profile.getKey();
        String iv = profile.getIv();
        int blockSize = profile.getBlockSize();
        if (blockSize % 8 != 0) {
            log.error("AES implementation only supports block sizes that are multiples of 8 bits! Exiting...");
            exit(0);
        }
        blockSize = blockSize / 8; // from bits to bytes
        log.info("Creating configuration: {} | {} | {}",
                profile.getAlg().toUpperCase(),
                profile.getMethod().toUpperCase(),
                profile.getPadding().toUpperCase()
        );
        return CryptoFactory.getCrypto(profile.getMethod(), key, blockSize, iv, profile.getPadding(), profile.getAlg());
    }

    public static Crypto setupCrypto(Collection<String> options, String commonKey) {
        String alg = (String) options.toArray()[0];
        String method = (String) options.toArray()[1];
        String padding = (String) options.toArray()[2];
        String blockSizeStr = (String) options.toArray()[3];
        String key = commonKey;
        String iv = commonKey;
        int blockSize = Integer.parseInt(blockSizeStr);

        if (blockSize % 8 != 0) {
            log.error("AES implementation only supports block sizes that are multiples of 8 bits! Exiting...");
            exit(0);
        }
        blockSize = blockSize / 8; // from bits to bytes
        log.info("Creating configuration: {} | {} | {}",
                alg.toUpperCase(),
                method.toUpperCase(),
                padding.toUpperCase()
        );
        return CryptoFactory.getCrypto(method, key, blockSize, iv, padding, alg);
    }
}
