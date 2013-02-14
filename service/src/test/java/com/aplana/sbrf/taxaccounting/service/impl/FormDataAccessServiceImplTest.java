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

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
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

	private static final long GOSB_TB1_CREATED_FORMDATA_ID = 13;

	private static final long INACTIVE_FORMDATA_ID = 10;

	private static final int TB1_CONTROL_USER_ID = 1;
	private static final int TB1_OPERATOR_USER_ID = 7;
	
	private static final int BANK_CONTROL_USER_ID = 3;
	private static final int BANK_OPERATOR_USER_ID = 4;
	private static final int BANK_CONTROL_UNP_USER_ID = 5;
	
	private static final int GOSB_CONTROL_USER_ID = 6;

	private static final int REPORT_PERIOD_ACTIVE_ID = 1;
	private static final int REPORT_PERIOD_INACTIVE_ID = 2;
	private static final boolean REPORT_PERIOD_ACTIVE = true;
	private static final boolean REPORT_PERIOD_INACTIVE = false;

	@BeforeClass
	public static void tearUp() {
		FormTemplateDao formTemplateDao = mock(FormTemplateDao.class);
		FormTemplate formTemplate1 = mockFormTemplate(1, 1, TaxType.INCOME, "Тип формы 1");
		when(formTemplateDao.get(1)).thenReturn(formTemplate1);
		FormTemplate formTemplate2 = mockFormTemplate(2, 2, TaxType.INCOME, "Тип формы 2");
		when(formTemplateDao.get(2)).thenReturn(formTemplate2);
		FormTemplate formTemplate3 = mockFormTemplate(3, 3, TaxType.INCOME, "Тип формы 3");
		when(formTemplateDao.get(3)).thenReturn(formTemplate3);		
		
		ReflectionTestUtils.setField(service, "formTemplateDao", formTemplateDao);

		
		DepartmentService departmentService = mock(DepartmentService.class);
		Department d;

		// В тербанках есть формы 1 (консолидированная и сводная) и 3 (выходная)
		List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(TB1_ID, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(TB1_ID, 1, FormDataKind.CONSOLIDATED));
		dfts.add(mockDepartmentFormType(TB1_ID, 3, FormDataKind.ADDITIONAL));
		d = mockDepartment(TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK, dfts);
		when(departmentService.getDepartment(TB1_ID)).thenReturn(d);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(TB2_ID, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(TB2_ID, 1, FormDataKind.CONSOLIDATED));
		dfts.add(mockDepartmentFormType(TB2_ID, 3, FormDataKind.ADDITIONAL));
		d = mockDepartment(TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK, dfts);
		when(departmentService.getDepartment(TB2_ID)).thenReturn(d);

		// В банке есть форма 1 (сводная), 2 (сводная) и 3 (выходная)
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, 2, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, 3, FormDataKind.ADDITIONAL));
		d = mockDepartment(Department.ROOT_BANK_ID, null, DepartmentType.ROOT_BANK, dfts);		
		when(departmentService.getDepartment(Department.ROOT_BANK_ID)).thenReturn(d);

		ReflectionTestUtils.setField(service, "departmentService", departmentService);
		
		// Сводная форма 1 из тербанка 1 является источником для сводной 1 банка
		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(Department.ROOT_BANK_ID, 2, FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getFormDestinations(TB1_ID, 1, FormDataKind.SUMMARY)).thenReturn(dfts);
		
		FormDataDao formDataDao = mock(FormDataDao.class);
		FormData fd;
		
		fd = mockFormData(TB1_CREATED_FORMDATA_ID, TB1_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB1_APPROVED_FORMDATA_ID, TB1_ID, WorkflowState.APPROVED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(TB1_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB1_ACCEPTED_FORMDATA_ID, TB1_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(TB1_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, TB1_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_INACTIVE_ID);
		when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(TB2_CREATED_FORMDATA_ID, TB2_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(TB2_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB2_APPROVED_FORMDATA_ID, TB2_ID, WorkflowState.APPROVED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(TB2_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB2_ACCEPTED_FORMDATA_ID, TB2_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(TB2_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, TB2_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_INACTIVE_ID);
		when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(BANK_CREATED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_PREPARED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.PREPARED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_PREPARED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_APPROVED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.APPROVED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_CREATED_SUMMARY_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_CREATED_SUMMARY_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_SUMMARY_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.SUMMARY, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_ACCEPTED_SUMMARY_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_CREATED_ADDITIONAL_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_CREATED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_PREPARED_ADDITIONAL_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.PREPARED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_PREPARED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(GOSB_TB1_CREATED_FORMDATA_ID, GOSB_TB1_ID, WorkflowState.CREATED, FormDataKind.ADDITIONAL, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.getWithoutRows(GOSB_TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, FormDataKind.ADDITIONAL, REPORT_PERIOD_INACTIVE_ID);
		when(formDataDao.getWithoutRows(INACTIVE_FORMDATA_ID)).thenReturn(fd);
		ReflectionTestUtils.setField(service, "formDataDao", formDataDao);
		
		TAUserDao userDao = mock(TAUserDao.class);
		TAUser user;
		
		user = mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL);
		when(userDao.getUser(TB1_CONTROL_USER_ID)).thenReturn(user);
		user = mockUser(TB1_OPERATOR_USER_ID, TB1_ID, TARole.ROLE_OPERATOR);
		when(userDao.getUser(TB1_OPERATOR_USER_ID)).thenReturn(user);
		user = mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL);
		when(userDao.getUser(BANK_CONTROL_USER_ID)).thenReturn(user);
		user = mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR);
		when(userDao.getUser(BANK_OPERATOR_USER_ID)).thenReturn(user);
		user = mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP);
		when(userDao.getUser(BANK_CONTROL_UNP_USER_ID)).thenReturn(user);
		user = mockUser(GOSB_CONTROL_USER_ID, GOSB_TB1_ID, TARole.ROLE_CONTROL);
		when(userDao.getUser(GOSB_CONTROL_USER_ID)).thenReturn(user);
		ReflectionTestUtils.setField(service, "userDao", userDao);
		
		ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
		ReportPeriod rp;
		rp = mockReportPeriod(REPORT_PERIOD_ACTIVE);
		when(reportPeriodDao.get(REPORT_PERIOD_ACTIVE_ID)).thenReturn(rp);
		rp = mockReportPeriod(REPORT_PERIOD_INACTIVE);
		when(reportPeriodDao.get(REPORT_PERIOD_INACTIVE_ID)).thenReturn(rp);
		ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);
	}

	@Test
	public void testCanReadForFirstLifeCycle(){
	/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

		//Любой пользователь может читать Выходные формы данного жизненного цикла, находящиеся в любом состоянии
		assertTrue(service.canRead(BANK_OPERATOR_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_OPERATOR_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
	}

	@Test
	public void testCanReadForSecondLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));

		//Оператор не имеет прав на чтение НФ данного жизненного цикла
		assertFalse(service.canRead(BANK_OPERATOR_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canRead(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));


	}

	@Test
	public void testCanReadForThirdLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/

		//Контроллер текущего уровня, вышестоящего уровня и контроллер УНП имеют доступ на чтение НФ данного жизненного цикла
		assertTrue(service.canRead(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertTrue(service.canRead(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_UNP_USER_ID, TB1_ACCEPTED_FORMDATA_ID));

		//Оператор не имеет прав на чтение НФ данного жизненного цикла
		assertFalse(service.canRead(TB1_OPERATOR_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canRead(TB1_OPERATOR_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canRead(TB1_OPERATOR_USER_ID, TB1_ACCEPTED_FORMDATA_ID));
	}

	@Test
	public void testCanEditForFirstLifeCycle(){
	/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

		//Все могут редактировать форму в статусе "Создана"
		assertTrue(service.canEdit(BANK_OPERATOR_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));

		//Все, кроме Оператора, могут редактировать форму в статусе "Подготовлена"
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID));

		//Никто не может редактировать НФ в статусе "Принята"
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_ADDITIONAL_FORMDATA_ID));
	}

	@Test
	public void testCanEditForSecondLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Никто не может редактировать налоговые формы данного жизненного цикла
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID));
	}

	@Test
	public void testCanEditForThirdLifeCycle(){
	/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/

		//Контролер текущего уровня, Контролер вышестоящего уровня и Контролер УНП могут редактировать НФ в состоянии "Создана"
		assertTrue(service.canEdit(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, TB1_CREATED_FORMDATA_ID));

		//Оператор не может редактировать НФ данного жизненного цикла в состоянии "Создана"
		assertFalse(service.canEdit(TB1_OPERATOR_USER_ID, TB1_CREATED_FORMDATA_ID));

		//Никто не может редактировать НФ данного жизненного цикла в состоянии "Утверждена" и "Принята"
		assertFalse(service.canEdit(TB1_OPERATOR_USER_ID, TB1_ACCEPTED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_OPERATOR_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, TB1_ACCEPTED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, TB1_APPROVED_FORMDATA_ID));
	}

	@Test
	public void testCanDelete() {
		// Удалять можно налоговые формы, находящиеся в состоянии "Создана" и для которых canEdit() == true
		assertTrue(service.canDelete(BANK_OPERATOR_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canDelete(BANK_CONTROL_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canDelete(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_ADDITIONAL_FORMDATA_ID));
		assertTrue(service.canDelete(BANK_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canDelete(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canDelete(BANK_CONTROL_UNP_USER_ID, TB1_CREATED_FORMDATA_ID));
	}
	
	@Test 
	public void testCanCreateOperator() {
		// Оператор может создавать первичные и выходные в своём подразделении
		assertTrue(service.canCreate(BANK_OPERATOR_USER_ID, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID));
		
		// Оператор не может создавать консолидированные и выходные формы даже в своём
		assertFalse(service.canCreate(BANK_OPERATOR_USER_ID, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
		
		// Оператор не может создавать в чужом подразделении 
		assertFalse(service.canCreate(BANK_OPERATOR_USER_ID, 1, FormDataKind.SUMMARY, TB1_ID));

		// Оператор не может создавать в своём подразделении, если в подразделении не разрешена работа с такой формой
		assertFalse(service.canCreate(BANK_OPERATOR_USER_ID, 2, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
	}
	
	@Test 
	public void testCanCreateControl() {		
		// Контролёр может создавать сводные и консолидированные в своём подразделении
		assertTrue(service.canCreate(BANK_CONTROL_USER_ID, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID));
		assertTrue(service.canCreate(BANK_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
		assertTrue(service.canCreate(TB1_CONTROL_USER_ID, 1, FormDataKind.CONSOLIDATED, TB1_ID));
		
		// Контролёр не может создавать формы, если они не разрешены в подразедении 
		assertFalse(service.canCreate(BANK_CONTROL_USER_ID, 3, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
		
		// Контролёр может создать форму в чужом подразделении, если она является источником для одной из форм его подраздлеления 
		assertFalse(service.canCreate(BANK_OPERATOR_USER_ID, 1, FormDataKind.SUMMARY, TB1_ID));
		
		// TODO: случай, когда форма в чужом подразделении является источником для другой формы в этом же подразделении, а уже эта вторая форма
		// является источником для одной из форм подразделения, к которому относится контролёр
		
		// Во всех остальных случаях контролёр не сможет создавать формы в чужих подразделениях 
		assertFalse(service.canCreate(BANK_OPERATOR_USER_ID, 1, FormDataKind.SUMMARY, TB2_ID));
	}
	
	@Test 
	public void testCanCreateControlUnp() {
		// Контролёр УНП может создавать любую разрешённую налоговую форму, в любом подразделении
		assertTrue(service.canCreate(BANK_CONTROL_UNP_USER_ID, 3, FormDataKind.ADDITIONAL, Department.ROOT_BANK_ID));
		assertTrue(service.canCreate(BANK_CONTROL_UNP_USER_ID, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
		assertTrue(service.canCreate(BANK_CONTROL_UNP_USER_ID, 1, FormDataKind.CONSOLIDATED, TB1_ID));
		// В том числе в чужих
		assertTrue(service.canCreate(BANK_CONTROL_UNP_USER_ID, 1, FormDataKind.SUMMARY, TB1_ID));
		// В том числе и без учёта отношений источник/приёмник
		assertTrue(service.canCreate(BANK_CONTROL_UNP_USER_ID, 1, FormDataKind.SUMMARY, TB2_ID));
		
		// Однако контролёр УНП не может создавать формы, если они не разрешены в подразедении 
		assertFalse(service.canCreate(BANK_CONTROL_UNP_USER_ID, 3, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
	}	

	@Test
	public void testGetAvailableMovesForFirstLifeCycle(){
		/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/

		//Перевести из состояния "Создана" в "Подготовлена" может любой пользователь
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_PREPARED },
				service.getAvailableMoves(BANK_OPERATOR_USER_ID, BANK_CREATED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_PREPARED },
				service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_PREPARED },
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_FORMDATA_ID).toArray());

		//Перевести из состояния "Подготовлена" в "Создана" и из "Подготовлена" в "Принята" может контролер текущего уровня,
		// контролер вышестоящего уровня или контролер УНП.
		assertArrayEquals(new Object[] { WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_ACCEPTED },
				service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_PREPARED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_ACCEPTED },
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, BANK_PREPARED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] {},service.getAvailableMoves(BANK_OPERATOR_USER_ID, BANK_PREPARED_FORMDATA_ID).toArray());

		//Перевести из состояния "Принята" в "Подготовлена" может контролер вышестоящего уровня и контролер УНП.
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_PREPARED},
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] {},service.getAvailableMoves(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] {},service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID).toArray());
	}

	@Test
	public void testGetAvailableMovesForSecondLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/

		//Перевести из состояния "Создана" в "Принята" может контролер текущего уровня, контролер вышестоящего уровня,
		//контролер УНП
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED },
				service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_ACCEPTED },
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(BANK_OPERATOR_USER_ID, BANK_CREATED_SUMMARY_FORMDATA_ID).toArray());

		//Перевести из состояния "Принята" в "Создана" может контролер вышестоящего уровня или контролер УНП
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_CREATED },
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_ACCEPTED_SUMMARY_FORMDATA_ID).toArray());

	}

	@Test
	public void testGetAvailableMovesForThirdLifeCycle(){
		/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/

		//Переводить из состояния "Создана" в "Утверждена" может контролер текущего уровня, контролер вышестоящего уровня
		// и контролер УНП
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, TB1_CREATED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.CREATED_TO_APPROVED },
				service.getAvailableMoves(BANK_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(TB1_OPERATOR_USER_ID, TB1_CREATED_FORMDATA_ID).toArray());

		//Перевести из состояния "Утверждена" в "Создана" и из "Утверждена" в "Принята" контролер вышестоящего уровня или контролер УНП.
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_ACCEPTED, WorkflowMove.APPROVED_TO_CREATED },
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, TB1_APPROVED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.APPROVED_TO_ACCEPTED, WorkflowMove.APPROVED_TO_CREATED },
				service.getAvailableMoves(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(TB1_OPERATOR_USER_ID, TB1_APPROVED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID).toArray());

		//Перевести из состояния "Принята" в "Утверждена" контролер вышестоящего уровня или контролер УНП.
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(BANK_CONTROL_UNP_USER_ID, TB1_ACCEPTED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED},
				service.getAvailableMoves(BANK_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(TB1_OPERATOR_USER_ID, TB1_ACCEPTED_FORMDATA_ID).toArray());
		assertArrayEquals(new Object[] { }, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID).toArray());
	}

	@Test
	public void testGetAvailableMovesCommon() {
		// Контролёр ТБ не может изменять статус в чужом тербанке
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB2_CREATED_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB2_APPROVED_FORMDATA_ID).size());

		// Никто не может выполнять переходы, если отчетный период неактивен
		assertEquals(0, service.getAvailableMoves(BANK_CONTROL_USER_ID, INACTIVE_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, INACTIVE_FORMDATA_ID).size());
	}
	
	@Test
	public void testGetAccessParams() {
		// Проверяем только один случай, так как этот метод просто аггрегирует результаты других методов,
		// а мы их уже оттестировали отдельно
		FormDataAccessParams params = service.getFormDataAccessParams(BANK_CONTROL_USER_ID, BANK_PREPARED_ADDITIONAL_FORMDATA_ID);
		assertTrue(params.isCanRead());
		assertTrue(params.isCanEdit());
		assertFalse(params.isCanDelete());
		assertArrayEquals(
			new Object[] { WorkflowMove.PREPARED_TO_CREATED, WorkflowMove.PREPARED_TO_ACCEPTED },
			params.getAvailableWorkflowMoves().toArray()
		);

	}
}
