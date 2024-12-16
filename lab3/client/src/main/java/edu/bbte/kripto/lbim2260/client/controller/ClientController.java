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
        log.info("{} {}", req.getMethod(), req.getRequestURL());
        log.info("Handshake request from {}", incomingHandshake.getIdClient());
        try {
            // get partner's public key and save it
            String ans = clientService.httpGetPublicKey(incomingHandshake.getIdClient());
            PublicKeyDto ansDto = clientService.jsonToPublicKeyDto(ans);
            log.info("Public key of {} is {}", incomingHandshake.getIdClient(), ansDto.getPublicKey());
            clientDao.savePublicKey(incomingHandshake.getIdClient(), ansDto.getPublicKey());

            // build response with the joint cipher method and save it
            HandshakeDto response = new HandshakeDto();
            response.setIdClient(clientDao.getId());
            Collection<String> commonList = clientService.getCommonCipherMethod(incomingHandshake.getBlockCipherList());
            response.setBlockCipherList(commonList);
            clientDao.saveCommonCryptMethod(incomingHandshake.getIdClient(), commonList);

            log.info("Sending response crypt-method to {}...", incomingHandshake.getIdClient());
            return response;
        } catch (NullPointerException e) {
            throw new SecurityException();
        }
    }

    @PostMapping("/half-key")
    public HalfKeyDto receiveHalfKey(HttpServletRequest req, @RequestBody HalfKeyDto incomingHalfKey) {
        log.info("{} {}", req.getMethod(), req.getRequestURL());
        log.info("Half-key request from {}", incomingHalfKey.getIdClient());
        try {
            // get partner's public key
            String publicKey = clientDao.getPublicKey(incomingHalfKey.getIdClient());

            // build response with other half of key
            HalfKeyDto response = new HalfKeyDto();
            response.setIdClient(clientDao.getId());
            int halfKey = Math.abs(new Random().nextInt());
            response.setHalfKey(String.valueOf(halfKey));

            // save common key
            clientDao.saveCommonKey(incomingHalfKey.getIdClient(), incomingHalfKey.getHalfKey() + halfKey);
            log.info("Common key with {} is {}!", incomingHalfKey.getIdClient(), incomingHalfKey.getHalfKey() + halfKey);

            log.info("Sending response half-key to {}...", incomingHalfKey.getIdClient());
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
