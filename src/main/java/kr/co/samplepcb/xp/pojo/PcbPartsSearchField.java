package kr.co.samplepcb.xp.pojo;

import java.util.HashMap;
import java.util.Map;

public class PcbPartsSearchField {

    public static final String[] PCB_PART_COLUMN_IDX_TARGET = new String[] {"",
            PcbPartsSearchField.LARGE_CATEGORY,
            PcbPartsSearchField.MEDIUM_CATEGORY,
            PcbPartsSearchField.SMALL_CATEGORY,
            PcbPartsSearchField.MANUFACTURER_NAME,
            PcbPartsSearchField.PACKAGING,
            PcbPartsSearchField.OFFER_NAME,
            PcbPartsSearchField.PARTS_PACKAGING
    };
    public static final Map<String, Integer> PCB_PART_TARGET_IDX_COLUMN = new HashMap<>();
    static {
        for (int i = 1; i < PCB_PART_COLUMN_IDX_TARGET.length; i++) {
            PCB_PART_TARGET_IDX_COLUMN.put(PCB_PART_COLUMN_IDX_TARGET[i], i);
        }
    }

    public static final String PART_NAME  = "partName";
    public static final String LARGE_CATEGORY  = "largeCategory";
    public static final String MEDIUM_CATEGORY  = "mediumCategory";
    public static final String SMALL_CATEGORY  = "smallCategory";
    public static final String MANUFACTURER_NAME  = "manufacturerName";
    public static final String PACKAGING  = "packaging";
    public static final String OFFER_NAME = "offerName";
    public static final String DESCRIPTION  = "description";
    public static final String PARTS_PACKAGING  = "partsPackaging";
}
