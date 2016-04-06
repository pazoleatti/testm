package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Fail Mukhametdinov
 */
public class FormTemplateServiceImplTest extends Assert {

    public static final int FORM_TEMPLATE_ID = 1;
    public static final int COLUMN_ID = 1;
    public static final String MESSAGE =
            "Следующие периоды форм данной версии макета закрыты: год 2014, первый квартал 2015. Для добавления в макет автонумеруемой графы с типом сквозной нумерации строк необходимо открыть перечисленные периоды!";

    private Logger logger;
	private FormTemplate formTemplateFromDB;
	private TAUserInfo userInfo;
    private List<DepartmentReportPeriod> departmentReportPeriodList;
    private DepartmentReportPeriodDao departmentReportPeriodDao;
	private FormTemplateDao formTemplateDao;
	private FormTemplate formTemplateEdited;
	private FormDataService formDataService;
	private LogEntryService logEntryService;
	private FormTemplateService formTemplateService = new FormTemplateServiceImpl();

    @Before
    public void init() {
        logger = new Logger();
        userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(0);
        user.setName("Name");
		user.setDepartmentId(0);
        userInfo.setUser(user);

        FormType formType = new FormType();
        formType.setName("Тестовый");
        formType.setTaxType(TaxType.ETR);
        // Макет, который "хранится" в БД. До редактирования
        formTemplateFromDB = new FormTemplate();
        formTemplateFromDB.setId(FORM_TEMPLATE_ID);
        formTemplateFromDB.setType(formType);

        AutoNumerationColumn autoNumerationColumn = new AutoNumerationColumn();
        autoNumerationColumn.setId(COLUMN_ID);
        autoNumerationColumn.setNumerationType(NumerationType.SERIAL);
        formTemplateFromDB.addColumn(autoNumerationColumn);

        // Закрытые отчетные периоды
        departmentReportPeriodList = new ArrayList<DepartmentReportPeriod>();

        ReportPeriod reportPeriod1 = new ReportPeriod();
        reportPeriod1.setName("год");
        TaxPeriod taxPeriod1 = new TaxPeriod();
        taxPeriod1.setYear(2014);
        reportPeriod1.setTaxPeriod(taxPeriod1);
        DepartmentReportPeriod departmentReportPeriod1 = new DepartmentReportPeriod();
        departmentReportPeriod1.setReportPeriod(reportPeriod1);
        departmentReportPeriodList.add(departmentReportPeriod1);

        ReportPeriod reportPeriod2 = new ReportPeriod();
        reportPeriod2.setName("первый квартал");
        TaxPeriod taxPeriod2 = new TaxPeriod();
        taxPeriod2.setYear(2015);
        reportPeriod2.setTaxPeriod(taxPeriod2);
        DepartmentReportPeriod departmentReportPeriod2 = new DepartmentReportPeriod();
        departmentReportPeriod2.setReportPeriod(reportPeriod2);
        departmentReportPeriodList.add(departmentReportPeriod2);

        // Версия макета до редактирования
        formTemplateDao = mock(FormTemplateDao.class);
        when(formTemplateDao.get(FORM_TEMPLATE_ID)).thenReturn(formTemplateFromDB);

        departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        formDataService = mock(FormDataService.class);

        formTemplateEdited = SerializationUtils.clone(formTemplateFromDB);

        logEntryService = mock(LogEntryService.class);
		TAUserService userService = mock(TAUserService.class);
		when(userService.getSystemUserInfo()).thenReturn(userInfo);

        ReflectionTestUtils.setField(formTemplateService, "formTemplateDao", formTemplateDao);
        ReflectionTestUtils.setField(formTemplateService, "formDataService", formDataService);
        ReflectionTestUtils.setField(formTemplateService, "departmentReportPeriodDao", departmentReportPeriodDao);
        ReflectionTestUtils.setField(formTemplateService, "logEntryService", logEntryService);
		ReflectionTestUtils.setField(formTemplateService, "userService", userService);

        FormDataScriptingServiceImpl scriptingService = new FormDataScriptingServiceImpl();
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBeansWithAnnotation(ScriptExposed.class)).thenReturn(new HashMap<String, Object>());
        scriptingService.setApplicationContext(ctx);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }

            @Override
            public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }
        };
        ReflectionTestUtils.setField(scriptingService, "tx", tx);

        DepartmentService departmentService = mock(DepartmentService.class);
        ReflectionTestUtils.setField(scriptingService, "departmentService", departmentService);

        ReflectionTestUtils.setField(formTemplateService, "scriptingService", scriptingService);
        ReflectionTestUtils.setField(formTemplateService, "mainOperatingService", mock(MainOperatingService.class));
    }

    /**
     * Значение атрибута "Тип нумерации строк" ни для одной автонумеруемой графы макета не изменено на "Сквозная".
     */
    @Test
    public void validateFormAutoNumerationColumn_notCross() {
        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger, userInfo);
        verify(formDataService, never()).batchUpdatePreviousNumberRow(any(FormTemplate.class), any(TAUserInfo.class));
        assertTrue(logger.getEntries().isEmpty());
    }

    /**
     * В данной версии макета до редактирования не было ни одной автонумеруемой графы
     * у которой "Тип нумерации строк" = "Сквозная".
     *
     * Отчетный период всех экземпляров НФ данной версии макета открыт.
     */
    @Test
    public void validateFormAutoNumerationColumn_cross1() {
        AutoNumerationColumn autoNumerationColumn = (AutoNumerationColumn) formTemplateEdited.getColumn(FORM_TEMPLATE_ID);
        autoNumerationColumn.setNumerationType(NumerationType.CROSS);

        when(departmentReportPeriodDao.getClosedForFormTemplate(FORM_TEMPLATE_ID)).thenReturn(new ArrayList<DepartmentReportPeriod>(0));
        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger, userInfo);

        verify(formDataService).batchUpdatePreviousNumberRow(any(FormTemplate.class), any(TAUserInfo.class));
        assertTrue(logger.getEntries().isEmpty());
    }

    /**
     * В данной версии макета до редактирования не было ни одной автонумеруемой графы
     * у которой "Тип нумерации строк" = "Сквозная".
     *
     * Отчетный период хотя бы части экземпляров НФ данной версии макета закрыт.
     */
    @Test
    public void validateFormAutoNumerationColumn_cross2() {
        // Редактируемый макет
        AutoNumerationColumn autoNumerationColumn = (AutoNumerationColumn) formTemplateEdited.getColumn(FORM_TEMPLATE_ID);
        autoNumerationColumn.setNumerationType(NumerationType.CROSS);

        when(departmentReportPeriodDao.getClosedForFormTemplate(FORM_TEMPLATE_ID)).thenReturn(departmentReportPeriodList);

        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger, userInfo);

        verify(formDataService, never()).batchUpdatePreviousNumberRow(any(FormTemplate.class), any(TAUserInfo.class));
        assertTrue(logger.getEntries().size() == 1);
        assertEquals(MESSAGE, logger.getEntries().get(0).getMessage());
    }

    /**
     * В данной версии макета до редактирования была хотя бы одна автонумеруемая графа
     * у которой "Тип нумерации строк" = "Сквозная".
     */
    @Test
    public void validateFormAutoNumerationColumn_cross3() {
        // Макет, который "хранится" в БД. До редактирования
        formTemplateFromDB = new FormTemplate();
        formTemplateFromDB.setId(FORM_TEMPLATE_ID);

        AutoNumerationColumn autoNumerationColumn = new AutoNumerationColumn();
        autoNumerationColumn.setId(COLUMN_ID);
		autoNumerationColumn.setNumerationType(NumerationType.CROSS);
        formTemplateFromDB.addColumn(autoNumerationColumn);

        when(formTemplateDao.get(FORM_TEMPLATE_ID)).thenReturn(formTemplateFromDB);

        formTemplateService.validateFormAutoNumerationColumn(formTemplateEdited, logger, userInfo);

        verify(formDataService, never()).batchUpdatePreviousNumberRow(any(FormTemplate.class), any(TAUserInfo.class));
        assertTrue(logger.getEntries().isEmpty());
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

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.SERIAL);
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
        Column autoNumerationColumn = new AutoNumerationColumn(NumerationType.SERIAL);

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(autoNumerationColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.SERIAL);
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
        Column autoNumerationColumn = new AutoNumerationColumn(NumerationType.CROSS);

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(autoNumerationColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.SERIAL);
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
        Column crossAutoNumerationColumn = new AutoNumerationColumn(NumerationType.CROSS);
        Column serialAutoNumerationColumn = new AutoNumerationColumn(NumerationType.SERIAL);

        formTemplate.addColumn(numericColumn);
        formTemplate.addColumn(crossAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(serialAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(crossAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);
        formTemplate.addColumn(serialAutoNumerationColumn);
        formTemplate.addColumn(stringColumn);

        boolean anyCrossAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS);
        boolean anySerialAutoNumerationColumn = formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.SERIAL);
        assertTrue("Должна быть хотя бы одна сквозная автонумеруемая графа", anyCrossAutoNumerationColumn);
        assertTrue("Должна быть хотя бы одна последовательная автонумеруемая графа", anySerialAutoNumerationColumn);
    }

    @Test
    public void updateScript1() throws IOException {
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        FormType formType = new FormType();
        formType.setName("Тестовый");
        formType.setTaxType(TaxType.ETR);
        formTemplate.setType(formType);
        InputStream stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("updateFormTemplateScript1.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        formTemplate.setScript(script);
        Logger log = new Logger();
        formTemplateService.updateScript(formTemplate, log, new TAUserInfo());
    }

    @Test(expected = ServiceLoggerException.class)
    public void updateScript2() throws IOException {
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        FormType formType = new FormType();
        formType.setName("Тестовый");
        formType.setTaxType(TaxType.ETR);
        formTemplate.setType(formType);
        InputStream stream = FormTemplateServiceImplTest.class.getResourceAsStream("updateFormTemplateScript2.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        formTemplate.setScript(script);
        Logger log = new Logger();
        formTemplateService.updateScript(formTemplate, log, new TAUserInfo());
    }

//    @Test(expected = ValidationException.class)
//    public void validationTest() {
//        formTemplateService.delete(null);
//    }
}
