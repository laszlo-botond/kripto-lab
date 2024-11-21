package edu.bbte.kripto.lbim2260.padding;

import edu.bbte.kripto.lbim2260.padding.impl.DESPadder;
import edu.bbte.kripto.lbim2260.padding.impl.SchneierFergusonPadder;
import edu.bbte.kripto.lbim2260.padding.impl.ZeroPadder;
import lombok.extern.slf4j.Slf4j;

import static java.lang.System.exit;

@Slf4j
public class PadderFactory {
    public static Padder getPadder(String type, int blockSize) {
        switch (type.toUpperCase()) {
            case "ZERO": {
                return new ZeroPadder(blockSize);
            }
            case "DES": {
                return new DESPadder(blockSize);
            }
            case "S-F": {
                return new SchneierFergusonPadder(blockSize);
            }
            default: {
                log.error("Unknown padding type, exiting...");
                exit(0);
            }
        }
        return new ZeroPadder(blockSize);
    }
}
