package kr.co.samplepcb.xp.util;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

public class XssSanitizerUtil {

    private static final List<Pattern> XSS_INPUT_PATTERNS = new ArrayList<>();
    private static List<Pattern> XSS_INPUT_PATTERNS_WITH_IFRAME;

    static {
        // Avoid anything between script tags
        XSS_INPUT_PATTERNS.add(Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE));

        // avoid iframes
       /* XSS_INPUT_PATTERNS.add(Pattern.compile("<iframe(.*?)>(.*?)</iframe>", Pattern.CASE_INSENSITIVE));

        // Avoid anything in a src='...' type of expression
        XSS_INPUT_PATTERNS.add(Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

        XSS_INPUT_PATTERNS.add(Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

        XSS_INPUT_PATTERNS.add(Pattern.compile("src[\r\n]*=[\r\n]*([^>]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));*/

        // Remove any lonesome </script> tag
        XSS_INPUT_PATTERNS.add(Pattern.compile("</script>", Pattern.CASE_INSENSITIVE));

        // Remove any lonesome <script ...> tag
        XSS_INPUT_PATTERNS.add(Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

        // Avoid eval(...) expressions
        XSS_INPUT_PATTERNS.add(Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

        // Avoid expression(...) expressions
        XSS_INPUT_PATTERNS.add(Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

        // Avoid javascript:... expressions
        XSS_INPUT_PATTERNS.add(Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE));

        // Avoid vbscript:... expressions
        XSS_INPUT_PATTERNS.add(Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE));

        // Avoid onload= expressions
        XSS_INPUT_PATTERNS.add(Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
    }

    /**
     * This method takes a string and strips out any potential script injections.
     *
     * @param value
     * @return String - the new "sanitized" string.
     */
    @SuppressWarnings({"Duplicates", "deprecation"})
    public static String stripXSS(String value) {

        try {

            if (value != null) {
                // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
                // avoid encoded attacks.
//                 value = ESAPI.encoder().canonicalize(value);
                value = Jsoup.parse(value).html();
                // 개행문자
                value = value.replaceAll("\n", "");

                // Avoid null characters
                value = value.replaceAll("\0", "");

                // test against known XSS input patterns
                for (Pattern xssInputPattern : XSS_INPUT_PATTERNS) {
                    value = xssInputPattern.matcher(value).replaceAll("");
                }
            }

        } catch (Exception ex) {
            System.out.println("Could not strip XSS from value = " + value + " | ex = " + ex.getMessage());
        }

        return value;
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    public static String stripXSSWithIframe(String value) {
        if (XSS_INPUT_PATTERNS_WITH_IFRAME == null) {
            XSS_INPUT_PATTERNS_WITH_IFRAME = (List<Pattern>) ((ArrayList) XSS_INPUT_PATTERNS).clone();
            XSS_INPUT_PATTERNS_WITH_IFRAME.add(Pattern.compile("<iframe(.*?)>(.*?)</iframe>", Pattern.CASE_INSENSITIVE));
        }
        try {
            if (value != null) {
                value = value.replaceAll("\0", "");

                for (Pattern xssInputPattern : XSS_INPUT_PATTERNS_WITH_IFRAME) {
                    value = xssInputPattern.matcher(value).replaceAll("");
                }
            }

        } catch (Exception ex) {
            System.out.println("Could not strip XSS from value = " + value + " | ex = " + ex.getMessage());
        }

        return value;
    }
}
