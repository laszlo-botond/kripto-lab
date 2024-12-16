package edu.bbte.kripto.lbim2260.keyserver.dao;

import edu.bbte.kripto.lbim2260.keyserver.dao.exception.IdNotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Primary
@Repository
public class MemoryKeyDao implements KeyDao {

    ConcurrentHashMap<String, String> keyMap;

    public MemoryKeyDao() {
        keyMap = new ConcurrentHashMap<>();
    }

    @Override
    public String findKey(String id) throws IdNotFoundException {
        if (!keyMap.containsKey(id)) {
            throw new IdNotFoundException("Requested ID not found!");
        }
        return keyMap.get(id);
    }

    @Override
    public void registerKey(String id, String publicKey) {
        keyMap.put(id, publicKey);
    }
}
