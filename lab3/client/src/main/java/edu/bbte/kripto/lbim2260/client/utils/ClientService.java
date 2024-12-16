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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
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
            reqBody.setPublicKey(Arrays.toString(pair.getPublic().getEncoded()));

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

        try {
            String url = "http://localhost:" + idClient + "/comm/hello";
            log.info("Sending request to {}", url);

            // build request body
            HandshakeDto reqBody = new HandshakeDto();
            reqBody.setIdClient(clientDao.getId());
            reqBody.setBlockCipherList(clientDao.getSupportedCipherModes());

            return restTemplate.postForObject(url, reqBody, String.class);
        } catch (RestClientException e) {
            log.info("Didn't receive.", e);
            return null;
        }
    }

    public String httpSendHalfSecret(String idClient, String halfKey) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = "http://localhost:" + idClient + "/comm/half-key";
            log.info("Sending request to {}", url);

            // build request body
            HalfKeyDto reqBody = new HalfKeyDto();
            reqBody.setIdClient(clientDao.getId());
            reqBody.setHalfKey(halfKey);

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
        PublicKeyDto ansDto = jsonToPublicKeyDto(ans);
        log.info("Public key of {} is {}", idClient, ansDto.getPublicKey());
        clientDao.savePublicKey(idClient, ansDto.getPublicKey());

        // send handshake
        String ans2 = httpSendHandshake(idClient);
        HandshakeDto ansDto2 = jsonToHandshakeDto(ans2);
        receiveAck(ansDto2);
    }

    private void receiveAck(HandshakeDto incomingHandshake) {
        log.info("ACK from {}", incomingHandshake.getIdClient());
        log.info("Common encryption method: {}", incomingHandshake.getBlockCipherList());
        clientDao.saveCommonCryptMethod(incomingHandshake.getIdClient(), incomingHandshake.getBlockCipherList());
        int halfKey = Math.abs(new Random().nextInt());

        // receive answer and save half-key
        String ans = httpSendHalfSecret(incomingHandshake.getIdClient(), String.valueOf(halfKey));
        HalfKeyDto ansDto = jsonToHalfKeyDto(ans);
        clientDao.saveCommonKey(ansDto.getIdClient(), halfKey + ansDto.getHalfKey());
        log.info("Common key with {} is {}!", ansDto.getIdClient(), halfKey + ansDto.getHalfKey());
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
