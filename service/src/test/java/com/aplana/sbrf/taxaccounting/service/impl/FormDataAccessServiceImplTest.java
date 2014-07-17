package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.test.FormTypeMockUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentFormTypeMockUtils.mockDepartmentFormType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.FormDataMockUtils.mockFormData;
import static com.aplana.sbrf.taxaccounting.test.FormTemplateMockUtils.mockFormTemplate;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormDataAccessServiceImplTest {
	private static FormDataAccessServiceImpl service = new FormDataAccessServiceImpl();

	private static final TAUserInfo userInfo = new TAUserInfo(){{setIp(LOCAL_IP);}};

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

	private static final long GOSB_TB1_CREATED_FORMDATA_ID = 13;

	private static final long INACTIVE_FORMDATA_ID = 10;

	private static final int TB1_CONTROL_USER_ID = 1;
	private static final int TB1_OPERATOR_USER_ID = 7;
	
	private static final int BANK_CONTROL_USER_ID = 3;
	private static final int BANK_OPERATOR_USER_ID = 4;
	private static final int BANK_CONTROL_UNP_USER_ID = 5;
	
	private static final int REPORT_PERIOD_ACTIVE_ID = 1;
	private static final int REPORT_PERIOD_INACTIVE_ID = 2;
	private static final int REPORT_PERIOD_BALANCED_ID = 3;
	private static final boolean REPORT_PERIOD_ACTIVE = true;
	private static final boolean REPORT_PERIOD_INACTIVE = false;
	private static final boolean REPORT_PERIOD_BALANCED = true;

    private static final int DECLARATION_TYPE_1_ID = 101;

	private final static String LOCAL_IP = "127.0.0.1";

	@BeforeClass
	public static void tearUp() {
		FormType summaryFormType1 = FormTypeMockUtils.mockFormType(1, TaxType.INCOME, "summary 1");
		FormType summaryFormType2 = FormTypeMockUtils.mockFormType(2, TaxType.INCOME, "summary 2");
		FormType additionalFormType = FormTypeMockUtils.mockFormType(3, TaxType.INCOME, "additional");

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

        ReflectionTestUtils.setField(service, "formTemplateDao", formTemplateDao);
		
		final DepartmentService departmentService = mock(DepartmentService.class);
        final FormTemplateService formTemplateService = mock(FormTemplateService.class);
        ReflectionTestUtils.setField(service, "formTemplateService", formTemplateService);
        when(formTemplateService.getNearestFTRight(1)).thenReturn(formTemplate1);
        when(formTemplateService.getNearestFTRight(3)).thenReturn(formTemplate3);
		Department d;

		// В тербанках есть формы 1 (консолидированная и сводная) и 3 (выходная)

		d = mockDepartment(TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERR_BANK);
		when(departmentService.getDepartment(TB1_ID)).thenReturn(d);
		

		d = mockDepartment(TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERR_BANK);
		when(departmentService.getDepartment(TB2_ID)).thenReturn(d);

		// В банке есть форма 1 (сводная), 2 (сводная) и 3 (выходная)
		d = mockDepartment(Department.ROOT_BANK_ID, null, DepartmentType.ROOT_BANK);		
		when(departmentService.getDepartment(Department.ROOT_BANK_ID)).thenReturn(d);

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
                            retVal.add(Department.ROOT_BANK_ID);
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
		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getFormDestinations(any(Integer.class), any(Integer.class), any(FormDataKind.class), any(Date.class), any(Date.class))).thenReturn(dfts);

		ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
		
		FormDataDao formDataDao = mock(FormDataDao.class);
		FormData fd;
		
		fd = mockFormData(TB1_CREATED_FORMDATA_ID, TB1_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB1_APPROVED_FORMDATA_ID, TB1_ID, WorkflowState.APPROVED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB1_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB1_ACCEPTED_FORMDATA_ID, TB1_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB1_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, TB1_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_INACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(TB2_CREATED_FORMDATA_ID, TB2_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB2_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB2_APPROVED_FORMDATA_ID, TB2_ID, WorkflowState.APPROVED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB2_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB2_ACCEPTED_FORMDATA_ID, TB2_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB2_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, TB2_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_INACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(BANK_CREATED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_PREPARED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.PREPARED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_PREPARED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_APPROVED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.APPROVED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_CREATED_SUMMARY_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(BANK_CREATED_SUMMARY_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_SUMMARY_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(BANK_ACCEPTED_SUMMARY_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_CREATED_ADDITIONAL_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_CREATED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_PREPARED_ADDITIONAL_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.PREPARED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_PREPARED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(GOSB_TB1_CREATED_FORMDATA_ID, GOSB_TB1_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(GOSB_TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, REPORT_PERIOD_INACTIVE_ID, additionalFormType);
		when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(TB1_CREATED_FORMDATA_BALANCED_ID, TB1_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_BALANCED_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB1_CREATED_FORMDATA_BALANCED_ID)).thenReturn(fd);
		fd = mockFormData(TB1_ACCEPTED_FORMDATA_BALANCED_ID, TB1_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_BALANCED_ID, summaryFormType1);
		when(formDataDao.getWithoutRows(TB1_ACCEPTED_FORMDATA_BALANCED_ID)).thenReturn(fd);
		ReflectionTestUtils.setField(service, "formDataDao", formDataDao);

//		ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
//		ReportPeriod rp;
//		rp = mockReportPeriod(REPORT_PERIOD_ACTIVE_ID);
//		when(reportPeriodDao.get(REPORT_PERIOD_ACTIVE_ID)).thenReturn(rp);
//		rp = mockReportPeriod(REPORT_PERIOD_INACTIVE_ID);
//		when(reportPeriodDao.get(REPORT_PERIOD_INACTIVE_ID)).thenReturn(rp);
//		rp = mockReportPeriod(REPORT_PERIOD_BALANCED_ID);
//		when(reportPeriodDao.get(REPORT_PERIOD_BALANCED_ID)).thenReturn(rp);
//		ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);

		PeriodService reportPeriodService = mock(PeriodService.class);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_ACTIVE_ID, TB1_ID)).thenReturn(REPORT_PERIOD_ACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_ACTIVE_ID, TB1_ID)).thenReturn(false);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_ACTIVE_ID, TB2_ID)).thenReturn(REPORT_PERIOD_ACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_ACTIVE_ID, TB2_ID)).thenReturn(false);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_ACTIVE_ID, Department.ROOT_BANK_ID)).thenReturn(REPORT_PERIOD_ACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_ACTIVE_ID, Department.ROOT_BANK_ID)).thenReturn(false);

		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_INACTIVE_ID, TB1_ID)).thenReturn(REPORT_PERIOD_INACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_INACTIVE_ID, TB1_ID)).thenReturn(false);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_INACTIVE_ID, TB2_ID)).thenReturn(REPORT_PERIOD_INACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_INACTIVE_ID, TB2_ID)).thenReturn(false);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_INACTIVE_ID, Department.ROOT_BANK_ID)).thenReturn(REPORT_PERIOD_INACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_INACTIVE_ID, Department.ROOT_BANK_ID)).thenReturn(false);

		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_BALANCED_ID, TB1_ID)).thenReturn(REPORT_PERIOD_ACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_BALANCED_ID, TB1_ID)).thenReturn(REPORT_PERIOD_BALANCED);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_BALANCED_ID, TB2_ID)).thenReturn(REPORT_PERIOD_ACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_BALANCED_ID, TB2_ID)).thenReturn(REPORT_PERIOD_BALANCED);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_BALANCED_ID, Department.ROOT_BANK_ID)).thenReturn(REPORT_PERIOD_ACTIVE);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_BALANCED_ID, Department.ROOT_BANK_ID)).thenReturn(REPORT_PERIOD_BALANCED);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setStartDate(new Date(456256245));
        reportPeriod.setEndDate(new Date(908686433));
        when(reportPeriodService.getReportPeriod(REPORT_PERIOD_ACTIVE_ID)).thenReturn(reportPeriod);
        when(reportPeriodService.getReportPeriod(REPORT_PERIOD_BALANCED_ID)).thenReturn(reportPeriod);
		ReflectionTestUtils.setField(service, "reportPeriodService", reportPeriodService);
		
		SourceService sourceService = mock(SourceService.class);
		dfts.add(mockDepartmentFormType(TB1_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(TB1_ID, summaryFormType1.getId(), FormDataKind.CONSOLIDATED));
		dfts.add(mockDepartmentFormType(TB1_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
		when(sourceService.getDFTByDepartment(Matchers.eq(TB1_ID), Matchers.any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(dfts);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(TB2_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(TB2_ID, summaryFormType1.getId(), FormDataKind.CONSOLIDATED));
		dfts.add(mockDepartmentFormType(TB2_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
		when(sourceService.getDFTByDepartment(Matchers.eq(TB2_ID), Matchers.any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(dfts);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
		when(sourceService.getDFTByDepartment(Matchers.eq(Department.ROOT_BANK_ID), Matchers.any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(dfts);
        List<DepartmentDeclarationType> ddtList = new ArrayList<DepartmentDeclarationType>();
        when(sourceService.getDeclarationDestinations(Department.ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY, 1)).thenReturn(ddtList);
        when(sourceService.getDeclarationDestinations(Department.ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY, 1)).thenReturn(null);
		ReflectionTestUtils.setField(service, "sourceService", sourceService);
		
		FormTypeDao formTypeDao = mock(FormTypeDao.class);
		FormType ft = new FormType();
		ft.setTaxType(TaxType.INCOME);
		when(formTypeDao.get(Matchers.anyInt())).thenReturn(ft);
		ReflectionTestUtils.setField(service, "formTypeDao", formTypeDao);

        ddtList.add(mockDepartmentDeclarationType(Department.ROOT_BANK_ID, DECLARATION_TYPE_1_ID));

        DeclarationDataDao declarationDataDao = mock(DeclarationDataDao.class);
        DeclarationData declarationData = new DeclarationData();
        declarationData.setAccepted(false);
        declarationData.setDepartmentId(TB1_ID);
        when(declarationDataDao.find(DECLARATION_TYPE_1_ID, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID)).thenReturn(declarationData);
        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);

        FormDataService formDataService = mock(FormDataService.class);
        fd = mockFormData(TB1_CREATED_FORMDATA_ID, TB1_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID, summaryFormType1);
        when(formDataService.findFormData(summaryFormType1.getId(), FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID, null)).thenReturn(fd);
		ReflectionTestUtils.setField(service, "formDataService", formDataService);
	}

    @Test
	public void testCanReadForFirstLifeCycle(){
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/
		//Любой пользователь может читать Выходные формы данного жизненного цикла, находящиеся в любом состоянии
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID);
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID);
	}

	@Test(expected = AccessDeniedException.class)
	public void testCanReadForSecondLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID);

		//Оператор не имеет прав на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID);
        service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID);
	}

	@Test(expected = AccessDeniedException.class)
	public void testCanReadForThirdLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/
		//Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        service.canRead(userInfo, TB1_CREATED_FORMDATA_ID);
        service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID);
        service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
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
	public void testCanEditForFirstLifeCycle(){
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

		//Все могут редактировать форму в статусе "Создана"
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID, false);
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID, false);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID, false);

		//Все, кроме Оператора, могут редактировать форму в статусе "Подготовлена"
        service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID, false);
        service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID, false);
		/*userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
		assertFalse(service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));*/

		//Никто не может редактировать НФ в статусе "Принята"
		/*assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));*/
	}

	@Test
	public void testCanEditForSecondLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Никто не может редактировать налоговые формы данного жизненного цикла
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID, false); //TODO (Marat Fayzullin 20.03.2013) временно до появления первичных форм
		/*assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
		assertFalse(service.canEdit(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));*/

	}

	@Test
	public void testCanEditForThirdLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/

		//Контролер текущего уровня, Контролер вышестоящего уровня и Контролер УНП могут редактировать НФ в состоянии "Создана"
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID, false);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID, false);


	}

    @Test(expected = AccessDeniedException.class)
    public void testCanEditForThirdLifeCycleFalseResult(){
        //Оператор не может редактировать НФ данного жизненного цикла в состоянии "Создана"
        userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPER));
        service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID, false);

        //Никто не может редактировать НФ данного жизненного цикла в состоянии "Утверждена" и "Принята"
        /*assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));*/
    }

	@Test
	public void testCanDelete() {
		// Удалять можно налоговые формы, находящиеся в состоянии "Создана" и для которых canEdit() == true
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
		/*assertFalse(service.canDelete(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID));
		assertFalse(service.canDelete(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID));*/
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID);
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID);
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID);
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID);
	}
	
	@Test 
	public void testCanCreateOperator() {
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));

		// Оператор может создавать первичные и выходные в своём подразделении
		assertTrue(checkFail(userInfo, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Оператор не может создавать консолидированные и выходные формы даже в своём
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Оператор не может создавать в чужом подразделении 
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_ACTIVE_ID));

		// Оператор не может создавать в своём подразделении, если в подразделении не разрешена работа с такой формой
		assertFalse(checkFail(userInfo, 2, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));
		assertFalse(checkFail(userInfo, 2, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_BALANCED_ID));
	}
	
	@Test 
	public void testCanCreateControl() {
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));

		// Контролёр может создавать выходные формы
		assertTrue(checkFail(userInfo, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
		//TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр не может создавать формы, если они не разрешены в подразделении
		assertFalse(checkFail(userInfo, 3, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
		//TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertTrue(checkFail(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр может создать форму в чужом подразделении, если она является источником для одной из форм его подраздлеления 
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_ACTIVE_ID));
		
		// TODO: случай, когда форма в чужом подразделении является источником для другой формы в этом же подразделении, а уже эта вторая форма
		// является источником для одной из форм подразделения, к которому относится контролёр
		
		// Во всех остальных случаях контролёр не сможет создавать формы в чужих подразделениях 
		assertFalse(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB2_ID, REPORT_PERIOD_ACTIVE_ID));
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
        assertTrue(checkFail(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_BALANCED_ID));
	}
	
	@Test 
	public void testCanCreateControlUnp() {
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));

		// Контролёр УНП может создавать любую разрешённую налоговую форму, в любом подразделении
        assertTrue(checkFail(userInfo, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр УНП может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

        // Контролёр УНП не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
        assertTrue(checkFail(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_ACTIVE_ID));
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB2_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр УНП может создавать консолидированные и сводные, передающиеся в вышестоящее подразделение
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_ACTIVE_ID));

		// Однако контролёр УНП не может создавать формы, если они не разрешены в подразедении 
        assertFalse(checkFail(userInfo, 3, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));
        assertTrue(checkFail(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_BALANCED_ID));
	}

    @Test
    public void testGetAvailableMovesForFirstLifeCycle() {
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
             и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

        //Перевести из состояния "Создана" в "Подготовлена" может любой пользователь
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        assertArrayEquals(new Object[]{WorkflowMove.CREATED_TO_PREPARED},
                service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertArrayEquals(new Object[]{WorkflowMove.CREATED_TO_PREPARED},
                service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[]{WorkflowMove.CREATED_TO_PREPARED},
                service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());

        //Перевести из состояния "Подготовлена" в "Создана" и из "Подготовлена" в "Принята" может контролер текущего уровня,
        // контролер вышестоящего уровня или контролер УНП.
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertArrayEquals(new Object[]{WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_APPROVED},
                service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[]{WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_APPROVED},
                service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());

        //Перевести из состояния "Принята" в "Подготовлена" может контролер вышестоящего уровня и контролер УНП.
        userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[]{WorkflowMove.ACCEPTED_TO_APPROVED},
                service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        assertArrayEquals(new Object[]{}, service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
    }

	@Test
	public void testGetAvailableMovesForSecondLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Перевести из состояния "Создана" в "Принята" может контролер текущего уровня, контролер УНП
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
		/*assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());*/

		//Перевести из состояния "Принята" в "Создана" может текущий контролер или контролер УНП
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED },
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED},
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
/*		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED},
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());*/
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
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
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPER));
		/*assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());*/
	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle2(){
		// Перевести из состояния "Утверждена" в "Создана" и из "Утверждена" в "Принята" контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
        assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED, WorkflowMove.APPROVED_TO_ACCEPTED },
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED, WorkflowMove.APPROVED_TO_ACCEPTED },
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());

		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED},
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle3(){
		// Перевести из состояния "Принята" в "Утверждена" контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
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
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertEquals(0, service.getAvailableMoves(userInfo, INACTIVE_FORMDATA_ID).size());
	}
	
	@Test
	public void testGetAccessParams() {
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		// Проверяем только один случай, так как этот метод просто агрегирует результаты других методов,
		// а мы их уже оттестировали отдельно
		FormDataAccessParams params = service.getFormDataAccessParams(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID, false);
		assertTrue(params.isCanRead());
		assertTrue(params.isCanEdit());
		assertFalse(params.isCanDelete());
		assertArrayEquals(
			new Object[] {WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_APPROVED},
			params.getAvailableWorkflowMoves().toArray()
		);
	}

    /**
     * Проверяет выбрасывается ли ошибка прав доступа и т.д.. Необходим для совместимости написанных тестов и изменениями
     * сделанными в http://jira.aplana.com/browse/SBRFACCTAX-3939.
     * @param userInfo
     * @param formTemplateId
     * @param kind
     * @param departmentId
     * @param reportPeriodId
     * @return true если не было ошибки
     */
    private boolean checkFail(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentId, int reportPeriodId){
        boolean isThrown = false;
        try{
            service.canCreate(userInfo, formTemplateId, kind, departmentId, reportPeriodId);
            isThrown = true;
        }catch (ServiceException se){
        }

        return isThrown;
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
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        List<FormDataKind> list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 1);
        assertTrue(kinds.equals(list));

        /* Для контролеров */
        kinds.addAll(controlKinds);
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 3);
        assertTrue(kinds.equals(list));

        /* Для налога на прибыль */
        taxType.clear();
        taxType.add(TaxType.INCOME);

        /* Для контролеров */
        kinds.addAll(taxTypeKinds);
        userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
        list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 5);
        assertTrue(kinds.equals(list));
        /* Для оператора */
        kinds.removeAll(controlKinds);
        userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPER));
        list = service.getAvailableFormDataKind(userInfo, taxType);
        assertTrue(list.size() == 3);
        assertTrue(kinds.equals(list));
    }
}
