package kr.co.samplepcb.xp.util;

import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PcbPartsUtils {

    public enum FaradsUnit {
        FARADS, MEGAFARADS, KILOFARADS, MILLIFARADS, MICROFARADS, NANOFARADS, PICOFARADS, NONE
    }

    /**
     * 미리 정의된 패턴에 기반하여 문자열을 파싱하고 각 구성요소를 분류합니다
     *
     * @param text the input string to be parsed
     * @return a Map containing various classifications of the input string
     */
    public static Map<String, List<String>> parseString(String text) {
        Map<String, List<String>> classifications = new HashMap<>();
        classifications.put(PcbPartsSearchField.PRODUCT_NAME, new ArrayList<>());

        Map<String, Pattern> units = new HashMap<>();
        units.put(PcbPartsSearchField.WATT, Pattern.compile("([0-9.]+/[0-9.]+|[0-9.]+)\\s*([Ww]|watt(s)?|WATT(S)?)\\b", Pattern.CASE_INSENSITIVE));
        units.put(PcbPartsSearchField.TOLERANCE, Pattern.compile("[±]?[0-9.]+(\\s*%)"));
        units.put(PcbPartsSearchField.OHM, Pattern.compile("([0-9.]+/[0-9.]+)?[0-9.]*\\s*(k|m)?(ohm(s)?|Ω)\\b", Pattern.CASE_INSENSITIVE));
        units.put(PcbPartsSearchField.CONDENSER, Pattern.compile("[0-9.]+(?:μF|µF|uF|nF|pF|mF|F)(?![a-zA-Z])", Pattern.CASE_INSENSITIVE));
        units.put(PcbPartsSearchField.VOLTAGE, Pattern.compile("([0-9.]+/[0-9.]+)?[0-9.]*\\s*(V|v|kV|KV|kv|mV|MV|mv|µV|UV|uv|Volt|volt|vdc|VDC|kvdc|KVDC)\\b", Pattern.CASE_INSENSITIVE));
        units.put(PcbPartsSearchField.TEMPERATURE, Pattern.compile("(-?\\d+\\.?\\d*)\\s?(℃|°C)"));
        units.put(PcbPartsSearchField.SIZE, Pattern.compile("((\\d+\\.\\d+|\\d+)([xX*])(\\d+\\.\\d+|\\d+)(([xX*])(\\d+\\.\\d+|\\d+))?)|((\\d+)(?=사이즈))|(\\d+\\.?\\d*mm)", Pattern.CASE_INSENSITIVE));
        units.put(PcbPartsSearchField.INDUCTOR, Pattern.compile("[0-9.]+(?:pH|nH|uH|mH|H)(?![a-zA-Z])", Pattern.CASE_INSENSITIVE));
        units.put(PcbPartsSearchField.CURRENT, Pattern.compile("[0-9.]+(?:uA|µA|mA|A)(?![a-zA-Z])", Pattern.CASE_INSENSITIVE));

        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            boolean matched = false;
            for (Map.Entry<String, Pattern> unit : units.entrySet()) {
                Matcher matcher = unit.getValue().matcher(token);
                if (matcher.find()) {
                    matched = true;
                    List<String> classification = classifications.getOrDefault(unit.getKey(), new ArrayList<>());
                    if (unit.getKey().equals(PcbPartsSearchField.TEMPERATURE)) {
                        classification.add(matcher.group(1));
                    } else {
                        classification.add(matcher.group());
                    }
                    classifications.put(unit.getKey(), classification);
                }
            }

            if (!matched) {
                classifications.get(PcbPartsSearchField.PRODUCT_NAME).add(token);
            }
        }

        return classifications;
    }

    public static Map<FaradsUnit, String> convertToFarads(String input) {
        String[] parts = input.split("(?<=\\d)(?=\\D)");
        if (parts.length < 2) {
            return Collections.emptyMap();
        }
        double value = Double.parseDouble(parts[0]);
        FaradsUnit unit = determineUnit(parts[1]);
        if (unit == FaradsUnit.NONE) {
            return Collections.emptyMap();
        }
        // Farads로 변환
        double valueInFarads = convertToFarads(value, unit);

        // 다른 단위로 변환
        double valueInMegafarads = convertFaradsToMegafarads(valueInFarads);
        double valueInKilofarads = convertFaradsToKilofarads(valueInFarads);
        double valueInMillifarads = convertFaradsToMillifarads(valueInFarads);
        double valueInMicrofarads = convertFaradsToMicrofarads(valueInFarads);
        double valueInNanofarads = convertFaradsToNanofarads(valueInFarads);
        double valueInPicofarads = convertFaradsToPicofarads(valueInFarads);

        Map<FaradsUnit, String> result = new HashMap<>();
        result.put(FaradsUnit.FARADS, stringifyDoubleWithConditionalDecimal(valueInFarads) + "F");
        result.put(FaradsUnit.MEGAFARADS, stringifyDoubleWithConditionalDecimal(valueInMegafarads) + "MF");
        result.put(FaradsUnit.KILOFARADS, stringifyDoubleWithConditionalDecimal(valueInKilofarads) + "kF");
        result.put(FaradsUnit.MILLIFARADS, stringifyDoubleWithConditionalDecimal(valueInMillifarads) + "mF");
        result.put(FaradsUnit.MICROFARADS, stringifyDoubleWithConditionalDecimal(valueInMicrofarads) + "uF");
        result.put(FaradsUnit.NANOFARADS, stringifyDoubleWithConditionalDecimal(valueInNanofarads) + "nF");
        result.put(FaradsUnit.PICOFARADS, stringifyDoubleWithConditionalDecimal(valueInPicofarads) + "pF");
        return result;
    }

    private static String stringifyDoubleWithConditionalDecimal(double number) {
        String str;
        if (number == Math.floor(number)) {
            // 정수값이면 소수점 제거
            str = String.format("%.0f", number);
        } else {
            // 소수점이 있는 경우 그대로 문자열로 변환
            str = String.valueOf(number);
        }
        return str;
    }

    private static FaradsUnit determineUnit(String unitStr) {
        switch (unitStr) {
            case "MF":
                return FaradsUnit.MEGAFARADS;
            case "kF":
                return FaradsUnit.KILOFARADS;
            case "mF":
                return FaradsUnit.MILLIFARADS;
            case "uF":
                return FaradsUnit.MICROFARADS;
            case "nF":
                return FaradsUnit.NANOFARADS;
            case "pF":
                return FaradsUnit.PICOFARADS;
            default:
                return FaradsUnit.NONE;
        }
    }

    private static double convertToFarads(double value, FaradsUnit unit) {
        switch (unit) {
            case MEGAFARADS:
                return value * 1_000_000; // MF to F
            case KILOFARADS:
                return value * 1_000; // kF to F
            case MILLIFARADS:
                return value / 1_000; // mF to F
            case MICROFARADS:
                return value / 1_000_000; // μF to F
            case NANOFARADS:
                return value / 1_000_000_000; // nF to F
            case PICOFARADS:
                return value / 1_000_000_000_000L; // pF to F
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    private static double convertFaradsToMegafarads(double farads) {
        return farads / 1_000_000; // F to MF
    }

    private static double convertFaradsToKilofarads(double farads) {
        return farads / 1_000; // F to kF
    }

    private static double convertFaradsToMillifarads(double farads) {
        return farads * 1_000; // F to mF
    }

    private static double convertFaradsToMicrofarads(double farads) {
        return farads * 1_000_000; // F to μF
    }

    private static double convertFaradsToNanofarads(double farads) {
        return farads * 1_000_000_000; // F to nF
    }

    private static double convertFaradsToPicofarads(double farads) {
        return farads * 1_000_000_000_000L; // F to pF
    }
}
