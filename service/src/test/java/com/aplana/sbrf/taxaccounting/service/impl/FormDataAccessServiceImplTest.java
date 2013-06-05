package com.aplana.sbrf.taxaccounting.service.impl;

import static com.aplana.sbrf.taxaccounting.test.DepartmentFormTypeMockUtils.mockDepartmentFormType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.FormDataMockUtils.mockFormData;
import static com.aplana.sbrf.taxaccounting.test.FormTemplateMockUtils.mockFormTemplate;
import static com.aplana.sbrf.taxaccounting.test.ReportPeriodMockUtils.mockReportPeriod;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.test.FormTypeMockUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;

public class FormDataAccessServiceImplTest {
	private static FormDataAccessServiceImpl service = new FormDataAccessServiceImpl();
	
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

	private final static String LOCAL_IP = "127.0.0.1";

	@BeforeClass
	public static void tearUp() {
		FormType summaryFormType1 = FormTypeMockUtils.mockFormType(1, TaxType.INCOME, "summary 1");
		FormType summaryFormType2 = FormTypeMockUtils.mockFormType(2, TaxType.INCOME, "summary 2");
		FormType additionalFormType = FormTypeMockUtils.mockFormType(3, TaxType.INCOME, "additional");

		FormTemplateDao formTemplateDao = mock(FormTemplateDao.class);
		FormTemplate formTemplate1 = mockFormTemplate(1, summaryFormType1.getId(), TaxType.INCOME, "Тип формы 1");
		when(formTemplateDao.get(1)).thenReturn(formTemplate1);
		FormTemplate formTemplate2 = mockFormTemplate(2, summaryFormType2.getId(), TaxType.INCOME, "Тип формы 2");
		when(formTemplateDao.get(2)).thenReturn(formTemplate2);
		FormTemplate formTemplate3 = mockFormTemplate(3, additionalFormType.getId(), TaxType.INCOME, "Тип формы 3");
		when(formTemplateDao.get(3)).thenReturn(formTemplate3);		
		
		ReflectionTestUtils.setField(service, "formTemplateDao", formTemplateDao);

		
		DepartmentService departmentService = mock(DepartmentService.class);
		Department d;

		// В тербанках есть формы 1 (консолидированная и сводная) и 3 (выходная)
		List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(TB1_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(TB1_ID, summaryFormType1.getId(), FormDataKind.CONSOLIDATED));
		dfts.add(mockDepartmentFormType(TB1_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
		d = mockDepartment(TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK, dfts);
		when(departmentService.getDepartment(TB1_ID)).thenReturn(d);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(TB2_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(TB2_ID, summaryFormType1.getId(), FormDataKind.CONSOLIDATED));
		dfts.add(mockDepartmentFormType(TB2_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
		d = mockDepartment(TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK, dfts);
		when(departmentService.getDepartment(TB2_ID)).thenReturn(d);

		// В банке есть форма 1 (сводная), 2 (сводная) и 3 (выходная)
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, additionalFormType.getId(), FormDataKind.ADDITIONAL));
		d = mockDepartment(Department.ROOT_BANK_ID, null, DepartmentType.ROOT_BANK, dfts);		
		when(departmentService.getDepartment(Department.ROOT_BANK_ID)).thenReturn(d);

		ReflectionTestUtils.setField(service, "departmentService", departmentService);
		
		// Сводная форма 1 из тербанка 1 является источником для сводной 1 банка
		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType1.getId(), FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, summaryFormType2.getId(), FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getFormDestinations(TB1_ID, summaryFormType1.getId(), FormDataKind.SUMMARY)).thenReturn(dfts);

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

		ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
		ReportPeriod rp;
		rp = mockReportPeriod(REPORT_PERIOD_ACTIVE_ID, REPORT_PERIOD_ACTIVE);
		when(reportPeriodDao.get(REPORT_PERIOD_ACTIVE_ID)).thenReturn(rp);
		rp = mockReportPeriod(REPORT_PERIOD_INACTIVE_ID, REPORT_PERIOD_INACTIVE);
		when(reportPeriodDao.get(REPORT_PERIOD_INACTIVE_ID)).thenReturn(rp);
		rp = mockReportPeriod(REPORT_PERIOD_BALANCED_ID, REPORT_PERIOD_ACTIVE, REPORT_PERIOD_BALANCED);
		when(reportPeriodDao.get(REPORT_PERIOD_BALANCED_ID)).thenReturn(rp);
		ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);
	}

