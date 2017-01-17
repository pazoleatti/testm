package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.test.FormTypeMockUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentFormTypeMockUtils.mockDepartmentFormType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.DepartmentReportPeriodMockUtils.mockDepartmentReportPeriodData;
import static com.aplana.sbrf.taxaccounting.test.FormDataMockUtils.mockFormData;
import static com.aplana.sbrf.taxaccounting.test.FormTemplateMockUtils.mockFormTemplate;
import static com.aplana.sbrf.taxaccounting.test.ReportPeriodMockUtils.mockReportPeriod;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

public class FormDataAccessServiceImplTest {
    private static FormDataAccessServiceImpl service = new FormDataAccessServiceImpl();

    private static final TAUserInfo userInfo = new TAUserInfo() {{
        setIp(LOCAL_IP);
    }};
    static SourceService sourceService = mock(SourceService.class);

    private static final Logger logger = new Logger();

    private static final int ROOT_BANK_ID = 1;
    private static final int TB1_ID = 2;
    private static final int TB2_ID = 3;
    private static final int GOSB_TB1_ID = 4;

    private static final long TB1_CREATED_FORMDATA_ID = 1;
    private static final long TB1_APPROVED_FORMDATA_ID = 2;
    private static final long TB1_ACCEPTED_FORMDATA_ID = 3;

    private static final long TB2_CREATED_FORMDATA_ID = 4;
    private static final long TB2_APPROVED_FORMDATA_ID = 5;
    private static final long TB2_ACCEPTED_FORMDATA_ID = 6;

    private static final long BANK_CREATED_FORMDATA_ID = 7;
    private static final long BANK_ACCEPTED_FORMDATA_ID = 9;
    private static final long BANK_PREPARED_FORMDATA_ID = 11;
    private static final long BANK_APPROVED_FORMDATA_ID = 12;
    private static final long BANK_CREATED_SUMMARY_FORMDATA_ID = 14;
    private static final long BANK_ACCEPTED_SUMMARY_FORMDATA_ID = 15;
    private static final long BANK_CREATED_ADDITIONAL_FORMDATA_ID = 16;
    private static final long BANK_PREPARED_ADDITIONAL_FORMDATA_ID = 17;
    private static final long BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID = 18;

    private static final long TB1_CREATED_FORMDATA_BALANCED_ID = 19;
    private static final long TB1_ACCEPTED_FORMDATA_BALANCED_ID = 20;

    private static final long GOSB_TB1_CREATED_CALCULATED_FORMDATA_ID = 21;
    private static final long GOSB_TB1_ACCEPTED_CALCULATED_FORMDATA_ID = 22;

    private static final long GOSB_TB1_CREATED_FORMDATA_ID = 13;

    private static final long INACTIVE_FORMDATA_ID = 10;

    private static final int TB1_CONTROL_USER_ID = 1;
    private static final int TB1_OPERATOR_USER_ID = 7;

    private static final int BANK_CONTROL_USER_ID = 3;
    private static final int BANK_OPERATOR_USER_ID = 4;
    private static final int BANK_CONTROL_UNP_USER_ID = 5;
    private static final int BANK_CONTROL_NS_USER_ID = 6;

    private static final int REPORT_PERIOD_ACTIVE_ID = 1;
    private static final int REPORT_PERIOD_INACTIVE_ID = 2;
    private static final int REPORT_PERIOD_BALANCED_ID = 3;

    private static final int BANK_ACTIVE_ID = 11;
    private static final int BANK_INACTIVE_ID = 12;
    private static final int BANK_BALANCED_ID = 13;
    private static final int TB1_ACTIVE_ID = 21;
    private static final int TB1_INACTIVE_ID = 22;
    private static final int TB1_BALANCED_ID = 23;
    private static final int TB2_ACTIVE_ID = 31;
    private static final int TB2_INACTIVE_ID = 32;
    private static final int TB2_BALANCED_ID = 33;
    private static final int GOSB_TB1_ACTIVE_ID = 41;
    private static final int GOSB_TB1_INACTIVE_ID = 42;
    private static final int GOSB_TB1_BALANCED_ID = 43;

    private static final int DECLARATION_TYPE_1_ID = 101;

    private static final String LOCAL_IP = "127.0.0.1";

    static DepartmentFormTypeDao departmentFormTypeDao;
    static FormDataService formDataService;

