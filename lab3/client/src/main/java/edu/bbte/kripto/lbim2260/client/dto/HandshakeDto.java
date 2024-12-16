package edu.bbte.kripto.lbim2260.client.dto;

import lombok.Data;

import java.util.Collection;

@Data
public class HandshakeDto {
    String idClient;
    Collection<String> blockCipherList;
}
