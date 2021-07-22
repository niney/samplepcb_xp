package kr.co.samplepcb.xp.service.common.sub;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.springframework.stereotype.Service;

@Service
public class ExcelSubService {

    /**
     * 엑셀 row 문자형 데이터 가져오기
     * @param row 로우
     * @param columnIndex 인덱스
     * @return 문자 데이터
     */
    public String getCellStrValue(XSSFRow row, int columnIndex) {
        String valueStr = "";
        XSSFCell cell = row.getCell(columnIndex); // 셀에 담겨있는 값을 읽는다.
        if(cell == null) {
            return "";
        }
        switch (cell.getCellType()) { // 각 셀에 담겨있는 데이터의 타입을 체크하고 해당 타입에 맞게 가져온다.
            case NUMERIC:
                valueStr = Integer.toString((int) cell.getNumericCellValue());
                break;
            case STRING:
                valueStr = cell.getStringCellValue() + "";
                break;
            case BLANK:
                valueStr = cell.getStringCellValue() + "";
                break;
            case ERROR:
                valueStr = cell.getErrorCellValue() + "";
                break;
        }
        return valueStr;
    }

    /**
     * 엑셀 row 숫자형 데이터 가져오기
     * @param row 로우
     * @param columnIndex 인덱스
     * @return 숫자 데이터
     */
    public Number getCellNumberValue(XSSFRow row, int columnIndex) {
        Number value = 0;
        XSSFCell cell = row.getCell(columnIndex); // 셀에 담겨있는 값을 읽는다.
        if(cell == null) {
            return 0;
        }
        switch (cell.getCellType()) { // 각 셀에 담겨있는 데이터의 타입을 체크하고 해당 타입에 맞게 가져온다.
            case NUMERIC:
                value = cell.getNumericCellValue();
                break;
            case STRING:
            case BLANK:
            case ERROR:
                value = 0;
                break;
        }
        return value;
    }
}
