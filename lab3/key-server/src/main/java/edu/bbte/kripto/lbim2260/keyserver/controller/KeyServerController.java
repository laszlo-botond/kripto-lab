package edu.bbte.kripto.lbim2260.keyserver.controller;

import edu.bbte.kripto.lbim2260.keyserver.dao.KeyDao;
import edu.bbte.kripto.lbim2260.keyserver.dao.exception.IdNotFoundException;
import edu.bbte.kripto.lbim2260.keyserver.dto.KeyAndIdDto;
import edu.bbte.kripto.lbim2260.keyserver.dto.PublicKeyDto;
import edu.bbte.kripto.lbim2260.keyserver.mapper.KeyMapper;
import edu.bbte.kripto.lbim2260.keyserver.model.KeyModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/keys")
@Slf4j
public class KeyServerController {

    @Autowired
    KeyDao keyDao;

    @Autowired
    KeyMapper keyMapper;

    @GetMapping("/{id}")
    public PublicKeyDto getKeyById(HttpServletRequest req, @PathVariable("id") String clientId) throws IdNotFoundException {
        log.info("{} {}", req.getMethod(), req.getRequestURL());
        String publicKey = keyDao.findKey(clientId);
        KeyModel result = new KeyModel();
        result.setPublicKey(publicKey);
        log.info("Sent publicKey of {}: {}.", clientId, publicKey);
        return keyMapper.keyModelToPublicKeyDto(result);
    }

    @PostMapping
    public KeyAndIdDto register(HttpServletRequest req, @RequestBody(required = true) KeyAndIdDto dto) {
        log.info("{} {}", req.getMethod(), req.getRequestURL());
        log.debug("RequestBody: {}", dto);
        KeyModel keyModel = keyMapper.keyAndIdDtoToKeyModel(dto);
        keyDao.registerKey(keyModel.getId(), keyModel.getPublicKey());
        log.info("Set publicKey of {}: {}.", keyModel.getId(), keyModel.getPublicKey());
        return dto;
    }
}
