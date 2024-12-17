package edu.bbte.kripto.lbim2260.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.bbte.kripto.lbim2260.client.dao.ClientDao;
import edu.bbte.kripto.lbim2260.client.dto.*;
import edu.bbte.kripto.lbim2260.crypto.Crypto;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static java.lang.System.exit;

@Service
@NoArgsConstructor
@Slf4j
public class ClientService {

    @Autowired
    ClientDao clientDao;

    private final String[] mapKeys = new String[] {"algorithms", "blockCipherModes", "paddingModes", "blockLengths"};

    @PostConstruct
    protected void init() {
        String ans = httpRegister();
        if (ans == null) {
            log.error("Couldn't register, exiting...");
            exit(0);
        }
    }

    // -------------------- HTTP requests --------------------

    public String httpRegister() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            clientDao.saveOwnKey(pair);

            // build request body
            RegistrationDto reqBody = new RegistrationDto();
            reqBody.setId(clientDao.getId());
            String stringifiedPublicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            reqBody.setPublicKey(stringifiedPublicKey);

            String url = "http://localhost:8000/keys";
            log.info("Sending registration request to {}", url);
            return restTemplate.postForObject(url, reqBody, String.class);
        } catch (RestClientException | NoSuchAlgorithmException e) {
            log.error("Couldn't register, exiting...", e);
            exit(0);
            return null;
        }
    }

    public String httpGetPublicKey(String idClient) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = "http://localhost:8000/keys/" + idClient;
            log.info("Sending request to {}", url);
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            log.info("Didn't receive.", e);
            return null;
        }
    }

    public String httpSendHandshake(String idClient) {
        RestTemplate restTemplate = new RestTemplate();

        // encrypt
        String encId = encryptRsa(clientDao.getId(), idClient);
        Collection<String> encCiphers = clientDao.getSupportedCipherModes()
                .stream()
                .map(mode -> encryptRsa(mode, idClient))
                .toList();
        try {
            String url = "http://localhost:" + idClient + "/comm/hello";
            log.info("Sending request to {}", url);

            // build request body
            HandshakeDto reqBody = new HandshakeDto();
            reqBody.setIdClient(encId);
            reqBody.setBlockCipherList(encCiphers);

            return restTemplate.postForObject(url, reqBody, String.class);
        } catch (RestClientException e) {
            log.info("Didn't receive.", e);
            return null;
        }
    }

    public String httpSendHalfSecret(String idClient, String halfKey) {
        RestTemplate restTemplate = new RestTemplate();

        // encrypt
        String encOwnId = encryptRsa(clientDao.getId(), idClient);
        String encHalfKey = encryptRsa(halfKey, idClient);

        try {
            String url = "http://localhost:" + idClient + "/comm/half-key";
            log.info("Sending request to {}", url);

            // build request body
            HalfKeyDto reqBody = new HalfKeyDto();
            reqBody.setIdClient(encOwnId);
            reqBody.setHalfKey(encHalfKey);

            return restTemplate.postForObject(url, reqBody, String.class);
        } catch (RestClientException e) {
            log.info("Didn't receive.", e);
            return null;
        }
    }

    public String httpSendMessage(String targetId, String message) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = "http://localhost:" + targetId + "/comm/msg";
            log.info("Sending request to {}", url);

            // build request body
            MessageDto reqBody = new MessageDto();
            reqBody.setIdClient(clientDao.getId());
            reqBody.setMessage(message);

            return restTemplate.postForObject(url, reqBody, String.class);
        } catch (RestClientException e) {
            log.info("Didn't receive.", e);
            return null;
        }
    }

    // -------------------- JSON convert --------------------

    public PublicKeyDto jsonToPublicKeyDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, PublicKeyDto.class);

        } catch (Exception e) {
            return null;
        }
    }

    public PublicKeyByteArrayDto jsonToPublicKeyByteArrayDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, PublicKeyByteArrayDto.class);

        } catch (Exception e) {
            log.error("Error converting...", e);
            return null;
        }
    }

    public HalfKeyDto jsonToHalfKeyDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, HalfKeyDto.class);

        } catch (Exception e) {
            return null;
        }
    }

    public HandshakeDto jsonToHandshakeDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, HandshakeDto.class);

        } catch (Exception e) {
            return null;
        }
    }

    public MessageDto jsonToMessageDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, MessageDto.class);

        } catch (Exception e) {
            return null;
        }
    }

    // -------------------- communication --------------------

    public void sendHandshake(String idClient) {
        // get partner's public key and save it
        String ans = httpGetPublicKey(idClient);
        PublicKeyByteArrayDto ansDto = jsonToPublicKeyByteArrayDto(ans);
        log.info("Public key of {} is {}", idClient, ansDto.getPublicKey());
        clientDao.savePublicKey(idClient, ansDto.getPublicKey());

        // send handshake
        String ans2 = httpSendHandshake(idClient);
        HandshakeDto ansDto2 = jsonToHandshakeDto(ans2);
        receiveAck(ansDto2);
    }

    private void receiveAck(HandshakeDto incomingHandshake) {
        // decrypt
        String decIdClient = decryptRsa(incomingHandshake.getIdClient());
        Collection<String> decBlockCipherList = incomingHandshake.getBlockCipherList()
                .stream()
                .map(this::decryptRsa)
                .toList();
        log.info("ACK from {}", decIdClient);
        log.info("Common encryption method: {}", decBlockCipherList);
        clientDao.saveCommonCryptMethod(decIdClient, decBlockCipherList);
        int halfKey = Math.abs(new Random().nextInt());

        // receive answer and save half-key
        String ans = httpSendHalfSecret(decIdClient, String.valueOf(halfKey));
        HalfKeyDto ansDto = jsonToHalfKeyDto(ans);
        // decrypt
        decIdClient = decryptRsa(ansDto.getIdClient());
        String decHalfKey = decryptRsa(ansDto.getHalfKey());
        clientDao.saveCommonKey(decIdClient, halfKey + decHalfKey);
        log.info("Common key with {} is {}!", decIdClient, halfKey + decHalfKey);
    }

    // -------------------- others --------------------

    public Collection<String> getCommonCipherMethod(Collection<String> partnerOptions) {
        Map<String, Collection<String>> myOptionsMap = extractAttributes(clientDao.getSupportedCipherModes());
        Map<String, Collection<String>> partnerOptionsMap = extractAttributes(partnerOptions);

        Collection<String> commonOptions = new ArrayList<>();
        for (String s : mapKeys) {
            List<String> common = myOptionsMap.get(s).stream().filter(opt -> partnerOptionsMap.get(s).contains(opt)).toList();
            if (common.isEmpty()) {
                return null;
            }
            commonOptions.add(common.getFirst());
        }
        return commonOptions;
    }

    // -------------------- Encrypt-Decrypt --------------------

    public String encryptMessage(Crypto crypto, String message) {
        try {
            byte[] byteArray = message.getBytes(StandardCharsets.ISO_8859_1);
            byte[] paddedBytes = crypto.getPadder().padByteArray(byteArray);
            byte[] enc = crypto.encrypt(paddedBytes);
            return new String(enc, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            log.error("Error encrypting message. It will not be sent.");
            return null;
        }
    }

    public String decryptMessage(Crypto crypto, String encryptedMessage) {
        try {
            byte[] byteArray = encryptedMessage.getBytes(StandardCharsets.ISO_8859_1);
            byte[] dec = crypto.decrypt(byteArray);
            byte[] decNoPad = crypto.getPadder().removePadding(dec);
            return new String(decNoPad, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            log.error("Error decrypting message. It will not be sent.", e);
            return null;
        }
    }

    public String encryptRsa(String message, String partnerId) {
        try {
            byte[] partnerPublicKeyBytes = clientDao.getPublicKey(partnerId);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(partnerPublicKeyBytes);
            PublicKey partnerPublicKey = keyFactory.generatePublic(publicKeySpec);


            byte[] byteArray = message.getBytes(StandardCharsets.ISO_8859_1);
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, partnerPublicKey);
            byte[] encryptedBytes = encryptCipher.doFinal(byteArray);
            return new String(encryptedBytes, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            log.error("RSA encryption error.", e);
            return null;
        }
    }

    public String decryptRsa(String message) {
        try {
            byte[] byteArray = message.getBytes(StandardCharsets.ISO_8859_1);
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, clientDao.getOwnKey().getPrivate());
            byte[] encryptedBytes = decryptCipher.doFinal(byteArray);
            return new String(encryptedBytes, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            log.error("RSA encryption error.", e);
            return null;
        }
    }

    // -------------------- private --------------------

    private Map<String, Collection<String>> extractAttributes(Collection<String> options) {
        Map<String, Collection<String>> map = new HashMap<>();
        int underscoresPassed = 0;
        map.put(mapKeys[0], new ArrayList<>());
        for (String s : options) {
            if ("_".equals(s)) {
                underscoresPassed++;
                map.put(mapKeys[underscoresPassed], new ArrayList<>());
                continue;
            }

            map.get(mapKeys[underscoresPassed]).add(s);
        }

        return map;
    }

}
