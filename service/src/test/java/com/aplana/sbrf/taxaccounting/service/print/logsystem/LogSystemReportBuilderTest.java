package com.aplana.sbrf.taxaccounting.service.print.logsystem;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemXlsxReportBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemReportBuilderTest {

    private List<LogSystemSearchResultItem> items = new ArrayList<LogSystemSearchResultItem>();

    @Before
    public void init(){
        LogSystemSearchResultItem item = new LogSystemSearchResultItem();
        FormType type = new FormType();
        Department department = new Department();
        department.setName("Начальный");
        DeclarationType declarationType = new DeclarationType();
        declarationType.setName("РНУ");
        type.setName("Сводная");
        TAUser user = new TAUser();
        user.setName("Пользователь");
        user.setLogin("controlBank");
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("1 квартал");
        item.setIp("172.15.0.1");
        item.setLogDate(new Date());
        item.setNote("Проверочка!");
        item.setFormKind(FormDataKind.ADDITIONAL);
        item.setFormType(type);
        item.setDepartment(department);
        item.setDeclarationType(declarationType);
        item.setRoles("Контролер");
        item.setEvent(FormDataEvent.ADD_ROW);
        item.setUser(user);
        item.setReportPeriod(reportPeriod);

        for (int i = 0; i < 130; i++)
            items.add(item);
    }

    @Test
    public void test() throws IOException {
        LogSystemXlsxReportBuilder builder = new LogSystemXlsxReportBuilder(items);
        builder.createReport();
    }
}
