package kr.co.samplepcb.xp.pojo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    public static final String WATT = "watt";
    public static final String WATT_FIELD1 = "watt.field1";
    public static final String WATT_FIELD2 = "watt.field2";
    public static final String WATT_FIELD3 = "watt.field3";
    public static final String WATT_FIELD4 = "watt.field4";
    public static final String WATT_FIELD5 = "watt.field5";
    public static final List<String> WATT_LIST = Arrays.asList(WATT_FIELD1, WATT_FIELD2, WATT_FIELD3, WATT_FIELD4, WATT_FIELD5);
    public static final String TOLERANCE = "tolerance";
    public static final String TOLERANCE_FIELD1 = "tolerance.field1";
    public static final String TOLERANCE_FIELD2 = "tolerance.field2";
    public static final String TOLERANCE_FIELD3 = "tolerance.field3";
    public static final String TOLERANCE_FIELD4 = "tolerance.field4";
    public static final String TOLERANCE_FIELD5 = "tolerance.field5";
    public static final List<String> TOLERANCE_LIST = Arrays.asList(TOLERANCE_FIELD1, TOLERANCE_FIELD2, TOLERANCE_FIELD3, TOLERANCE_FIELD4, TOLERANCE_FIELD5);
    public static final String OHM = "ohm";
    public static final String OHM_FIELD1 = "ohm.field1";
    public static final String OHM_FIELD2 = "ohm.field2";
    public static final String OHM_FIELD3 = "ohm.field3";
    public static final String OHM_FIELD4 = "ohm.field4";
    public static final String OHM_FIELD5 = "ohm.field5";
    public static final List<String> OHM_LIST = Arrays.asList(OHM_FIELD1, OHM_FIELD2, OHM_FIELD3, OHM_FIELD4, OHM_FIELD5);
    public static final String CONDENSER = "condenser";
    public static final String CONDENSER_FIELD1 = "condenser.field1";
    public static final String CONDENSER_FIELD2 = "condenser.field2";
    public static final String CONDENSER_FIELD3 = "condenser.field3";
    public static final String CONDENSER_FIELD4 = "condenser.field4";
    public static final String CONDENSER_FIELD5 = "condenser.field5";
    public static final List<String> CONDENSER_LIST = Arrays.asList(CONDENSER_FIELD1, CONDENSER_FIELD2, CONDENSER_FIELD3, CONDENSER_FIELD4, CONDENSER_FIELD5);
    public static final String VOLTAGE = "voltage";
    public static final String VOLTAGE_FIELD1 = "voltage.field1";
    public static final String VOLTAGE_FIELD2 = "voltage.field2";
    public static final String VOLTAGE_FIELD3 = "voltage.field3";
    public static final String VOLTAGE_FIELD4 = "voltage.field4";
    public static final String VOLTAGE_FIELD5 = "voltage.field5";
    public static final List<String> VOLTAGE_LIST = Arrays.asList(VOLTAGE_FIELD1, VOLTAGE_FIELD2, VOLTAGE_FIELD3, VOLTAGE_FIELD4, VOLTAGE_FIELD5);
    public static final String TEMPERATURE = "temperature";
    public static final String SIZE = "size";
    public static final String CURRENT = "current";
    public static final String CURRENT_FIELD1 = "current.field1";
    public static final String CURRENT_FIELD2 = "current.field2";
    public static final String CURRENT_FIELD3 = "current.field3";
    public static final String CURRENT_FIELD4 = "current.field4";
    public static final String CURRENT_FIELD5 = "current.field5";
    public static final List<String> CURRENT_LIST = Arrays.asList(CURRENT_FIELD1, CURRENT_FIELD2, CURRENT_FIELD3, CURRENT_FIELD4, CURRENT_FIELD5);
    public static final String INDUCTOR = "inductor";
    public static final String INDUCTOR_FIELD1 = "inductor.field1";
    public static final String INDUCTOR_FIELD2 = "inductor.field2";
    public static final String INDUCTOR_FIELD3 = "inductor.field3";
    public static final String INDUCTOR_FIELD4 = "inductor.field4";
    public static final String INDUCTOR_FIELD5 = "inductor.field5";
    public static final List<String> INDUCTOR_LIST = Arrays.asList(INDUCTOR_FIELD1, INDUCTOR_FIELD2, INDUCTOR_FIELD3, INDUCTOR_FIELD4, INDUCTOR_FIELD5);
    public static final String PRODUCT_NAME = "productName";
}
