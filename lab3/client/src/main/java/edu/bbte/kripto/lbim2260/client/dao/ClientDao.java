package edu.bbte.kripto.lbim2260.client.dao;

import java.security.KeyPair;
import java.util.Collection;

public interface ClientDao {

    public Collection<String> getSupportedCipherModes();

    public String getId();

    public void saveOwnKey(KeyPair ownKeyPair);

    public KeyPair getOwnKey();

    public void savePublicKey(String idClient, byte[] publicKey);

    public byte[] getPublicKey(String idClient);

    public void saveCommonKey(String idClient, String commonKey);

    public String getCommonKey(String idClient);

    public void saveCommonCryptMethod(String idClient, Collection<String> methods);

    public Collection<String> getCommonCryptMethod(String idClient);
}
