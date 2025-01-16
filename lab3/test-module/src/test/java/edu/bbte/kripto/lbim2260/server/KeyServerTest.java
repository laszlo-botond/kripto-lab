package edu.bbte.kripto.lbim2260.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.bbte.kripto.lbim2260.server.dto.PublicKeyDto;
import edu.bbte.kripto.lbim2260.server.dto.RegistrationDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.lang.System.exit;
import static org.junit.jupiter.api.Assertions.*;

public class KeyServerTest {
    @Test
    public void testKeyWorkflow() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // build request body
            RegistrationDto reqBody = new RegistrationDto();
            reqBody.setId("testId");
            reqBody.setPublicKey("testPublicKey");

            String url = "http://localhost:8000/keys";
            restTemplate.postForObject(url, reqBody, String.class);

            url = "http://localhost:8000/keys/testId";
            String resp = restTemplate.getForObject(url, String.class);
            PublicKeyDto publicKey = jsonToPublicKeyDto(resp);
            assertEquals("testPublicKey", publicKey.getPublicKey(), "Key server does not serve consistent data!");
        } catch (RestClientException e) {
            fail("The key server is unreachable!");
        }
    }

    public PublicKeyDto jsonToPublicKeyDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, PublicKeyDto.class);

        } catch (Exception e) {
            return null;
        }
    }
}