    @BeforeClass
    public static void tearUp() {
        FormType summaryFormType1 = FormTypeMockUtils.mockFormType(1, TaxType.INCOME, "summary 1");
        FormType summaryFormType2 = FormTypeMockUtils.mockFormType(2, TaxType.INCOME, "summary 2");
        FormType additionalFormType = FormTypeMockUtils.mockFormType(3, TaxType.INCOME, "additional");
        FormType etrFormType = FormTypeMockUtils.mockFormType(4, TaxType.ETR, "etr");

        FormTemplateDao formTemplateDao = mock(FormTemplateDao.class);
        FormTemplate formTemplate1 = mockFormTemplate(1, summaryFormType1.getId(), TaxType.INCOME, "Тип формы 1",
                VersionedObjectStatus.NORMAL);
        when(formTemplate1.getVersion()).thenReturn(new Date(34543534));
        when(formTemplateDao.get(1)).thenReturn(formTemplate1);
        FormTemplate formTemplate2 = mockFormTemplate(2, summaryFormType2.getId(), TaxType.INCOME, "Тип формы 2",
                VersionedObjectStatus.NORMAL);
        when(formTemplate2.getVersion()).thenReturn(new Date(34543534));
        when(formTemplateDao.get(2)).thenReturn(formTemplate2);
        FormTemplate formTemplate3 = mockFormTemplate(3, additionalFormType.getId(), TaxType.INCOME, "Тип формы 3",
                VersionedObjectStatus.NORMAL);
        when(formTemplate3.getVersion()).thenReturn(new Date(34543534));
        when(formTemplateDao.get(3)).thenReturn(formTemplate3);

        FormTemplate formTemplate4 = mockFormTemplate(4, etrFormType.getId(), TaxType.ETR, "Форма ЭНС 1",
                VersionedObjectStatus.NORMAL);
        when(formTemplate4.getVersion()).thenReturn(new Date(34543534));
        when(formTemplate4.isComparative()).thenReturn(true);
        when(formTemplate4.isAccruing()).thenReturn(true);
        when(formTemplateDao.get(4)).thenReturn(formTemplate4);

        FormTemplate formTemplate5 = mockFormTemplate(5, etrFormType.getId(), TaxType.ETR, "Форма ЭНС 2",
                VersionedObjectStatus.NORMAL);
        when(formTemplate5.getVersion()).thenReturn(new Date(34543534));
        when(formTemplate5.isComparative()).thenReturn(false);
        when(formTemplate5.isAccruing()).thenReturn(true);
        when(formTemplateDao.get(5)).thenReturn(formTemplate5);

        ReflectionTestUtils.setField(service, "formTemplateDao", formTemplateDao);

        final DepartmentService departmentService = mock(DepartmentService.class);
        final FormTemplateService formTemplateService = mock(FormTemplateService.class);
        ReflectionTestUtils.setField(service, "formTemplateService", formTemplateService);
        when(formTemplateService.getNearestFTRight(1)).thenReturn(formTemplate1);
        when(formTemplateService.getNearestFTRight(3)).thenReturn(formTemplate3);
        Department d;

        // В тербанках есть формы 1 (консолидированная и сводная) и 3 (выходная)

        d = mockDepartment(TB1_ID, ROOT_BANK_ID, DepartmentType.TERR_BANK);
        when(departmentService.getDepartment(TB1_ID)).thenReturn(d);


        d = mockDepartment(TB2_ID, ROOT_BANK_ID, DepartmentType.TERR_BANK);
        when(departmentService.getDepartment(TB2_ID)).thenReturn(d);

        // В банке есть форма 1 (сводная), 2 (сводная) и 3 (выходная)
        d = mockDepartment(ROOT_BANK_ID, null, DepartmentType.ROOT_BANK);
        when(departmentService.getDepartment(ROOT_BANK_ID)).thenReturn(d);

        // Доступные подразделения (для чтения)
        when(departmentService.getTaxFormDepartments(any(TAUser.class), anyListOf(TaxType.class), any(Date.class), any(Date.class))).thenAnswer(
                new Answer<List<Integer>>() {
                    @Override
                    public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                        TAUser user = (TAUser) invocation.getArguments()[0];

                        List<Integer> retVal = new LinkedList<Integer>();
                        if (user.hasRole(TARole.ROLE_OPER)
                                || user.hasRole(TARole.ROLE_CONTROL)
                                || user.hasRole(TARole.ROLE_CONTROL_NS)
                                || user.hasRole(TARole.ROLE_CONTROL_UNP)) {
                            retVal.add(ROOT_BANK_ID);
                        }
                        if (user.hasRole(TARole.ROLE_CONTROL)
                                || user.hasRole(TARole.ROLE_CONTROL_NS)
                                || user.hasRole(TARole.ROLE_CONTROL_UNP)) {
                            retVal.add(TB1_ID);
                        }
                        if (user.hasRole(TARole.ROLE_CONTROL_NS)
                                || user.hasRole(TARole.ROLE_CONTROL_UNP)) {
                            retVal.add(TB2_ID);
                        }
                        if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
                            retVal.add(GOSB_TB1_ID);
                        }
                        if (!retVal.contains(user.getDepartmentId())) {
                            retVal.add(user.getDepartmentId());
                        }
                        return retVal;
                    }
                });

        // Доступные подразделения (для создания)
        when(departmentService.getOpenPeriodDepartments(any(TAUser.class), anyListOf(TaxType.class),
                anyInt())).thenAnswer(new Answer<List<Integer>>() {
            @Override
            public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                TAUser user = (TAUser) invocation.getArguments()[0];
                List<TaxType> taxTypeList = (List<TaxType>) invocation.getArguments()[1];
                Integer reportPeriodId = (Integer) invocation.getArguments()[2];
                if (reportPeriodId.equals(REPORT_PERIOD_ACTIVE_ID) || reportPeriodId.equals(REPORT_PERIOD_BALANCED_ID)) {
                    return departmentService.getTaxFormDepartments(user, taxTypeList, null, null);
                } else {
                    return new ArrayList<Integer>(0);
                }
            }
        });

        ReflectionTestUtils.setField(service, "departmentService", departmentService);

        PeriodService periodService = mock(PeriodService.class);
        when(periodService.getReportPeriod(any(Integer.class))).thenReturn(mock(ReportPeriod.class));
        ReflectionTestUtils.setField(service, "periodService", periodService);

        // Сводная форма 1 из тербанка 1 является источником для сводной 1 банка
        departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
        dfts.add(mockDepartmentFormType(ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY));


        //when(departmentFormTypeDao.getFormDestinations(any(Integer.class), any(Integer.class), any(FormDataKind.class), any(Date.class), any(Date.class))).thenReturn(dfts);

        final Map<Integer, DepartmentReportPeriod> periods = new HashMap<Integer, DepartmentReportPeriod>();
        periods.put(BANK_ACTIVE_ID, mockDepartmentReportPeriodData(BANK_ACTIVE_ID, ROOT_BANK_ID, mockReportPeriod(REPORT_PERIOD_ACTIVE_ID), true, false, null));
        periods.put(BANK_INACTIVE_ID, mockDepartmentReportPeriodData(BANK_INACTIVE_ID, ROOT_BANK_ID, mockReportPeriod(REPORT_PERIOD_INACTIVE_ID), false, false, null));
        periods.put(BANK_BALANCED_ID, mockDepartmentReportPeriodData(BANK_BALANCED_ID, ROOT_BANK_ID, mockReportPeriod(REPORT_PERIOD_BALANCED_ID), true, true, null));
        periods.put(TB1_ACTIVE_ID, mockDepartmentReportPeriodData(TB1_ACTIVE_ID, TB1_ID, mockReportPeriod(REPORT_PERIOD_ACTIVE_ID), true, false, null));
        periods.put(TB1_INACTIVE_ID, mockDepartmentReportPeriodData(TB1_INACTIVE_ID, TB1_ID, mockReportPeriod(REPORT_PERIOD_INACTIVE_ID), false, false, null));
        periods.put(TB1_BALANCED_ID, mockDepartmentReportPeriodData(TB1_BALANCED_ID, TB1_ID, mockReportPeriod(REPORT_PERIOD_BALANCED_ID), true, true, null));
        periods.put(TB2_ACTIVE_ID, mockDepartmentReportPeriodData(TB2_ACTIVE_ID, TB2_ID, mockReportPeriod(REPORT_PERIOD_ACTIVE_ID), true, false, null));
        periods.put(TB2_INACTIVE_ID, mockDepartmentReportPeriodData(TB2_INACTIVE_ID, TB2_ID, mockReportPeriod(REPORT_PERIOD_INACTIVE_ID), false, false, null));
        periods.put(TB2_BALANCED_ID, mockDepartmentReportPeriodData(TB2_BALANCED_ID, TB2_ID, mockReportPeriod(REPORT_PERIOD_BALANCED_ID), true, true, null));
        periods.put(GOSB_TB1_ACTIVE_ID, mockDepartmentReportPeriodData(GOSB_TB1_ACTIVE_ID, GOSB_TB1_ID, mockReportPeriod(REPORT_PERIOD_ACTIVE_ID), true, false, null));
        periods.put(GOSB_TB1_INACTIVE_ID, mockDepartmentReportPeriodData(GOSB_TB1_INACTIVE_ID, GOSB_TB1_ID, mockReportPeriod(REPORT_PERIOD_INACTIVE_ID), false, false, null));
        periods.put(GOSB_TB1_BALANCED_ID, mockDepartmentReportPeriodData(GOSB_TB1_BALANCED_ID, GOSB_TB1_ID, mockReportPeriod(REPORT_PERIOD_BALANCED_ID), true, true, null));

        FormDataDao formDataDao = mock(FormDataDao.class);
        FormData fd;
        final List<FormData> fdList = new ArrayList<FormData>();

        fd = mockFormData(TB1_CREATED_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB1_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(TB1_APPROVED_FORMDATA_ID, WorkflowState.APPROVED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB1_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB1_APPROVED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(TB1_ACCEPTED_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB1_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB1_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(INACTIVE_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB1_INACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

        fd = mockFormData(TB2_CREATED_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB2_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB2_CREATED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(TB2_APPROVED_FORMDATA_ID, WorkflowState.APPROVED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB2_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB2_APPROVED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(TB2_ACCEPTED_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB2_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB2_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(INACTIVE_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB2_INACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

        fd = mockFormData(BANK_CREATED_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_CREATED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_ACCEPTED_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_PREPARED_FORMDATA_ID, WorkflowState.PREPARED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_PREPARED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_APPROVED_FORMDATA_ID, WorkflowState.APPROVED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_APPROVED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_CREATED_SUMMARY_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, summaryFormType1, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_CREATED_SUMMARY_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_ACCEPTED_SUMMARY_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, summaryFormType1, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_ACCEPTED_SUMMARY_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_CREATED_ADDITIONAL_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_CREATED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_PREPARED_ADDITIONAL_FORMDATA_ID, WorkflowState.PREPARED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_PREPARED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(INACTIVE_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(BANK_INACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

        fd = mockFormData(GOSB_TB1_CREATED_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, additionalFormType, periods.get(GOSB_TB1_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(GOSB_TB1_CREATED_FORMDATA_ID)).thenReturn(fd);

        fd = mockFormData(TB1_CREATED_FORMDATA_BALANCED_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB1_BALANCED_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB1_CREATED_FORMDATA_BALANCED_ID)).thenReturn(fd);
        fd = mockFormData(TB1_ACCEPTED_FORMDATA_BALANCED_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, summaryFormType1, periods.get(TB1_BALANCED_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(TB1_ACCEPTED_FORMDATA_BALANCED_ID)).thenReturn(fd);

        fd = mockFormData(GOSB_TB1_CREATED_CALCULATED_FORMDATA_ID, WorkflowState.CREATED, FormDataKind.CALCULATED, summaryFormType1, periods.get(GOSB_TB1_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(GOSB_TB1_CREATED_CALCULATED_FORMDATA_ID)).thenReturn(fd);
        fd = mockFormData(GOSB_TB1_ACCEPTED_CALCULATED_FORMDATA_ID, WorkflowState.ACCEPTED, FormDataKind.CALCULATED, summaryFormType1, periods.get(GOSB_TB1_ACTIVE_ID));
        fdList.add(fd);
        when(formDataDao.getWithoutRows(GOSB_TB1_ACCEPTED_CALCULATED_FORMDATA_ID)).thenReturn(fd);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                int formTypeId = (Integer) invocation.getArguments()[0];
                FormDataKind kind = (FormDataKind) invocation.getArguments()[1];
                int departmentId = (Integer) invocation.getArguments()[2];
                int reportPeriodId = (Integer) invocation.getArguments()[3];
                int periodOrder = (Integer) invocation.getArguments()[4];
                for (FormData formData : fdList) {
                    if (formData.getFormType().getId() == formTypeId &&
                            formData.getKind().equals(kind) &&
                            formData.getDepartmentId() == departmentId &&
                            formData.getReportPeriodId() == reportPeriodId &&
                            formData.getPeriodOrder() == periodOrder) {
                       return formData;
                    }
                }
                return null;
            }
        }).when(formDataDao).getLast(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), anyInt(), any(Integer.class), any(Boolean.class));

        ReflectionTestUtils.setField(service, "formDataDao", formDataDao);

        DepartmentReportPeriodDao departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Integer key = (Integer) invocation.getArguments()[0];
                return periods.get(key);
            }
        }).when(departmentReportPeriodDao).get(anyInt());
        when(departmentReportPeriodDao.getListIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(Arrays.asList(1));
        //when(departmentReportPeriodDao.get().thenReturn(Arrays.asList(1));
        ReflectionTestUtils.setField(service, "departmentReportPeriodDao", departmentReportPeriodDao);

        PeriodService reportPeriodService = mock(PeriodService.class);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setStartDate(new Date(456256245));
        reportPeriod.setEndDate(new Date(908686433));
        when(reportPeriodService.getReportPeriod(REPORT_PERIOD_ACTIVE_ID)).thenReturn(reportPeriod);
        when(reportPeriodService.getReportPeriod(REPORT_PERIOD_BALANCED_ID)).thenReturn(reportPeriod);
        when(reportPeriodService.isFirstPeriod(REPORT_PERIOD_ACTIVE_ID)).thenReturn(false);
        when(reportPeriodService.isFirstPeriod(REPORT_PERIOD_BALANCED_ID)).thenReturn(true);
        ReflectionTestUtils.setField(service, "reportPeriodService", reportPeriodService);

        dfts.add(mockDepartmentFormType(TB1_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(TB1_ID, summaryFormType1.getId(), FormDataKind.CONSOLIDATED));
        dfts.add(mockDepartmentFormType(TB1_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
        dfts.add(mockDepartmentFormType(TB1_ID, etrFormType.getId(), FormDataKind.CONSOLIDATED));
        when(sourceService.getDFTByDepartment(Matchers.eq(TB1_ID), Matchers.any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(dfts);

        dfts = new ArrayList<DepartmentFormType>();
        dfts.add(mockDepartmentFormType(TB2_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(TB2_ID, summaryFormType1.getId(), FormDataKind.CONSOLIDATED));
        dfts.add(mockDepartmentFormType(TB2_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
        dfts.add(mockDepartmentFormType(TB2_ID, etrFormType.getId(), FormDataKind.CONSOLIDATED));
        when(sourceService.getDFTByDepartment(Matchers.eq(TB2_ID), Matchers.any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(dfts);

        dfts = new ArrayList<DepartmentFormType>();
        dfts.add(mockDepartmentFormType(ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(ROOT_BANK_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
        when(sourceService.getDFTByDepartment(Matchers.eq(ROOT_BANK_ID), Matchers.any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(dfts);
        List<DepartmentDeclarationType> ddtList = new ArrayList<DepartmentDeclarationType>();
        when(sourceService.getDeclarationDestinations(ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY, 1)).thenReturn(ddtList);
        when(sourceService.getDeclarationDestinations(ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY, 1)).thenReturn(null);
        ReflectionTestUtils.setField(service, "sourceService", sourceService);

        FormTypeDao formTypeDao = mock(FormTypeDao.class);
        FormType ft = new FormType();
        ft.setTaxType(TaxType.INCOME);
        when(formTypeDao.get(Matchers.anyInt())).thenReturn(ft);
        ReflectionTestUtils.setField(service, "formTypeDao", formTypeDao);

        ddtList.add(mockDepartmentDeclarationType(ROOT_BANK_ID, DECLARATION_TYPE_1_ID));

        DeclarationDataDao declarationDataDao = mock(DeclarationDataDao.class);
        DeclarationData declarationData = new DeclarationData();
        declarationData.setState(State.CREATED);
        declarationData.setDepartmentId(TB1_ID);
        //when(declarationDataDao.find(DECLARATION_TYPE_1_ID, ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID)).thenReturn(declarationData);
        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);

        formDataService = mock(FormDataService.class);
        fd = mockFormData(TB1_CREATED_FORMDATA_ID, TB1_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
        when(formDataService.getLast(summaryFormType1.getId(), FormDataKind.SUMMARY, ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID, null, null, false)).thenReturn(fd);
    }

    @Before
    public void setup() {
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
        ReflectionTestUtils.setField(service, "formDataService", formDataService);
    }

    @Test
    public void testCanReadForFirstLifeCycle() {
        /* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
             и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/
        //Любой пользователь может читать Выходные формы данного жизненного цикла, находящиеся в любом состоянии
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID);
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID);
    }

    @Test(expected = AccessDeniedException.class)
    public void testCanReadForSecondLifeCycle() {
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

        //Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID);

        //Оператор не имеет прав на чтение НФ данного жизненного цикла
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID);
    }

    @Test(expected = AccessDeniedException.class)
    public void testCanReadForThirdLifeCycle() {
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/
        //Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        service.canRead(userInfo, TB1_CREATED_FORMDATA_ID);
        service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID);
        service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canRead(userInfo, TB1_CREATED_FORMDATA_ID);
        service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID);
        service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID);

        //Оператор не имеет прав на чтение НФ данного жизненного цикла
        userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPER));
        service.canRead(userInfo, TB1_CREATED_FORMDATA_ID);
        service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID);
        service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID);
    }

    @Test
    public void testCanEditForFirstLifeCycle() {
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

        //Все могут редактировать форму в статусе "Создана"
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID, false);
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID, false);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID, false);

        //Все, кроме Оператора, могут редактировать форму в статусе "Подготовлена"
        service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID, false);
        service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID, false);
		/*userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
		assertFalse(service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));*/

        //Никто не может редактировать НФ в статусе "Принята"
		/*assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));*/
    }

    @Test
    public void testCanEditForSecondLifeCycle() {
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

        //Никто не может редактировать налоговые формы данного жизненного цикла
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID, false); //TODO (Marat Fayzullin 20.03.2013) временно до появления первичных форм
		/*assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
		assertFalse(service.canEdit(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));*/

    }

    @Test
    public void testCanEditForThirdLifeCycle() {
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/

        userInfo.setUser(mockUser(BANK_CONTROL_NS_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_NS));
        service.canEdit(userInfo, GOSB_TB1_CREATED_CALCULATED_FORMDATA_ID, false);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, GOSB_TB1_ACCEPTED_CALCULATED_FORMDATA_ID, true);


        //Контролер текущего уровня, Контролер вышестоящего уровня и Контролер УНП могут редактировать НФ в состоянии "Создана"
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID, false);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID, false);


    }

    @Test(expected = AccessDeniedException.class)
    public void testCanEditForThirdLifeCycleFalseResult() {
        //Оператор не может редактировать НФ данного жизненного цикла в состоянии "Создана"
        userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPER));
        service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID, false);

        //Никто не может редактировать НФ данного жизненного цикла в состоянии "Утверждена" и "Принята"
        /*assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));*/
    }

    @Test(expected = AccessDeniedException.class)
    public void testCanEditForThirdLifeCycleFalseResult2() {
        //Никто не может редактировать НФ данного жизненного цикла в состоянии "Принята"
        userInfo.setUser(mockUser(BANK_CONTROL_NS_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_NS));
        service.canEdit(userInfo, GOSB_TB1_ACCEPTED_CALCULATED_FORMDATA_ID, false);
    }

    @Test
    public void testCanDelete() {
        // Удалять можно налоговые формы, находящиеся в состоянии "Создана" и для которых canEdit() == true
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
		/*assertFalse(service.canDelete(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID));
		assertFalse(service.canDelete(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID));*/
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID);
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID);
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID);
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID);
    }

    @Test
    public void testCanCreateOperator() {
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));

        // Оператор может создавать первичные и выходные в своём подразделении
		assertTrue(checkFail(userInfo, 3, FormDataKind.ADDITIONAL, BANK_ACTIVE_ID));

        // Оператор не может создавать консолидированные и выходные формы даже в своём
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, BANK_ACTIVE_ID));

        // Оператор не может создавать в чужом подразделении
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ACTIVE_ID));

        // Оператор не может создавать в своём подразделении, если в подразделении не разрешена работа с такой формой
		assertFalse(checkFail(userInfo, 2, FormDataKind.SUMMARY, BANK_ACTIVE_ID));
		assertFalse(checkFail(userInfo, 2, FormDataKind.SUMMARY, BANK_BALANCED_ID));
    }

    @Test
    public void testCanCreateControl() {
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));

        // Контролёр может создавать выходные формы
		assertTrue(checkFail(userInfo, 3, FormDataKind.ADDITIONAL, BANK_ACTIVE_ID));

        // Контролёр не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
        //TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, BANK_ACTIVE_ID));

        // Контролёр не может создавать формы, если они не разрешены в подразделении
		assertFalse(checkFail(userInfo, 3, FormDataKind.SUMMARY, BANK_ACTIVE_ID));

        // Контролёр не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
        //TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertTrue(checkFail(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID));

        // Контролёр может создать форму в чужом подразделении, если она является источником для одной из форм его подраздлеления
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ACTIVE_ID));

        // TODO: случай, когда форма в чужом подразделении является источником для другой формы в этом же подразделении, а уже эта вторая форма
        // является источником для одной из форм подразделения, к которому относится контролёр

        // Во всех остальных случаях контролёр не сможет создавать формы в чужих подразделениях
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB2_ACTIVE_ID));
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertTrue(checkFail(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_BALANCED_ID));

        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertFalse(checkFail(userInfo, 4, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, null, false));
        assertFalse(checkFail(userInfo, 4, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, null, true));
        assertTrue(checkFail(userInfo, 4, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, TB1_ACTIVE_ID, false));
        assertTrue(checkFail(userInfo, 4, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, TB1_ACTIVE_ID, true));
        assertTrue(checkFail(userInfo, 4, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, TB1_BALANCED_ID, false));
        assertFalse(checkFail(userInfo, 4, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, TB1_BALANCED_ID, true));

        assertTrue(checkFail(userInfo, 5, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, null, false));
        assertTrue(checkFail(userInfo, 5, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, null, true));
        assertFalse(checkFail(userInfo, 5, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, TB1_ACTIVE_ID, false));
        assertFalse(checkFail(userInfo, 5, FormDataKind.CONSOLIDATED, TB1_ACTIVE_ID, TB1_ACTIVE_ID, true));

        assertTrue(checkFail(userInfo, 5, FormDataKind.CONSOLIDATED, TB1_BALANCED_ID, null, false));
        assertFalse(checkFail(userInfo, 5, FormDataKind.CONSOLIDATED, TB1_BALANCED_ID, null, true));
    }

    @Test
    public void testCanCreateControlUnp() {
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));

        // Контролёр УНП может создавать любую разрешённую налоговую форму, в любом подразделении
        assertTrue(checkFail(userInfo, 3, FormDataKind.ADDITIONAL, BANK_ACTIVE_ID));

        // Контролёр УНП может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, BANK_ACTIVE_ID));

        // Контролёр УНП не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
