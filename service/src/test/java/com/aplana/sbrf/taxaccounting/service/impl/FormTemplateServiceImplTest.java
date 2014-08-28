package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Fail Mukhametdinov
 */
public class FormTemplateServiceImplTest extends Assert {

    public static final int FORM_TEMPLATE_ID = 1;
    public static final int COLUMN_ID = 1;

    public static final String MESSAGE = "Следующие периоды налоговых форм данной версии макета закрыты: " +
            "год 2014, первый квартал 2015. " +
            "Для добавления в макет автонумеруемой графы с типом сквозной нумерации строк необходимо открыть перечисленные периоды!";

    Logger logger;

    FormTemplate formTemplateFromDB;

    List<ReportPeriod> reportPeriodList;

    ReportPeriodDao reportPeriodDao;

    FormTemplateDao formTemplateDao;

    FormTemplate formTemplateEdited;

    FormTemplateService formTemplateService = new FormTemplateServiceImpl();

    @Before
    public void init() {
        logger = new Logger();

        // Макет, который "хранится" в БД. До редактирования
        formTemplateFromDB = new FormTemplate();
        formTemplateFromDB.setId(FORM_TEMPLATE_ID);

        AutoNumerationColumn autoNumerationColumn = new AutoNumerationColumn();
        autoNumerationColumn.setId(COLUMN_ID);
        autoNumerationColumn.setTypeName(AutoNumerationColumnType.SERIAL.getName());
        autoNumerationColumn.setType(AutoNumerationColumnType.SERIAL.getType());
        formTemplateFromDB.addColumn(autoNumerationColumn);

        // Закрытые отчетные периоды
        reportPeriodList = new ArrayList<ReportPeriod>();

        ReportPeriod reportPeriod1 = new ReportPeriod();
        reportPeriod1.setName("год");
        TaxPeriod taxPeriod1 = new TaxPeriod();
        taxPeriod1.setYear(2014);
        reportPeriod1.setTaxPeriod(taxPeriod1);
        reportPeriodList.add(reportPeriod1);

        ReportPeriod reportPeriod2 = new ReportPeriod();
        reportPeriod2.setName("первый квартал");
        TaxPeriod taxPeriod2 = new TaxPeriod();
        taxPeriod2.setYear(2015);
        reportPeriod2.setTaxPeriod(taxPeriod2);
        reportPeriodList.add(reportPeriod2);

        // Версия макета до редактирования
        formTemplateDao = mock(FormTemplateDao.class);
        when(formTemplateDao.get(FORM_TEMPLATE_ID)).thenReturn(formTemplateFromDB);

        reportPeriodDao = mock(ReportPeriodDao.class);

        formTemplateEdited = SerializationUtils.clone(formTemplateFromDB);

        ReflectionTestUtils.setField(formTemplateService, "formTemplateDao", formTemplateDao);
    }

