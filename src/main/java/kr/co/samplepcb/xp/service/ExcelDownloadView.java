package kr.co.samplepcb.xp.service;

import coolib.util.CCDateUtils;
import kr.co.samplepcb.xp.pojo.PcbItemSearchVM;
import kr.co.samplepcb.xp.pojo.PcbKindSearchVM;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static kr.co.samplepcb.xp.service.PcbItemService.PCB_ITEM_TARGET_NAMES;

@Component(ExcelDownloadView.VIEW_NAME)
public class ExcelDownloadView extends AbstractXlsxView {

    public static final String VIEW_NAME = "excelDownloadView";
    public static final String ALL_ITEM_GROUP_BY_TARGET = "allItemGroupByTarget";
    public static final String ALL_KIND_GROUP_BY_TARGET = "allKindGroupByTarget";
    public static final String ALL_KIND_GROUP_BY_TARGET_FOR_CATEGORY = "allKindGroupByTargetForCategory";
    public static final String ALL_PARTS = "allParts";

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object itemGroupByTargetObj = model.get(ALL_ITEM_GROUP_BY_TARGET);
        if(itemGroupByTargetObj != null) {
            List<List<PcbItemSearchVM>> pcbItemLists = (List<List<PcbItemSearchVM>>) itemGroupByTargetObj;
            this.makeBomItemList(pcbItemLists, workbook, response);
        }
        Object kingGroupByTargetObj = model.get(ALL_KIND_GROUP_BY_TARGET);
        if(kingGroupByTargetObj != null) {
            List<List<PcbKindSearchVM>> pcbKindLists = (List<List<PcbKindSearchVM>>) kingGroupByTargetObj;
            this.makeKindList(pcbKindLists, workbook, response);
        }
        Object kingGroupByTargetObjForCategory = model.get(ALL_KIND_GROUP_BY_TARGET_FOR_CATEGORY);
        if (kingGroupByTargetObjForCategory != null) {
            List<List<PcbKindSearchVM>> pcbKindLists = (List<List<PcbKindSearchVM>>) kingGroupByTargetObjForCategory;
            this.makeKindListForCategory(pcbKindLists, workbook, response);
        }
        Object partsListObj = model.get(ALL_PARTS);
        if(partsListObj != null) {
            List partsList = (List) partsListObj;
            this.makePartsList(partsList, workbook, response);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private void makeBomItemList(List<List<PcbItemSearchVM>> pcbItemLists, Workbook workbook, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"samplepcb_bom.xlsx\"");

        for (List<PcbItemSearchVM> pcbItemList : pcbItemLists) {
            if(CollectionUtils.isEmpty(pcbItemList)) {
                continue;
            }
            Sheet sheet = workbook.createSheet(PCB_ITEM_TARGET_NAMES[pcbItemList.get(0).getTarget()]);
            for (int j = 0; j < pcbItemList.size(); j++) {
                Row row = sheet.createRow(j);
                PcbItemSearchVM pcbItem = pcbItemList.get(j);
                row.createCell(0).setCellValue(pcbItem.getItemName());
                row.createCell(1).setCellValue(pcbItem.getTarget());
            }
        }
    }

    private void makeKindList(List<List<PcbKindSearchVM>> pcbKindLists, Workbook workbook, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"samplepcb_parts_kind.xlsx\"");

        String[] targetName = {"", "1. 대분류", "2. 중분류", "3. 소분류", "4. 제조사", "5. 포장단위", "6. 공급업체", "7. 부품패키지"};

        for (List<PcbKindSearchVM> pcbKindList : pcbKindLists) {
            if(CollectionUtils.isEmpty(pcbKindList)) {
                continue;
            }
            Sheet sheet = workbook.createSheet(targetName[pcbKindList.get(0).getTarget()]);
            for (int j = 0; j < pcbKindList.size(); j++) {
                Row row = sheet.createRow(j);
                PcbKindSearchVM pcbItem = pcbKindList.get(j);
                row.createCell(0).setCellValue(pcbItem.getId());
                row.createCell(1).setCellValue(pcbItem.getItemName());
            }
        }
    }

    private void makeKindListForCategory(List<List<PcbKindSearchVM>> pcbKindLists, Workbook workbook, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"samplepcb_parts_kind_for_category.xlsx\"");

        // todo: 구현필요
//        Sheet sheet = workbook.createSheet("카테고리 분류");
//        for (int i = 0; i < pcbKindLists.size(); i++) {
//            List<PcbKindSearchVM> pcbKindList = pcbKindLists.get(i);
//            if (CollectionUtils.isEmpty(pcbKindList)) {
//                continue;
//            }
//            for (int j = 0; j < pcbKindList.size(); j++) {
//                Row row = sheet.createRow(j);
//                PcbKindSearchVM pcbItem = pcbKindList.get(j);
//                row.createCell(0).setCellValue(pcbItem.getId());
//                row.createCell(1).setCellValue(pcbItem.getItemName());
//            }
//        }
    }

    @SuppressWarnings("rawtypes")
    private void makePartsList(List<Map> partsList, Workbook workbook, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"samplepcb_parts_list_" + CCDateUtils.getSimpleToday() + ".xlsx\"");

        String[] columnNames = {"식별값","대분류", "중분류", "소분류", "모델명", "제품사양", "제조사", "부품패키지", "포장단위", "최소판매수량", "단가(1~10)", "단가(11~50)", "단가(51~100)", "단가(101~500)", "단가(501~1000)", "현재고", "상품세부정보", "공급업체", "담당자 연락처", "담당자명", "담당자 이메일"};

        Sheet sheet = workbook.createSheet("parts");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnNames.length; i++) {
            headerRow.createCell(i).setCellValue(columnNames[i]);
        }
        for (int i = 0; i < partsList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map parts = partsList.get(i);
            createCellByMap(row, 0, parts, "id");
            createCellByMap(row, 1, parts, "largeCategory");
            createCellByMap(row, 2, parts, "mediumCategory");
            createCellByMap(row, 3, parts, "smallCategory");
            createCellByMap(row, 4, parts, "partName");
            createCellByMap(row, 5, parts, "description");
            createCellByMap(row, 6, parts, "manufacturerName");
            createCellByMap(row, 7, parts, "partsPackaging");
            createCellByMap(row, 8, parts, "packaging");
            createCellByMap(row, 9, parts, "moq");
            createCellByMap(row, 10, parts, "price1");
            createCellByMap(row, 11, parts, "price2");
            createCellByMap(row, 12, parts, "price3");
            createCellByMap(row, 13, parts, "price4");
            createCellByMap(row, 14, parts, "price5");
            createCellByMap(row, 15, parts, "inventoryLevel");
            createCellByMap(row, 16, parts, "memo");
            createCellByMap(row, 17, parts, "offerName");
            createCellByMap(row, 18, parts, "memberId");
        }
    }

    @SuppressWarnings("rawtypes")
    private void createCellByMap(Row refRow, int column, Map map, String key) {
        if(map.get(key) instanceof Number) {
            refRow.createCell(column).setCellValue((Integer) map.get(key));
        } else {
            refRow.createCell(column).setCellValue((String) map.get(key));
        }
    }

}
