package com.aplana.sbrf.taxaccounting.service.print.logsystem;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemXlsxReportBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemReportBuilderTest {

    private List<LogSearchResultItem> items = new ArrayList<LogSearchResultItem>();

    @Before
    public void init(){
        LogSearchResultItem item = new LogSearchResultItem();
        FormType type = new FormType();
        DeclarationType declarationType = new DeclarationType();
        declarationType.setName("РНУ");
        type.setName("Сводная");
        TAUser user = new TAUser();
        user.setName("Пользователь");
        user.setLogin("controlBank");
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("первый квартал");
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        reportPeriod.setTaxPeriod(taxPeriod);
        item.setIp("172.15.0.1");
        item.setLogDate(new Date());
        item.setNote("Проверочка!");
        item.setFormKind(FormDataKind.ADDITIONAL);
        item.setFormTypeName(type.getName());
        item.setDepartmentName("Начальный");
        item.setDepartmentName("Подразделение");
        item.setDeclarationTypeName(declarationType.getName());
        item.setRoles("Контролер");
        item.setEvent(FormDataEvent.ADD_ROW);
        item.setUser(user.getLogin());
        item.setReportPeriodName(String.valueOf(taxPeriod.getYear()) + " " + reportPeriod.getName());

        for (int i = 0; i < 130; i++)
            items.add(item);
    }

    @Test
    public void test() throws IOException {
        LogSystemXlsxReportBuilder builder = new LogSystemXlsxReportBuilder(items);
        String reportPath = null;
        try {
            reportPath = builder.createReport();
        } finally {
            assert reportPath != null;
            File file = new File(reportPath);
            file.delete();
        }
    }
}
