package com.aplana.sbrf.taxaccounting.service.impl.print.departmentConfigs;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPresentPlace;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookReorganization;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSignatoryMark;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.service.impl.print.departmentConfigs.StyleBuilder.CellType;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Формирует excel файл с настройками подразделений
 */
public class DepartmentConfigsReportBuilder extends AbstractReportBuilder {
    // начальная строка с данными
    private final static int DATA_ROW_INDEX = 1;

    // Записи настроек подразделений
    private List<DepartmentConfig> departmentConfigs;
    // Отображаемые столбцы
    private List<String> header = asList("Дата начала действия настройки", "Дата окончания действия настройки", "КПП", "ОКТМО", "Код НО (конечного)", "Код по месту представления",
            "Наименование для титульного листа", "Контактный телефон", "Признак подписанта", "Фамилия подписанта", "Имя подписанта",
            "Отчество подписанта", "Документ полномочий подписанта", "Код формы реорганизации", "КПП реорганизованной организации", "ИНН реорганизованной организации",
            "КПП подразделения правопреемника", "Наименование подразделения правопреемника");

    private int curRowIndex;
    private StyleBuilder styleBuilder;

    public DepartmentConfigsReportBuilder(List<DepartmentConfig> departmentConfigs, Department department) {
        super("tmp_настройки_подразделений_", ".xlsx");
        this.departmentConfigs = departmentConfigs;

        workBook = new SXSSFWorkbook();
        styleBuilder = new StyleBuilder(workBook);
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);

        this.sheet = workBook.createSheet(StringUtils.substring(department.getShortName(), 0, 31));
        sheet.setRowSumsBelow(false);
    }

    @Override
    protected void cellAlignment() {
        List<Integer> widthList = Arrays.asList(16, 16, 11, 12, 8, 8, 25, 22, 12, 20, 20, 20, 34, 15, 15, 15, 15, 25);
        for (int i = 0; i < header.size(); i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, widthList.get(i) * 269);
        }
    }

    @Override
    protected void fillHeader() {
    }

    @Override
    protected void createTableHeaders() {
        CellStyle style = styleBuilder.getTableHeaderStyle();
        Row row6 = sheet.createRow(0);
        for (int colIndex = 0; colIndex < header.size(); colIndex++) {
            Cell cell = row6.createCell(colIndex);
            cell.setCellValue(header.get(colIndex));
            cell.setCellStyle(style);
        }
    }

    @Override
    protected void createDataForTable() {
        curRowIndex = DATA_ROW_INDEX;
        for (DepartmentConfig departmentConfig : departmentConfigs) {
            createRowValues(departmentConfig);
            curRowIndex++;
        }
    }

    private void createRowValues(DepartmentConfig departmentConfig) {
        sheet.createRow(curRowIndex);
        int colIndex = 0;
        createCellValue(colIndex++, departmentConfig.getStartDate(), "startDate", CellType.DATE, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getEndDate(), "endDate", CellType.DATE, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getKpp(), "kpp", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, toString(departmentConfig.getOktmo()), "oktmo", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getTaxOrganCode(), "taxOrganCode", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, toString(departmentConfig.getPresentPlace()), "presentPlace", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getName(), "name", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, departmentConfig.getPhone(), "phone", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, toNumber(departmentConfig.getSignatoryMark()), "signatoryMark", CellType.INTEGER, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getSignatorySurName(), "signatorySurName", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, departmentConfig.getSignatoryFirstName(), "signatoryFirstName", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, departmentConfig.getSignatoryLastName(), "signatoryLastName", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, departmentConfig.getApproveDocName(), "approveDocName", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, toString(departmentConfig.getReorganization()), "reorganizationCode", CellType.INTEGER, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getReorgKpp(), "reorgKpp", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getReorgInn(), "reorgInn", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getReorgSuccessorKpp(), "reorgSuccessorKpp", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, departmentConfig.getReorgSuccessorName(), "reorgSuccessorName", CellType.STRING, CellStyle.ALIGN_LEFT);
    }

    private String toString(RefBookOktmo oktmo) {
        return oktmo != null ? oktmo.getCode() : null;
    }

    private String toString(RefBookPresentPlace presentPlace) {
        return presentPlace != null ? presentPlace.getCode() : null;
    }

    private Number toNumber(RefBookSignatoryMark signatoryMark) {
        return signatoryMark != null ? signatoryMark.getCode() : null;
    }

    private String toString(RefBookReorganization reorganization) {
        return reorganization != null ? reorganization.getCode() : null;
    }

    private Cell createCellValue(int colIndex, Object value, String propName, CellType cellType, short align) {
        Cell cell = sheet.getRow(curRowIndex).createCell(colIndex);
        if (value != null) {
            switch (cellType) {
                case STRING:
                    if (!isEmpty(value.toString())) {
                        cell.setCellValue(value.toString());
                    }
                    break;
                case INTEGER:
                    Number number = value instanceof String ? Integer.valueOf((String) value) : (Number) value;
                    cell.setCellValue(number.doubleValue());
                    break;
                case DATE:
                    cell.setCellValue((Date) value);
                    break;
            }
        }
        cell.setCellStyle(styleBuilder.getCellStyle(propName, cellType, align));
        return cell;
    }

    @Override
    protected void fillFooter() {
    }
}
