package edu.bbte.kripto.lbim2260.utils;

import lombok.Getter;

import java.util.Map;

@Getter
public class Config {
    Map<String, Profile> profiles;

    @Getter
    public static class Profile {
        private int blockSize;
        private String alg;
        private String key;
        private String method;
        private String iv;
        private String padding;
    }

}