    /**
     * Значение атрибута "Тип нумерации строк" ни для одной автонумеруемой графы макета не изменено на "Сквозная".
     */
    @Test
    public void validateFormAutoNumerationColumn_notCross() {
        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger);
        assertTrue(logger.getEntries().size() == 0);
    }

    /**
     * В данной версии макета до редактирования не было ни одной автонумеруемой графы,
     * у которой "Тип нумерации строк" = "Сквозная".
     *
     * Отчетный период всех экземпляров НФ данной версии макета открыт.
     */
    @Test
    public void validateFormAutoNumerationColumn_cross1() {
        AutoNumerationColumn autoNumerationColumn = (AutoNumerationColumn) formTemplateEdited.getColumn(1);
        autoNumerationColumn.setTypeName(AutoNumerationColumnType.CROSS.getName());
        autoNumerationColumn.setType(AutoNumerationColumnType.CROSS.getType());

        when(reportPeriodDao.getClosedPeriodsForFormTemplate(FORM_TEMPLATE_ID)).thenReturn(new ArrayList<ReportPeriod>());
        ReflectionTestUtils.setField(formTemplateService, "reportPeriodDao", reportPeriodDao);

        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger);

        assertTrue(logger.getEntries().size() == 0);
    }

    /**
     * В данной версии макета до редактирования не было ни одной автонумеруемой графы,
     * у которой "Тип нумерации строк" = "Сквозная".
     *
     * Отчетный период хотя бы части экземпляров НФ данной версии макета закрыт.
     */
    @Test
    public void validateFormAutoNumerationColumn_cross2() {
        // Редактируемый макет
        AutoNumerationColumn autoNumerationColumn = (AutoNumerationColumn) formTemplateEdited.getColumn(1);
        autoNumerationColumn.setTypeName(AutoNumerationColumnType.CROSS.getName());
        autoNumerationColumn.setType(AutoNumerationColumnType.CROSS.getType());

        when(reportPeriodDao.getClosedPeriodsForFormTemplate(FORM_TEMPLATE_ID)).thenReturn(reportPeriodList);

        ReflectionTestUtils.setField(formTemplateService, "reportPeriodDao", reportPeriodDao);

        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger);

        assertTrue(logger.getEntries().size() == 1);
        assertEquals(MESSAGE, logger.getEntries().get(0).getMessage());
    }

    /**
     * В данной версии макета до редактирования была хотя бы одна автонумеруемая графа,
     * у которой "Тип нумерации строк" = "Сквозная".
     */
    @Test
    public void validateFormAutoNumerationColumn_cross3() {
        // Макет, который "хранится" в БД. До редактирования
        formTemplateFromDB = new FormTemplate();
        formTemplateFromDB.setId(FORM_TEMPLATE_ID);

        AutoNumerationColumn autoNumerationColumn = new AutoNumerationColumn();
        autoNumerationColumn.setId(COLUMN_ID);
        autoNumerationColumn.setTypeName(AutoNumerationColumnType.CROSS.getName());
        autoNumerationColumn.setType(AutoNumerationColumnType.CROSS.getType());
        formTemplateFromDB.addColumn(autoNumerationColumn);

        when(formTemplateDao.get(FORM_TEMPLATE_ID)).thenReturn(formTemplateFromDB);

        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger);

        assertTrue(logger.getEntries().size() == 0);
    }

    /**
     * Нет ни одной автонумеруемой графы
     */
    @Test
    public void testIsAnyAutoNumerationColumnNotExist() {
        FormTemplate formTemplate = new FormTemplate();

        Column numericColumn = new NumericColumn();
        Column stringColumn = new StringColumn();

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(stringColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.SERIAL);
        assertFalse("Не должно быть ни одной сквозной автонумеруемой графы", anyCrossAutoNumerationColumn);
        assertFalse("Не должно быть ни одной последовательной автонумеруемой графы", anySerialAutoNumerationColumn);
    }

    /**
     * Есть одна последовательная автонумеруемая графа
     */
    @Test
    public void testIsAnyAutoNumerationColumnSerialExist() {
        FormTemplate formTemplate = new FormTemplate();

        Column numericColumn = new NumericColumn();
        Column stringColumn = new StringColumn();
        Column autoNumerationColumn = new AutoNumerationColumn(AutoNumerationColumnType.SERIAL.getName(), AutoNumerationColumnType.SERIAL.getType());

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(autoNumerationColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.SERIAL);
        assertFalse("Не должно быть ни одной сквозной автонумеруемой графы", anyCrossAutoNumerationColumn);
        assertTrue("Должна быть хотя бы одна последовательная автонумеруемая графа", anySerialAutoNumerationColumn);
    }

    /**
     * Есть одна сквозная автонумеруемая графа
     */
    @Test
    public void testIsAnyAutoNumerationColumnCrossExist() {
        FormTemplate formTemplate = new FormTemplate();

        Column numericColumn = new NumericColumn();
        Column stringColumn = new StringColumn();
        Column autoNumerationColumn = new AutoNumerationColumn(AutoNumerationColumnType.CROSS.getName(), AutoNumerationColumnType.CROSS.getType());

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(autoNumerationColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.SERIAL);
        assertTrue("Должна быть хотя бы одна сквозная автонумеруемая графа", anyCrossAutoNumerationColumn);
        assertFalse("Не должно быть ни одной последовательной автонумеруемой графы", anySerialAutoNumerationColumn);
    }

    /**
     * Есть оба вида автонумеруемых граф
     */
    @Test
    public void testIsAnyAutoNumerationColumnBothExist() {
        FormTemplate formTemplate = new FormTemplate();

        Column numericColumn = new NumericColumn();
        Column stringColumn = new StringColumn();
        Column crossAutoNumerationColumn = new AutoNumerationColumn(AutoNumerationColumnType.CROSS.getName(), AutoNumerationColumnType.CROSS.getType());
        Column serialAutoNumerationColumn = new AutoNumerationColumn(AutoNumerationColumnType.SERIAL.getName(), AutoNumerationColumnType.SERIAL.getType());

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(crossAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(serialAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(crossAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(serialAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.SERIAL);
        assertTrue("Должна быть хотя бы одна сквозная автонумеруемая графа", anyCrossAutoNumerationColumn);
        assertTrue("Должна быть хотя бы одна последовательная автонумеруемая графа", anySerialAutoNumerationColumn);
    }

//    @Test(expected = ValidationException.class)
//    public void validationTest() {
//        formTemplateService.delete(null);
//    }
}
