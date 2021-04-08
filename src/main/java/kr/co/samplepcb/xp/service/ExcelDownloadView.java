package kr.co.samplepcb.xp.service;

import kr.co.samplepcb.xp.pojo.PcbItemSearchVM;
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

@Component(ExcelDownloadView.VIEW_NAME)
public class ExcelDownloadView extends AbstractXlsxView {

    public static final String VIEW_NAME = "excelDownloadView";
    public static final String ALL_ITEM_GROUP_BY_TARGET = "allItemGroupByTarget";

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<List<PcbItemSearchVM>> pcbItemLists = (List<List<PcbItemSearchVM>>) model.get(ALL_ITEM_GROUP_BY_TARGET);
        if (pcbItemLists != null) {
            this.makeBomItemList(pcbItemLists, workbook, response);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private void makeBomItemList(List<List<PcbItemSearchVM>> pcbItemLists, Workbook workbook, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"samplepcb_bom.xlsx\"");

        String[] targetName = {"", "Reference", "Part Number", "Description", "Qty", "Manufacturer", "Package", "Current", "W", "Value", "Tolerance", "Voltage", "datasheet", "item"};

        for (List<PcbItemSearchVM> pcbItemList : pcbItemLists) {
            if(CollectionUtils.isEmpty(pcbItemList)) {
                continue;
            }
            Sheet sheet = workbook.createSheet(targetName[pcbItemList.get(0).getTarget()]);
            for (int j = 0; j < pcbItemList.size(); j++) {
                Row row = sheet.createRow(j);
                PcbItemSearchVM pcbItem = pcbItemList.get(j);
                row.createCell(0).setCellValue(pcbItem.getItemName());
                row.createCell(1).setCellValue(pcbItem.getTarget());
            }
        }
    }

}
