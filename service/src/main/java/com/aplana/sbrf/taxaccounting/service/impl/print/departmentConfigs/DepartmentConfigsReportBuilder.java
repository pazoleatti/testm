package com.aplana.sbrf.taxaccounting.service.impl.print.departmentConfigs;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;

public class DepartmentConfigsReportBuilder extends AbstractReportBuilder {
    private final static int DATA_ROW_INDEX = 1;

    // Записи настроек подразделений
    private List<DepartmentConfig> departmentConfigs;
    // Фильтр настроек подразделений
    private DepartmentConfigsFilter filter;
    private int curRowIndex;
    private Department department;

    public DepartmentConfigsReportBuilder(List<DepartmentConfig> departmentConfigs, DepartmentConfigsFilter filter, Department department) {
        super("tmp_настройки_подразделений_", ".xlsx");
        this.departmentConfigs = departmentConfigs;
        this.filter = filter;
        this.department = department;

        workBook = new SXSSFWorkbook();
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);

        this.sheet = workBook.createSheet(StringUtils.substring(department.getShortName(), 0, 31));
        sheet.setRowSumsBelow(false);
    }

    @Override
    protected void fillHeader() {
    }

    @Override
    protected void createTableHeaders() {
    }

    @Override
    protected void createDataForTable() {
    }

    @Override
    protected void fillFooter() {
    }
}
