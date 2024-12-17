package edu.bbte.kripto.lbim2260.client.controller;

import edu.bbte.kripto.lbim2260.client.dao.ClientDao;
import edu.bbte.kripto.lbim2260.client.dto.*;
import edu.bbte.kripto.lbim2260.client.utils.ClientService;
import edu.bbte.kripto.lbim2260.crypto.Crypto;
import edu.bbte.kripto.lbim2260.utils.AssetLoader;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Random;

@RestController
@RequestMapping("/comm")
@Slf4j
public class ClientController {

    @Autowired
    ClientDao clientDao;

    @Autowired
    ClientService clientService;

    @GetMapping("/test/{partnerId}")
    public void startCommunication(HttpServletRequest req, @PathVariable("partnerId") String partnerId) {
        clientService.sendHandshake(partnerId);
    }

    @PostMapping("/hello")
    public HandshakeDto receiveHello(HttpServletRequest req, @RequestBody HandshakeDto incomingHandshake) {
        log.debug("Received: {}", incomingHandshake);
        // decrypt
        String clientId = clientService.decryptRsa(incomingHandshake.getIdClient());
        Collection<String> decryptedBlockCipherList = incomingHandshake.getBlockCipherList().stream().map(mode -> clientService.decryptRsa(mode)).toList();

        log.info("{} {}", req.getMethod(), req.getRequestURL());
        log.info("Handshake request from {}", clientId);
        try {
            // get partner's public key and save it
            String ans = clientService.httpGetPublicKey(clientId);
            PublicKeyByteArrayDto ansDto = clientService.jsonToPublicKeyByteArrayDto(ans);
            log.info("Public key of {} is {}", clientId, ansDto.getPublicKey());
            clientDao.savePublicKey(clientId, ansDto.getPublicKey());

            // build response with the joint cipher method and save it
            // encrypt
            String encId = clientService.encryptRsa(clientDao.getId(), clientId);
            Collection<String> commonList = clientService.getCommonCipherMethod(decryptedBlockCipherList);
            Collection<String> encCommonList = commonList
                    .stream()
                    .map(mode -> clientService.encryptRsa(mode, clientId))
                    .toList();
            HandshakeDto response = new HandshakeDto();
            response.setIdClient(encId);
            response.setBlockCipherList(encCommonList);
            clientDao.saveCommonCryptMethod(clientId, commonList);

            log.info("Sending response crypt-method to {}...", clientId);
            return response;
        } catch (NullPointerException e) {
            throw new SecurityException();
        }
    }

    @PostMapping("/half-key")
    public HalfKeyDto receiveHalfKey(HttpServletRequest req, @RequestBody HalfKeyDto incomingHalfKey) {
        log.debug("Received: {}", incomingHalfKey);
        String decClientId = clientService.decryptRsa(incomingHalfKey.getIdClient());
        String decHalfKey = clientService.decryptRsa(incomingHalfKey.getHalfKey());

        log.info("{} {}", req.getMethod(), req.getRequestURL());
        log.info("Half-key request from {}", decClientId);
        try {
            // build response with other half of key
            // encrypt
            String encOwnId = clientService.encryptRsa(clientDao.getId(), decClientId);
            HalfKeyDto response = new HalfKeyDto();
            response.setIdClient(encOwnId);
            int halfKey = Math.abs(new Random().nextInt());
            String encHalfKey = clientService.encryptRsa(String.valueOf(halfKey), decClientId);
            response.setHalfKey(encHalfKey);

            // save common key
            clientDao.saveCommonKey(decClientId, decHalfKey + halfKey);
            log.info("Common key with {} is {}!", decClientId, decHalfKey + halfKey);

            log.info("Sending response half-key to {}...", decClientId);
            return response;
        } catch (NullPointerException e) {
            throw new SecurityException();
        }
    }

    @PostMapping("/sendMessage")
    public MessageDto sendMessage(HttpServletRequest req, @RequestBody TargetedMessageDto messageDto) {
        String partner = messageDto.getTargetId();

        log.info("{} {}", req.getMethod(), req.getRequestURL());
        log.info("Encrypting message...");

        String commonKey = clientDao.getCommonKey(partner);
        Crypto crypto = AssetLoader.setupCrypto(clientDao.getCommonCryptMethod(partner), clientDao.getCommonKey(partner));
        String encryptedMessage = clientService.encryptMessage(crypto, messageDto.getMessage());

        String ans = clientService.httpSendMessage(messageDto.getTargetId(), encryptedMessage);
        MessageDto ansDto = clientService.jsonToMessageDto(ans);

        log.info("Encrypted and sent!");
        return ansDto;
    }

    @PostMapping("/msg")
    public MessageDto receiveMessage(HttpServletRequest req, @RequestBody MessageDto messageDto) {
        log.info("{} {}", req.getMethod(), req.getRequestURL());
        System.out.println("Received: ");
        System.out.println(messageDto.getMessage());
        log.info("Decrypting message...");

        String partner = messageDto.getIdClient();

        String commonKey = clientDao.getCommonKey(partner);
        Crypto crypto = AssetLoader.setupCrypto(clientDao.getCommonCryptMethod(partner), clientDao.getCommonKey(partner));
        String decryptedMessage = clientService.decryptMessage(crypto, messageDto.getMessage());

        System.out.println("Decrypted message:");
        System.out.println(decryptedMessage);

        MessageDto ansDto = new MessageDto();
        ansDto.setIdClient(clientDao.getId());
        ansDto.setMessage("Message received and decrypted!");
        return ansDto;
    }
}
