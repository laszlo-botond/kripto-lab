package edu.bbte.kripto.lbim2260.client.dao;

import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@NoArgsConstructor
@Primary
public class MemoryClientDao implements ClientDao {

    @Value("${idClient}")
    String idClient;

    @Value("${blockCipherModes}")
    String[] arrayOfBlockCipherModes;
    Collection<String> blockCipherModes;

    Map<String, byte[]> knownPublicKeys = new HashMap<>(); // cache, to avoid repeated http query
    Map<String, String> knownCommonKeys = new HashMap<>();
    Map<String, Collection<String>> commonCrpytMethods = new HashMap<>();

    KeyPair ownKeyPair;

    @PostConstruct
    protected void init() {
        blockCipherModes = Arrays.stream(arrayOfBlockCipherModes).toList();
    }

    @Override
    public Collection<String> getSupportedCipherModes() {
        return blockCipherModes;
    }

    @Override
    public String getId() {
        return idClient;
    }

    @Override
    public void saveOwnKey(KeyPair ownKeyPair) {
        this.ownKeyPair = ownKeyPair;
    }

    @Override
    public KeyPair getOwnKey() {
        return ownKeyPair;
    }

    @Override
    public void savePublicKey(String idClient, byte[] publicKey) {
        knownPublicKeys.put(idClient, publicKey);
    }

    @Override
    public byte[] getPublicKey(String idClient) {
        return knownPublicKeys.get(idClient);
    }

    @Override
    public void saveCommonKey(String idClient, String publicKey) {
        knownCommonKeys.put(idClient, publicKey);
    }

    @Override
    public String getCommonKey(String idClient) {
        return knownCommonKeys.get(idClient);
    }

    @Override
    public void saveCommonCryptMethod(String idClient, Collection<String> methods) {
        commonCrpytMethods.put(idClient, methods);
    }

    @Override
    public Collection<String> getCommonCryptMethod(String idClient) {
        return commonCrpytMethods.get(idClient);
    }
}
