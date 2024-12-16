package edu.bbte.kripto.lbim2260.keyserver.dao;

import edu.bbte.kripto.lbim2260.keyserver.dao.exception.IdNotFoundException;

public interface KeyDao {

    public String findKey(String id) throws IdNotFoundException;

    public void registerKey(String id, String publicKey);

}
