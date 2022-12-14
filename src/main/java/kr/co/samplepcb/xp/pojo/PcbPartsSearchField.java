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

    public enum Status {
        NOT_APPROVED(0), APPROVED(1);

        private final int value;

        Status(int value) {
            this.value = value;
        }
    }

    public static final String PART_NAME  = "partName";
    public static final String SERVICE_TYPE  = "serviceType";
    public static final String LARGE_CATEGORY  = "largeCategory";
    public static final String MEDIUM_CATEGORY  = "mediumCategory";
    public static final String SMALL_CATEGORY  = "smallCategory";
    public static final String MANUFACTURER_NAME  = "manufacturerName";
    public static final String PACKAGING  = "packaging";
    public static final String OFFER_NAME = "offerName";
    public static final String DESCRIPTION  = "description";
    public static final String PARTS_PACKAGING  = "partsPackaging";
    public static final String STATUS = "status";
    public static final String WRITE_DATE = "writeDate";
    public static final String CURRENT_MEMBER_NAME = "currentMemberName";
    public static final String CURRENT_MEMBER_PHONE_NUMBER = "currentMemberPhoneNumber";
    public static final String CURRENT_MEMBER_EMAIL = "currentMemberEmail";
    public static final String CURRENT_MANAGER_NAME = "currentManagerName";
    public static final String CURRENT_MANAGER_PHONE_NUMBER = "currentManagerPhoneNumber";
    public static final String CURRENT_MANAGER_EMAIL = "currentManagerEmail";
    public static final String MEMBER_ID = "memberId";
    public static final String INVENTORY_LEVEL = "inventoryLevel";
    public static final String PRICE1 = "price1";
}
