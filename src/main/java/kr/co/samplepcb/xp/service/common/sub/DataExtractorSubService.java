package kr.co.samplepcb.xp.service.common.sub;

import coolib.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

@Service
public class DataExtractorSubService {
    private static final Logger log = LoggerFactory.getLogger(DataExtractorSubService.class);

    static List<String> sizes;
    List<String> numericSizes;
    List<String> otherSizes;

    static {
        try {
            InputStream is = DataExtractorSubService.class.getClassLoader().getResourceAsStream("partsPackageOnly.json");
            if (is == null) throw new FileNotFoundException("File not found!");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String content = scanner.useDelimiter("\\A").next();

            String[] sizeArray = CommonUtils.getObjectMapper().readValue(content, String[].class);
            sizes = new ArrayList<>();
            for (String size : sizeArray) {
                if (size.length() > 1) {
                    sizes.add(size);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public DataExtractorSubService() {
        splitSizes();
    }

    private void splitSizes() {
        Pattern numPattern = Pattern.compile("\\d+(\\.\\d+)?");
        numericSizes = new ArrayList<>();
        otherSizes = new ArrayList<>();
        for (String size : sizes) {
            if (numPattern.matcher(size).matches()) {
                numericSizes.add(size);
            } else {
                otherSizes.add(size);
            }
        }
    }

    public String extractSizeFromTitle(Object titles) {
        List<String> titleWords = new ArrayList<>();
        if (titles instanceof List<?>) {
            for (Object title : (List<?>) titles) {
                titleWords.add(((String) title).toLowerCase());
            }
        } else if (titles instanceof String) {
            titleWords = Arrays.asList(((String) titles).toLowerCase().split(" "));
        } else {
            throw new IllegalArgumentException("titles should be either a string or a list");
        }

        for (String size : numericSizes) {
            for (String word : titleWords) {
                if (word.contains(size)) {
                    return size;
                }
            }
        }

        for (String size : otherSizes) {
            if (titleWords.contains(size.toLowerCase())) {
                return size;
            }
        }

        return null;
    }
}