//        assertTrue(checkFail(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_ACTIVE_ID));
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB2_ACTIVE_ID));

        // Контролёр УНП может создавать консолидированные и сводные, передающиеся в вышестоящее подразделение
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ACTIVE_ID));

        // Однако контролёр УНП не может создавать формы, если они не разрешены в подразедении
        assertFalse(checkFail(userInfo, 3, FormDataKind.SUMMARY, BANK_ACTIVE_ID));
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_BALANCED_ID));
    }

    @Test
    public void testGetAvailableMovesForFirstLifeCycle() {
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
             и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

        //Перевести из состояния "Создана" в "Подготовлена" может любой пользователь
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        assertArrayEquals(new Object[]{WorkflowMove.CREATED_TO_PREPARED},
                service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertArrayEquals(new Object[]{WorkflowMove.CREATED_TO_PREPARED},
                service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[]{WorkflowMove.CREATED_TO_PREPARED},
                service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());

        //Перевести из состояния "Подготовлена" в "Создана" и из "Подготовлена" в "Принята" может контролер текущего уровня,
        // контролер вышестоящего уровня или контролер УНП.
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertArrayEquals(new Object[]{WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_APPROVED},
                service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[]{WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_APPROVED},
                service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());

        //Перевести из состояния "Принята" в "Подготовлена" может контролер вышестоящего уровня и контролер УНП.
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[]{WorkflowMove.ACCEPTED_TO_APPROVED},
                service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
    }

    @Test
	public void testGetAvailableMovesForSecondLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Перевести из состояния "Создана" в "Принята" может контролер текущего уровня, контролер УНП
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
		/*assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());*/

		//Перевести из состояния "Принята" в "Создана" может текущий контролер или контролер УНП
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED },
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED},
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
/*		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED},
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());*/
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
		/*assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());*/
		/*assertArrayEquals(new Object[] { },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID).toArray());*/
		/*assertArrayEquals(new Object[] { },
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID).toArray());*/

	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle1(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/
		//Переводить из состояния "Создана" в "Утверждена" может контролер текущего уровня, контролер вышестоящего уровня
		// и контролер УНП
        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(1);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
                service.getAvailableMoves(userInfo, GOSB_TB1_ACCEPTED_CALCULATED_FORMDATA_ID).toArray());
        assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED},
                service.getAvailableMoves(userInfo, GOSB_TB1_CREATED_CALCULATED_FORMDATA_ID).toArray());
        /*assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());*/
	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle2(){
		// Перевести из состояния "Утверждена" в "Создана" и из "Утверждена" в "Принята" контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(1);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);
        assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED, WorkflowMove.APPROVED_TO_ACCEPTED },
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED, WorkflowMove.APPROVED_TO_ACCEPTED },
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());

		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED},
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle3(){
		// Перевести из состояния "Принята" в "Утверждена" контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(1);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());

		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
	}

	@Test(expected = AccessDeniedException.class)
	public void testGetAvailableMovesCommon() {
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));

		// Контролёр ТБ не может изменять статус в чужом тербанке
		assertEquals(0, service.getAvailableMoves(userInfo, TB2_ACCEPTED_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(userInfo, TB2_CREATED_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(userInfo, TB2_APPROVED_FORMDATA_ID).size());

		// Никто не может выполнять переходы, если отчетный период неактивен
		assertEquals(0, service.getAvailableMoves(userInfo, INACTIVE_FORMDATA_ID).size());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertEquals(0, service.getAvailableMoves(userInfo, INACTIVE_FORMDATA_ID).size());
	}

    @Test
    public void testGetAccessParams() {
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        // Проверяем только один случай, так как этот метод просто агрегирует результаты других методов,
        // а мы их уже оттестировали отдельно

        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(2);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);

        FormDataAccessParams params = service.getFormDataAccessParams(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID, false);
        assertTrue(params.isCanRead());
        assertTrue(params.isCanEdit());
        assertFalse(params.isCanDelete());
        assertArrayEquals(
                new Object[]{WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_APPROVED},
                params.getAvailableWorkflowMoves().toArray()
        );
    }

    /**
     * Проверяет выбрасывается ли ошибка прав доступа и т.д.. Необходим для совместимости написанных тестов и изменениями
     * сделанными в http://jira.aplana.com/browse/SBRFACCTAX-3939.
     * @return true если не было ошибки
     */
    private boolean checkFail(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentReportPeriodId) {
        try {
            service.canCreate(userInfo, formTemplateId, kind, departmentReportPeriodId, null, false);
        } catch (ServiceException se) {
            return false;
        }
        return true;
    }

    private boolean checkFail(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentReportPeriodId, Integer comparativeDepReportPeriod, boolean accruing) {
        try {
            service.canCreate(userInfo, formTemplateId, kind, departmentReportPeriodId, comparativeDepReportPeriod, accruing);
        } catch (ServiceException se) {
            return false;
        }
        return true;
    }

    @Test
    public void testGetAvailableFormDataKind() {
        List<TaxType> taxType = new ArrayList<TaxType>();
        List<FormDataKind> kinds = new ArrayList<FormDataKind>();
        List<FormDataKind> controlKinds = new ArrayList<FormDataKind>();
        List<FormDataKind> taxTypeKinds = new ArrayList<FormDataKind>();

        taxType.add(TaxType.DEAL);
        /* Тип по умолчанию */
        kinds.add(FormDataKind.PRIMARY);
        /* Дополнительные типы для контролеров */
        controlKinds.add(FormDataKind.CONSOLIDATED);
        controlKinds.add(FormDataKind.SUMMARY);
        /* Типы доступные для налогов на прибыль */
        taxTypeKinds.add(FormDataKind.ADDITIONAL);
        taxTypeKinds.add(FormDataKind.UNP);

        /* Для обычного оператора */
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        List<FormDataKind> list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 1);
        assertTrue(kinds.equals(list));

        /* Для контролеров */
        kinds.addAll(controlKinds);
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 3);
        assertTrue(kinds.equals(list));

        /* Для налога на прибыль */
        taxType.clear();
        taxType.add(TaxType.INCOME);

        /* Для контролеров */
        kinds.addAll(taxTypeKinds);
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, ROOT_BANK_ID, TARole.ROLE_CONTROL));
        list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 5);
        assertTrue(kinds.equals(list));
        /* Для оператора */
        kinds.removeAll(controlKinds);
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, ROOT_BANK_ID, TARole.ROLE_OPER));
        list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 3);
        assertTrue(kinds.equals(list));
    }

    @Test
    public void testCheckForDestinations() {
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);

        FormType formType = new FormType();
        formType.setId(1);

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setDepartmentId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setEndDate(new Date());

        when(departmentFormTypeDao.getFormDestinations(any(Integer.class), any(Integer.class), any(FormDataKind.class), any(Date.class), any(Date.class))).thenReturn(null);

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(mockUser(1, 1, TARole.ROLE_CONTROL_UNP));
        // Нет назначенных приемников
        assertFalse(service.checkDestinations(formData.getId(), userInfo, logger));
    }

    @Test
    public void testCheckForDestinations2() {
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);

        FormDataService formDataService = mock(FormDataService.class);
        ReflectionTestUtils.setField(service, "formDataService", formDataService);

        FormType formType = new FormType();
        formType.setId(1);
        // Редактируемая НФ
        FormData editedFormData = new FormData();
        editedFormData.setId(1L);
        editedFormData.setDepartmentId(1);
        editedFormData.setFormType(formType);
        editedFormData.setKind(FormDataKind.PRIMARY);
        editedFormData.setReportPeriodId(1);
        editedFormData.setPeriodOrder(1);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setEndDate(new Date());

        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setFormTypeId(1);
        departmentFormType.setKind(FormDataKind.PRIMARY);
        departmentFormType.setDepartmentId(1);

        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
        departmentFormTypes.add(departmentFormType);

        when(departmentFormTypeDao.getFormDestinations(any(Integer.class), any(Integer.class), any(FormDataKind.class), any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);
//        when(formDataService.findFormData(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), anyInt())).thenReturn(null);

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(mockUser(1, 1, TARole.ROLE_CONTROL_UNP));

        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(1);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);
        assertTrue(service.checkDestinations(editedFormData.getId(), userInfo, logger));
    }

    @Test
    public void testCheckForDestinations3() {
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);

        FormDataService formDataService = mock(FormDataService.class);
        ReflectionTestUtils.setField(service, "formDataService", formDataService);

        FormType formType = new FormType();
        formType.setId(1);
        // Редактируемая НФ
        FormData editedFormData = new FormData();
        editedFormData.setId(1L);
        editedFormData.setDepartmentId(1);
        editedFormData.setFormType(formType);
        editedFormData.setKind(FormDataKind.PRIMARY);
        editedFormData.setReportPeriodId(1);
        editedFormData.setPeriodOrder(1);
        // Приемник
        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setEndDate(new Date());

        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setFormTypeId(1);
        departmentFormType.setKind(FormDataKind.PRIMARY);
        departmentFormType.setDepartmentId(1);

        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
        departmentFormTypes.add(departmentFormType);

        when(departmentFormTypeDao.getFormDestinations(any(Integer.class), any(Integer.class), any(FormDataKind.class), any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(mockUser(1, 1, TARole.ROLE_CONTROL_UNP));

        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(1);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);
        assertTrue(service.checkDestinations(editedFormData.getId(), userInfo, logger));
    }

    @Test(expected = ServiceException.class)
    public void testCheckForDestinations4() {
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);

        FormDataService formDataService = mock(FormDataService.class);
        ReflectionTestUtils.setField(service, "formDataService", formDataService);

        FormType formType = new FormType();
        formType.setId(1);
        // Редактируемая НФ
        FormData editedFormData = new FormData();
        editedFormData.setId(1L);
        editedFormData.setDepartmentId(1);
        editedFormData.setFormType(formType);
        editedFormData.setKind(FormDataKind.PRIMARY);
        editedFormData.setReportPeriodId(1);
        editedFormData.setPeriodOrder(1);
        // Приемник
        FormData formData = new FormData();
        formData.setState(WorkflowState.PREPARED);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setEndDate(new Date());

        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setFormTypeId(1);
        departmentFormType.setKind(FormDataKind.PRIMARY);
        departmentFormType.setDepartmentId(1);

        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
        departmentFormTypes.add(departmentFormType);

        when(departmentFormTypeDao.getFormDestinations(any(Integer.class), any(Integer.class), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);
        when(formDataService.findFormData(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), any(Integer.class), any(Boolean.class))).thenReturn(formData);

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(mockUser(1, 1, TARole.ROLE_CONTROL_UNP));
        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setDepartmentId(1);
        r1.setFormTypeName("summary 1");
        r1.setFormDataKind(FormDataKind.SUMMARY);
        r1.setCreated(true);
        r1.setState(WorkflowState.ACCEPTED);
        destinations.add(r1);
        when(sourceService.getDestinationsInfo(any(FormData.class), any(Boolean.class), any(Boolean.class), any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(destinations);
        service.checkDestinations(editedFormData.getId(), userInfo, new Logger());
    }
}
