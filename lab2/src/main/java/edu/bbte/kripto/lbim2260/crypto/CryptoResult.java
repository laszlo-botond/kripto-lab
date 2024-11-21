package edu.bbte.kripto.lbim2260.crypto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
public class CryptoResult {
    private final ArrayList<String> parts;
    @Setter
    private byte[] byteContent;

    public CryptoResult() {
        parts = new ArrayList<>();
    }

    public String getPart(int index) {
        return parts.get(index);
    }

    public void push(String string) {
        parts.add(string);
    }

    public String toString() {
        return String.join("", parts);
    }
}
