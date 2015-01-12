package com.aplana.sbrf.taxaccounting.form_template.property.property_945_1.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Данные бухгалтерского учета для расчета налога на имущество.
 */
public class Property945_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 610;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formData.setId(TestScriptHelper.CURRENT_FORM_DATA_ID);
        formData.setFormType(formType);
        formData.setFormTemplateId(TYPE_ID);
        formData.setKind(KIND);
        formData.setState(WorkflowState.CREATED);
        formData.setDepartmentId(DEPARTMENT_ID);
        formData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Property945_1Test.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Before
    public void mockFormDataService() {
        when(testHelper.getDepartmentReportPeriodService().get(anyInt())).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
                        departmentReportPeriod.setBalance(false);
                        return departmentReportPeriod;
                    }
                }
        );

        when(testHelper.getRefBookFactory().getDataProvider(anyLong())).thenAnswer(new Answer<RefBookDataProvider>() {
            @Override
            public RefBookDataProvider answer(InvocationOnMock invocation) throws Throwable {
                final Long id = (Long) invocation.getArguments()[0];
                if (id == null) {
                    return null;
                }
                RefBookDataProvider refBookDataProvider = mock(RefBookDataProvider.class);
                // для остальных справочников используется метод getRecords
                when(refBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenAnswer(
                        new Answer<PagingResult<Map<String, RefBookValue>>>() {
                            @Override
                            public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                                Map<String, RefBookValue> record = testHelper.getFormDataService().getRefBookValue(id, 1L, new HashMap<String, Map<String, RefBookValue>>());
                                return new PagingResult<Map<String, RefBookValue>>(Arrays.asList(record));
                            }
                        });
                return refBookDataProvider;
            }
        });

        when(testHelper.getRefBookFactory().get(anyLong())).thenAnswer(new Answer<RefBook>() {
            @Override
            public RefBook answer(InvocationOnMock invocation) throws Throwable {
                final Long id = (Long) invocation.getArguments()[0];
                if (id == null) {
                    return null;
                }
                RefBookAttribute refBookAttribute = new RefBookAttribute();
                refBookAttribute.setAlias("CODE");
                List<RefBookAttribute> list = Arrays.asList(refBookAttribute);

                RefBook refBook = new RefBook();
                refBook.setAttributes(list);
                return refBook;
            }
        });
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void addDelRowTest() {
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size() + 1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(58, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(58, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Double [] values = new Double [] {
                32661.00, 32661.00, 0.00, 0.00, 32661.00, -1.00, -2.00,
                -1.00, -233.00, -1.00, -1.00, -2.00, -1.00, -1.00, -2.00,
                -1.00, -233.00, 0.00, 0.00, -1.00, -1.00, -1.00, 0.00, -233.00,
                -233.00, -433.00, -433.00, -433.00, -233.00, -1.00, -1.00, 0.00,
                0.00, -1.00, -1.00, -2.00, -1.00, -233.00, -1.00, -1.00, -2.00,
                -1.00, -1.00, -2.00, -1.00, -233.00, 0.00, 0.00, -1.00, -1.00,
                -1.00, 0.00, -233.00, -233.00, -433.00, -433.00, -433.00, -2331.00
        };

        // графа 7
        for (int i = 0; i < dataRows.size() - 1; i++) {
            Assert.assertEquals(values[i], dataRows.get(i).getCell("taxBaseSum").getNumericValue().doubleValue(), 0.00001);
        }
    }
}
