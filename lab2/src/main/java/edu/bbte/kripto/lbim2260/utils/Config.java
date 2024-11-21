package edu.bbte.kripto.lbim2260.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class Config {
    Map<String, Profile> profiles;

    @Getter
    public static class Profile {
        @JsonProperty
        private int blockSize;
        @JsonProperty
        private String alg;
        @JsonProperty
        private String key;
        @JsonProperty
        private String method;
        @JsonProperty
        private String iv;
        @JsonProperty
        private String padding;
    }

}
