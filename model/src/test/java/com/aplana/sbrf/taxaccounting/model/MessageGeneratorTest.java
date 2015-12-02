package com.aplana.sbrf.taxaccounting.model;


import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class MessageGeneratorTest {

    private static final String FORM_TYPE_NAME = "(724.1) Отчет о суммах начисленного НДС по операциям Банка";
    private static final Calendar CALENDAR = Calendar.getInstance();

    @Test
    public void getFDMsgTest(){
        FormData formData = new FormData();
        formData.setAccruing(true);
        FormType type = new FormType();
        type.setName(FORM_TYPE_NAME);
        formData.setFormType(type);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setPeriodOrder(1);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("первый квартал");
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setTaxType(TaxType.INCOME);
        taxPeriod.setYear(2015);
        reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setAccName("четвертый квартал (год)");
        DepartmentReportPeriod drPeriod = new DepartmentReportPeriod();
        CALENDAR.set(2015, Calendar.JANUARY, 1);
        drPeriod.setCorrectionDate(CALENDAR.getTime());
        drPeriod.setReportPeriod(reportPeriod);
        DepartmentReportPeriod drPeriodCompare = new DepartmentReportPeriod();
        drPeriodCompare.setCorrectionDate(CALENDAR.getTime());
        drPeriodCompare.setReportPeriod(reportPeriod);

        Assert.assertEquals(
                "Существует экземпляр формы: Тип: \"Консолидированная\", Вид: \"(724.1) Отчет о суммах начисленного НДС по операциям Банка\", Подразделение: \"Байкальский банк\", Период: \"четвертый квартал (год) 2015\", Период сравнения: \"четвертый квартал (год) 2015\", Месяц: Январь, Дата сдачи корректировки: 01.01.2015, Версия: \"Абсолютные значения\"",
                MessageGenerator.getFDMsg("Существует экземпляр формы:", formData, "Байкальский банк", true, drPeriod, drPeriodCompare));
    }
}