	@Test
	public void testCanReadForFirstLifeCycle(){
	/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Любой пользователь может читать Выходные формы данного жизненного цикла, находящиеся в любом состоянии
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertTrue(service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canRead(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
	}

	@Test
	public void testCanReadForSecondLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));

		//Оператор не имеет прав на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertFalse(service.canRead(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canRead(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
	}

	@Test
	public void testCanReadForThirdLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canRead(userInfo, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canRead(userInfo, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID));
		assertTrue(service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID));

		//Оператор не имеет прав на чтение НФ данного жизненного цикла
		userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPERATOR));
		assertFalse(service.canRead(userInfo, TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canRead(userInfo, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canRead(userInfo, TB1_ACCEPTED_FORMDATA_ID));
	}

	@Test
	public void testCanEditForFirstLifeCycle(){
	/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Все могут редактировать форму в статусе "Создана"
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertTrue(service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canEdit(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));

		//Все, кроме Оператора, могут редактировать форму в статусе "Подготовлена"
		assertTrue(service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertFalse(service.canEdit(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));

		//Никто не может редактировать НФ в статусе "Принята"
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
	}

	@Test
	public void testCanEditForSecondLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Никто не может редактировать налоговые формы данного жизненного цикла
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canEdit(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID)); //TODO (Marat Fayzullin 20.03.2013) временно до появления первичных форм
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertFalse(service.canEdit(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));

	}

	@Test
	public void testCanEditForThirdLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		//Контролер текущего уровня, Контролер вышестоящего уровня и Контролер УНП могут редактировать НФ в состоянии "Создана"
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID));

		//Оператор не может редактировать НФ данного жизненного цикла в состоянии "Создана"
		userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPERATOR));
		assertFalse(service.canEdit(userInfo, TB1_CREATED_FORMDATA_ID));

		//Никто не может редактировать НФ данного жизненного цикла в состоянии "Утверждена" и "Принята"
		assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertFalse(service.canEdit(userInfo, TB1_ACCEPTED_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(service.canEdit(userInfo, TB1_APPROVED_FORMDATA_ID));
	}

	@Test
	public void testCanDelete() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Удалять можно налоговые формы, находящиеся в состоянии "Создана" и для которых canEdit() == true
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertTrue(service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertFalse(service.canDelete(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID));
		assertFalse(service.canDelete(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID));
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(service.canDelete(userInfo, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canDelete(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID));
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canDelete(userInfo, TB1_CREATED_FORMDATA_ID));
	}
	
	@Test 
	public void testCanCreateOperator() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));

		// Оператор может создавать первичные и выходные в своём подразделении
		assertTrue(service.canCreate(userInfo, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));
		
		// Оператор не может создавать консолидированные и выходные формы даже в своём
		assertFalse(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));
		
		// Оператор не может создавать в чужом подразделении 
		assertFalse(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_ACTIVE_ID));

		// Оператор не может создавать в своём подразделении, если в подразделении не разрешена работа с такой формой
		assertFalse(service.canCreate(userInfo, 2, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		assertFalse(service.canCreate(userInfo, 2, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_BALANCED_ID));
	}
	
	@Test 
	public void testCanCreateControl() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));

		// Контролёр может создавать выходные формы
		assertTrue(service.canCreate(userInfo, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
		//TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр не может создавать формы, если они не разрешены в подразделении
		assertFalse(service.canCreate(userInfo, 3, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
		//TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр может создать форму в чужом подразделении, если она является источником для одной из форм его подраздлеления 
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertFalse(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_ACTIVE_ID));
		
		// TODO: случай, когда форма в чужом подразделении является источником для другой формы в этом же подразделении, а уже эта вторая форма
		// является источником для одной из форм подразделения, к которому относится контролёр
		
		// Во всех остальных случаях контролёр не сможет создавать формы в чужих подразделениях 
		assertFalse(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, TB2_ID, REPORT_PERIOD_ACTIVE_ID));
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_BALANCED_ID));
	}
	
	@Test 
	public void testCanCreateControlUnp() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));

		// Контролёр УНП может создавать любую разрешённую налоговую форму, в любом подразделении
		assertTrue(service.canCreate(userInfo, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр УНП не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
		//TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр УНП не может создавать консолидированные и сводные, не передающиеся в вышестоящее подразделение
		//TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм. Правильно assertFalse
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.CONSOLIDATED, TB1_ID, REPORT_PERIOD_ACTIVE_ID));
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, TB2_ID, REPORT_PERIOD_ACTIVE_ID));

		// Контролёр УНП может создавать консолидированные и сводные, передающиеся в вышестоящее подразделение
		assertTrue(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_ACTIVE_ID));

		// Однако контролёр УНП не может создавать формы, если они не разрешены в подразедении 
		assertFalse(service.canCreate(userInfo, 3, FormDataKind.SUMMARY, Department.ROOT_BANK_ID, REPORT_PERIOD_ACTIVE_ID));

		assertTrue(service.canCreate(userInfo, 1, FormDataKind.SUMMARY, TB1_ID, REPORT_PERIOD_BALANCED_ID));
	}	

	@Test
	public void testGetAvailableMovesForFirstLifeCycle(){
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Перевести из состояния "Создана" в "Подготовлена" может любой пользователь
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_PREPARED },
				service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_PREPARED },
				service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_PREPARED },
				service.getAvailableMoves(userInfo, BANK_CREATED_FORMDATA_ID).toArray());

		//Перевести из состояния "Подготовлена" в "Создана" и из "Подготовлена" в "Принята" может контролер текущего уровня,
		// контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.PREPARED_TO_ACCEPTED, WorkflowMove.PREPARED_TO_CREATED },
				service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.PREPARED_TO_ACCEPTED, WorkflowMove.PREPARED_TO_CREATED },
				service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] {},service.getAvailableMoves(userInfo, BANK_PREPARED_FORMDATA_ID).toArray());

		//Перевести из состояния "Принята" в "Подготовлена" может контролер вышестоящего уровня и контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_PREPARED},
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] {},service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] {},service.getAvailableMoves(userInfo, BANK_ACCEPTED_FORMDATA_ID).toArray());
	}

	@Test
	public void testGetAvailableMovesForSecondLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		//Перевести из состояния "Создана" в "Принята" может контролер текущего уровня, контролер УНП
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED },
				service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED },
				service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());

		//Перевести из состояния "Принята" в "Создана" может текущий контролер или контролер УНП
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED },
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED},
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_CREATED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED},
				service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { },
				service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_BALANCED_ID).toArray());
		assertArrayEquals(new Object[] { },
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_BALANCED_ID).toArray());

	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/

		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

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
		userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_CREATED_FORMDATA_ID).toArray());

		//Перевести из состояния "Утверждена" в "Создана" и из "Утверждена" в "Принята" контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_ACCEPTED, WorkflowMove.APPROVED_TO_CREATED },
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_ACCEPTED, WorkflowMove.APPROVED_TO_CREATED },
				service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_APPROVED_FORMDATA_ID).toArray());

		//Перевести из состояния "Принята" в "Утверждена" контролер вышестоящего уровня или контролер УНП.
		userInfo.setUser(mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPERATOR));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
		userInfo.setUser(mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL));
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(userInfo, TB1_ACCEPTED_FORMDATA_ID).toArray());
	}

	@Test
	public void testGetAvailableMovesCommon() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
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
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		// Проверяем только один случай, так как этот метод просто аггрегирует результаты других методов,
		// а мы их уже оттестировали отдельно
		FormDataAccessParams params = service.getFormDataAccessParams(userInfo, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
		assertTrue(params.isCanRead());
		assertTrue(params.isCanEdit());
		assertFalse(params.isCanDelete());
		assertArrayEquals(
			new Object[] {WorkflowMove.PREPARED_TO_ACCEPTED, WorkflowMove.PREPARED_TO_CREATED},
			params.getAvailableWorkflowMoves().toArray()
		);
	}
}
