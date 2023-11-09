package kr.co.samplepcb.xp.service;

import kr.co.samplepcb.xp.util.PcbPartsUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class PcbPartsServiceTest {

//    @Test
    public void parseStringTest() {
//        PcbPartsService.parseString("17.5x17.15x7.5mm 5% 500V");
    }

//    @Test
    public void testConvertToToTolerance() {
        PcbPartsUtils.FaradsConvert faradsConvert = new PcbPartsUtils.FaradsConvert();
        Map<PcbPartsUtils.PcbConvert.Unit, String> toleranceUnitStringMap = faradsConvert.convert("10%");
        toleranceUnitStringMap.forEach(
                (k, v) -> System.out.println("key: " + k + " value: " + v)
        );
    }
}
