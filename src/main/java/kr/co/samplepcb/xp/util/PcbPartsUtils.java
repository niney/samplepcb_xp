package kr.co.samplepcb.xp.util;

import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PcbPartsUtils {

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

    private static String stringifyStringWithConditionalDecimal(String input) {
        double number;
        try {
            number = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return "Invalid input"; // 입력이 유효한 숫자가 아닐 경우
        }

        return stringifyDoubleWithConditionalDecimal(number);
    }

    public static abstract class PcbConvert {
        public enum Unit {
            NONE,
            FARADS, MEGAFARADS, KILOFARADS, MILLIFARADS, MICROFARADS, NANOFARADS, PICOFARADS,
            PERCENT, PERCENT_STRING,
            VOLTS, KILOVOLTS, MILLIVOLTS, MICROVOLTS,
            AMPERES, MILLIAMPERES, MICROAMPERES,
            OHMS, KILOHMS, MEGAOHMS,
            HENRYS, MILLIHENRYS, MICROHENRYS
        }

        abstract Map<Unit, String> convert(String input);

        abstract Unit determineUnit(String unitStr);

        abstract double convert(double value, Unit unit);

    }

    public static class OhmConvert extends PcbConvert {

        @Override
        public Map<Unit, String> convert(String input) {
            String[] parts = input.split("(?<=\\d)(?=\\D)");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            double value = Double.parseDouble(parts[0]);
            Unit unit = determineUnit(parts[1]);
            if (unit == Unit.NONE) {
                return Collections.emptyMap();
            }
            // Convert to Ohms
            double valueInOhms = convert(value, unit);

            // Convert to other units
            double valueInKilohms = convertOhmsToKilohms(valueInOhms);
            double valueInMegaohms = convertOhmsToMegaohms(valueInOhms);

            Map<Unit, String> result = new HashMap<>();
            result.put(Unit.OHMS, stringifyDoubleWithConditionalDecimal(valueInOhms) + "ohm");
            result.put(Unit.KILOHMS, stringifyDoubleWithConditionalDecimal(valueInKilohms) + "kohm");
            result.put(Unit.MEGAOHMS, stringifyDoubleWithConditionalDecimal(valueInMegaohms) + "mohm");

            return result;
        }

        @Override
        Unit determineUnit(String unitStr) {
            unitStr = unitStr.toLowerCase();
            switch (unitStr) {
                case "Ω":
                case "ohm":
                case "ohms":
                    return Unit.OHMS;
                case "kΩ":
                case "kohm":
                case "kohms":
                    return Unit.KILOHMS;
                case "mΩ":
                case "mohm":
                case "mohms":
                    return Unit.MEGAOHMS;
                default:
                    return Unit.NONE;
            }
        }

        @Override
        double convert(double value, Unit unit) {
            switch (unit) {
                case OHMS:
                    return value; // Ω to Ω
                case KILOHMS:
                    return value * 1_000; // kΩ to Ω
                case MEGAOHMS:
                    return value * 1_000_000; // MΩ to Ω
                default:
                    throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        private static double convertOhmsToKilohms(double ohms) {
            return ohms / 1_000; // Ω to kΩ
        }

        private static double convertOhmsToMegaohms(double ohms) {
            return ohms / 1_000_000; // Ω to MΩ
        }
    }

    public static class FaradsConvert extends PcbConvert {

        @Override
        public Map<Unit, String> convert(String input) {
            String[] parts = input.split("(?<=\\d)(?=\\D)");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            double value = Double.parseDouble(parts[0]);
            Unit unit = determineUnit(parts[1]);
            if (unit == Unit.NONE) {
                return Collections.emptyMap();
            }
            // Farads로 변환
            double valueInFarads = convert(value, unit);

            // 다른 단위로 변환
            double valueInMegafarads = convertFaradsToMegafarads(valueInFarads);
            double valueInKilofarads = convertFaradsToKilofarads(valueInFarads);
            double valueInMillifarads = convertFaradsToMillifarads(valueInFarads);
            double valueInMicrofarads = convertFaradsToMicrofarads(valueInFarads);
            double valueInNanofarads = convertFaradsToNanofarads(valueInFarads);
            double valueInPicofarads = convertFaradsToPicofarads(valueInFarads);

            Map<Unit, String> result = new HashMap<>();
            result.put(Unit.FARADS, stringifyDoubleWithConditionalDecimal(valueInFarads) + "F");
            result.put(Unit.MEGAFARADS, stringifyDoubleWithConditionalDecimal(valueInMegafarads) + "MF");
            result.put(Unit.KILOFARADS, stringifyDoubleWithConditionalDecimal(valueInKilofarads) + "kF");
            result.put(Unit.MILLIFARADS, stringifyDoubleWithConditionalDecimal(valueInMillifarads) + "mF");
            result.put(Unit.MICROFARADS, stringifyDoubleWithConditionalDecimal(valueInMicrofarads) + "uF");
            result.put(Unit.NANOFARADS, stringifyDoubleWithConditionalDecimal(valueInNanofarads) + "nF");
            result.put(Unit.PICOFARADS, stringifyDoubleWithConditionalDecimal(valueInPicofarads) + "pF");
            return result;
        }

        @Override
        public Unit determineUnit(String unitStr) {
            unitStr = unitStr.replace("f", "F");
            switch (unitStr) {
                case "MF":
                    return Unit.MEGAFARADS;
                case "kF":
                    return Unit.KILOFARADS;
                case "mF":
                    return Unit.MILLIFARADS;
                case "uF":
                    return Unit.MICROFARADS;
                case "nF":
                    return Unit.NANOFARADS;
                case "pF":
                    return Unit.PICOFARADS;
                default:
                    return Unit.NONE;
            }
        }

        @Override
        public double convert(double value, Unit unit) {
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

        private double convertFaradsToMegafarads(double farads) {
            return farads / 1_000_000; // F to MF
        }

        private double convertFaradsToKilofarads(double farads) {
            return farads / 1_000; // F to kF
        }

        private double convertFaradsToMillifarads(double farads) {
            return farads * 1_000; // F to mF
        }

        private double convertFaradsToMicrofarads(double farads) {
            return farads * 1_000_000; // F to μF
        }

        private double convertFaradsToNanofarads(double farads) {
            return farads * 1_000_000_000; // F to nF
        }

        private double convertFaradsToPicofarads(double farads) {
            return farads * 1_000_000_000_000L; // F to pF
        }
    }

    public static class ToleranceConvert extends PcbConvert {

        @Override
        public Map<Unit, String> convert(String input) {
            Map<Unit, String> result = new HashMap<>();
            String[] parts = input.split("(?<=\\d)(?=\\D)");
            if (StringUtils.isEmpty(parts[0])) {
                return Collections.emptyMap();
            }
            result.put(Unit.PERCENT, stringifyStringWithConditionalDecimal(parts[0]) + "%");
            result.put(Unit.PERCENT_STRING, stringifyStringWithConditionalDecimal(parts[0]) + "percent");

            return result;
        }

        @Override
        Unit determineUnit(String unitStr) {
            return Unit.NONE;
        }

        @Override
        double convert(double value, Unit unit) {
            return 0;
        }
    }

    public static class VoltConvert extends PcbConvert {

        @Override
        public Map<Unit, String> convert(String input) {
            String[] parts = input.split("(?<=\\d)(?=\\D)");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            double value = Double.parseDouble(parts[0]);
            Unit unit = determineUnit(parts[1]);
            if (unit == Unit.NONE) {
                return Collections.emptyMap();
            }

            // Volts로 변환
            double valueInVolts = convert(value, unit);

            // 다른 단위로 변환
            double valueInKilovolts = convertVoltsToKilovolts(valueInVolts);
            double valueInMillivolts = convertVoltsToMillivolts(valueInVolts);
            double valueInMicrovolts = convertVoltsToMicrovolts(valueInVolts);

            Map<Unit, String> result = new HashMap<>();
            result.put(Unit.VOLTS, stringifyDoubleWithConditionalDecimal(valueInVolts) + "V");
            result.put(Unit.KILOVOLTS, stringifyDoubleWithConditionalDecimal(valueInKilovolts) + "kV");
            result.put(Unit.MILLIVOLTS, stringifyDoubleWithConditionalDecimal(valueInMillivolts) + "mV");
            result.put(Unit.MICROVOLTS, stringifyDoubleWithConditionalDecimal(valueInMicrovolts) + "uV");
            return result;
        }

        @Override
        Unit determineUnit(String unitStr) {
            unitStr = unitStr.replace("v", "V");
            switch (unitStr) {
                case "kV":
                    return Unit.KILOVOLTS;
                case "V":
                    return Unit.VOLTS;
                case "mV":
                    return Unit.MILLIVOLTS;
                case "uV":
                    return Unit.MICROVOLTS;
                default:
                    return Unit.NONE;
            }
        }

        @Override
        double convert(double value, Unit unit) {
            switch (unit) {
                case KILOVOLTS:
                    return value * 1_000; // kV to V
                case VOLTS:
                    return value; // V to V
                case MILLIVOLTS:
                    return value / 1_000; // mV to V
                case MICROVOLTS:
                    return value / 1_000_000; // µV to V
                default:
                    throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        private static double convertVoltsToKilovolts(double volts) {
            return volts / 1_000; // V to kV
        }

        private static double convertVoltsToMillivolts(double volts) {
            return volts * 1_000; // V to mV
        }

        private static double convertVoltsToMicrovolts(double volts) {
            return volts * 1_000_000; // V to µV
        }
    }

    public static class CurrentConvert extends PcbConvert {

        @Override
        public Map<Unit, String> convert(String input) {
            String[] parts = input.split("(?<=\\d)(?=\\D)");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            double value = Double.parseDouble(parts[0]);
            Unit unit = determineUnit(parts[1]);
            if (unit == Unit.NONE) {
                return Collections.emptyMap();
            }

            // Amperes로 변환
            double valueInAmperes = convert(value, unit);

            // 다른 단위로 변환
            double valueInMilliamperes = convertAmperesToMilliamperes(valueInAmperes);
            double valueInMicroamperes = convertAmperesToMicroamperes(valueInAmperes);

            // 결과 출력
            Map<Unit, String> result = new HashMap<>();
            result.put(Unit.AMPERES, stringifyDoubleWithConditionalDecimal(valueInAmperes) + "A");
            result.put(Unit.MILLIAMPERES, stringifyDoubleWithConditionalDecimal(valueInMilliamperes) + "mA");
            result.put(Unit.MICROAMPERES, stringifyDoubleWithConditionalDecimal(valueInMicroamperes) + "uA");
            return result;

        }

        @Override
        Unit determineUnit(String unitStr) {
            unitStr = unitStr.replace("a", "A");
            switch (unitStr) {
                case "A":
                    return Unit.AMPERES;
                case "mA":
                    return Unit.MILLIAMPERES;
                case "uA":
                    return Unit.MICROAMPERES;
                default:
                    return Unit.NONE;
            }
        }

        @Override
        double convert(double value, Unit unit) {
            switch (unit) {
                case AMPERES:
                    return value; // A to A
                case MILLIAMPERES:
                    return value / 1_000; // mA to A
                case MICROAMPERES:
                    return value / 1_000_000; // µA to A
                default:
                    throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        private static double convertAmperesToMilliamperes(double amperes) {
            return amperes * 1_000; // A to mA
        }

        private static double convertAmperesToMicroamperes(double amperes) {
            return amperes * 1_000_000; // A to µA
        }
    }

    public static class InductorConvert extends PcbConvert {

        @Override
        public Map<Unit, String> convert(String input) {
            String[] parts = input.split("(?<=\\d)(?=\\D)");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            double value = Double.parseDouble(parts[0]);
            Unit unit = determineUnit(parts[1]);
            if (unit == Unit.NONE) {
                return Collections.emptyMap();
            }
            // Convert to Henrys
            double valueInHenrys = convert(value, unit);

            // Convert to other units
            double valueInMillihenrys = convertHenrysToMillihenrys(valueInHenrys);
            double valueInMicrohenrys = convertHenrysToMicrohenrys(valueInHenrys);

            // 결과 출력
            Map<Unit, String> result = new HashMap<>();
            result.put(Unit.HENRYS, stringifyDoubleWithConditionalDecimal(valueInHenrys) + "H");
            result.put(Unit.MILLIHENRYS, stringifyDoubleWithConditionalDecimal(valueInMillihenrys) + "mH");
            result.put(Unit.MICROHENRYS, stringifyDoubleWithConditionalDecimal(valueInMicrohenrys) + "uH");
            return result;
        }

        @Override
        Unit determineUnit(String unitStr) {
            switch (unitStr) {
                case "H":
                    return Unit.HENRYS;
                case "mH":
                    return Unit.MILLIHENRYS;
                case "µH":
                    return Unit.MICROHENRYS;
                default:
                    return Unit.NONE;
            }
        }

        @Override
        double convert(double value, Unit unit) {
            switch (unit) {
                case HENRYS:
                    return value; // H to H
                case MILLIHENRYS:
                    return value / 1_000; // mH to H
                case MICROHENRYS:
                    return value / 1_000_000; // µH to H
                default:
                    throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        private static double convertHenrysToMillihenrys(double henrys) {
            return henrys * 1_000; // H to mH
        }

        private static double convertHenrysToMicrohenrys(double henrys) {
            return henrys * 1_000_000; // H to µH
        }
    }

    // 레벤슈타인 거리 계산
    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }

    // 유사도 점수 (백분율)로 변환
    public static double similarityScorePercentage(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) return 100.0; // 두 문자열 모두 비어있는 경우
        return (1.0 - (double) levenshteinDistance(a, b) / maxLength) * 100;
    }


}
