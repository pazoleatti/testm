package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.FormDataMockUtils.mockFormData;
import static com.aplana.sbrf.taxaccounting.test.ReportPeriodMockUtils.mockReportPeriod;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

	private static final long GOSB_TB1_CREATED_FORMDATA_ID = 13;



	private static final long INACTIVE_FORMDATA_ID = 10;

	private static final int TB1_CONTROL_USER_ID = 1;
	private static final int BANK_CONTROL_USER_ID = 3;
	private static final int BANK_OPERATOR_USER_ID = 4;
	private static final int BANK_CONTROL_UNP_USER_ID = 5;
	private static final int GOSBANK_CONTROL_USER_ID = 6;

	private static final int REPORT_PERIOD_ACTIVE_ID = 1;
	private static final int REPORT_PERIOD_INACTIVE_ID = 2;
	private static final boolean REPORT_PERIOD_ACTIVE = true;
	private static final boolean REPORT_PERIOD_INACTIVE = false;

	@BeforeClass
	public static void tearUp() {
		FormDataDao formDataDao = mock(FormDataDao.class);
		FormData fd;
		
		fd = mockFormData(TB1_CREATED_FORMDATA_ID, TB1_ID, WorkflowState.CREATED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB1_APPROVED_FORMDATA_ID, TB1_ID, WorkflowState.APPROVED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(TB1_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB1_ACCEPTED_FORMDATA_ID, TB1_ID, WorkflowState.ACCEPTED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(TB1_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, TB1_ID, WorkflowState.ACCEPTED, REPORT_PERIOD_INACTIVE_ID);
		when(formDataDao.get(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(TB2_CREATED_FORMDATA_ID, TB2_ID, WorkflowState.CREATED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(TB2_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB2_APPROVED_FORMDATA_ID, TB2_ID, WorkflowState.APPROVED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(TB2_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(TB2_ACCEPTED_FORMDATA_ID, TB2_ID, WorkflowState.ACCEPTED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(TB2_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, TB2_ID, WorkflowState.ACCEPTED, REPORT_PERIOD_INACTIVE_ID);
		when(formDataDao.get(INACTIVE_FORMDATA_ID)).thenReturn(fd);

		fd = mockFormData(BANK_CREATED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.CREATED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(BANK_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_ACCEPTED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(BANK_ACCEPTED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_PREPARED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.PREPARED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(BANK_PREPARED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(BANK_APPROVED_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.APPROVED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(BANK_APPROVED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(GOSB_TB1_CREATED_FORMDATA_ID, GOSB_TB1_ID, WorkflowState.CREATED, REPORT_PERIOD_ACTIVE_ID);
		when(formDataDao.get(GOSB_TB1_CREATED_FORMDATA_ID)).thenReturn(fd);
		fd = mockFormData(INACTIVE_FORMDATA_ID, Department.ROOT_BANK_ID, WorkflowState.ACCEPTED, REPORT_PERIOD_INACTIVE_ID);
		when(formDataDao.get(INACTIVE_FORMDATA_ID)).thenReturn(fd);
		ReflectionTestUtils.setField(service, "formDataDao", formDataDao);
		
		TAUserDao userDao = mock(TAUserDao.class);
		TAUser user;
		
		user = mockUser(TB1_CONTROL_USER_ID, TB1_ID, TARole.ROLE_CONTROL);
		when(userDao.getUser(TB1_CONTROL_USER_ID)).thenReturn(user);
		user = mockUser(BANK_CONTROL_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL);
		when(userDao.getUser(BANK_CONTROL_USER_ID)).thenReturn(user);
		user = mockUser(BANK_OPERATOR_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_OPERATOR);
		when(userDao.getUser(BANK_OPERATOR_USER_ID)).thenReturn(user);
		user = mockUser(BANK_CONTROL_UNP_USER_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL_UNP);
		when(userDao.getUser(BANK_CONTROL_UNP_USER_ID)).thenReturn(user);
		user = mockUser(GOSBANK_CONTROL_USER_ID, GOSB_TB1_ID, TARole.ROLE_CONTROL);
		when(userDao.getUser(GOSBANK_CONTROL_USER_ID)).thenReturn(user);
		ReflectionTestUtils.setField(service, "userDao", userDao);
		
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		Department d;
		d = mockDepartment(TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);
		when(departmentDao.getDepartment(TB1_ID)).thenReturn(d);
		d = mockDepartment(TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);
		when(departmentDao.getDepartment(TB2_ID)).thenReturn(d);
		d = mockDepartment(GOSB_TB1_ID, TB1_ID, DepartmentType.GOSB);
		when(departmentDao.getDepartment(GOSB_TB1_ID)).thenReturn(d);
		d = mockDepartment(Department.ROOT_BANK_ID, DepartmentType.ROOT_BANK);
		when(departmentDao.getDepartment(Department.ROOT_BANK_ID)).thenReturn(d);

		ReflectionTestUtils.setField(service, "departmentDao", departmentDao);

		ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
		ReportPeriod rp;
		rp = mockReportPeriod(REPORT_PERIOD_ACTIVE);
		when(reportPeriodDao.get(REPORT_PERIOD_ACTIVE_ID)).thenReturn(rp);
		rp = mockReportPeriod(REPORT_PERIOD_INACTIVE);
		when(reportPeriodDao.get(REPORT_PERIOD_INACTIVE_ID)).thenReturn(rp);
		ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);
	}
	
	@Test
	public void testCanRead() {
		// Контролёр тербанка может просматривать любые записи в своём тербанке
		assertTrue(service.canRead(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertTrue(service.canRead(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));

		// Контролёр тербанка не может просматривать записи в чужом тербанке
		assertFalse(service.canRead(TB1_CONTROL_USER_ID, TB2_CREATED_FORMDATA_ID));
		assertFalse(service.canRead(TB1_CONTROL_USER_ID, TB2_APPROVED_FORMDATA_ID));
		assertFalse(service.canRead(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID));

		// Контролёр тербанка не может просматривать записи в вышестоящем уровне
		assertFalse(service.canRead(TB1_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertFalse(service.canRead(TB1_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID));
		
		// Контролёр уровня банка может просматривать записи в банке
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID));

		// Контролёр уровня банка может просматривать записи в тербанках
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertTrue(service.canRead(BANK_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));
	}

	@Test
	public void testOperatorCanEdit() {
		// Оператор может редактировать НФ в статусе "Создана" и только принадлежащую назначенному подразделению пользователя
		assertTrue(service.canEdit(BANK_OPERATOR_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, TB2_CREATED_FORMDATA_ID));

		// Оператор любого уровня не может редактировать НФ в статусах "Подготовлена", "Утверждена", "Принята"
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_PREPARED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_OPERATOR_USER_ID, BANK_ACCEPTED_FORMDATA_ID));
	}
	
	@Test
	public void testControllerCanEdit() {
		// Контролёр тербанка может редактировать записи в тербанке в статусе "Создана" 
		assertTrue(service.canEdit(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));

		// Контролёр тербанка не может редактировать записи в чужом тербанке
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB2_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB2_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID));

		// Контролёр тербанка не может редактировать записи на вышестоящем уровне
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID));
		
		// Контролёр уровня банка может редактировать записи в тербанке в статусе "Создана" и "Утверждена"
		assertTrue(service.canEdit(BANK_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));

		// Контролёр уровня банка может редактировать записи в тербанке в статусе "Создана"
		assertTrue(service.canEdit(BANK_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID));

		// Контролер уровня Госбанка, может редактировать записи в госбанке
		assertTrue(service.canEdit(GOSBANK_CONTROL_USER_ID, GOSB_TB1_CREATED_FORMDATA_ID));

		// Контролер уровня Тербанка, может редактировать записи в госбанке
		assertTrue(service.canEdit(TB1_CONTROL_USER_ID, GOSB_TB1_CREATED_FORMDATA_ID));

		// Контролер уровня Банка, не может редактировать записи в госбанке
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, GOSB_TB1_CREATED_FORMDATA_ID));

		// Никто не может редактировать записи, если отчетный период не активный
		assertFalse(service.canEdit(BANK_CONTROL_USER_ID, INACTIVE_FORMDATA_ID));
		assertFalse(service.canEdit(TB1_CONTROL_USER_ID, INACTIVE_FORMDATA_ID));
	}

	@Test
	public void testControllerUNPCanEdit() {
		// Контролер УНП может редактировать налогувую форму в любом состоянии (кроме "Принята") и любого подразделения
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_PREPARED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_APPROVED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, TB2_CREATED_FORMDATA_ID));
		assertTrue(service.canEdit(BANK_CONTROL_UNP_USER_ID, GOSB_TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canEdit(BANK_CONTROL_UNP_USER_ID, BANK_ACCEPTED_FORMDATA_ID));

	}

	@Test
	public void testCanDelete() {
		// Контролёр тербанка может удалить налоговую форму своего банка в статусе "Создана"
		assertTrue(service.canDelete(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));		
		
		// Контролёр тербанка не может удалить налоговую форму в чужом тербанке
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, TB2_CREATED_FORMDATA_ID));
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, TB2_APPROVED_FORMDATA_ID));
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID));		
		
		// Контролёр тербанка не может удалить налоговую форму на вышестоящем уровне
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID));
	
		// Контролёр банка может удалить налоговую форму на уровне банка в статусе "Создана"
		assertTrue(service.canDelete(BANK_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID));
		assertFalse(service.canDelete(BANK_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID));
		
		// Контролёр банка может удалить налоговую форму на уровне тербанка в статусе "Создана"
		assertTrue(service.canDelete(BANK_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID));
		assertFalse(service.canDelete(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID));
		assertFalse(service.canDelete(BANK_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID));

		// Никто не может удалять записи, если отчетный период неактивен
		assertFalse(service.canDelete(BANK_CONTROL_USER_ID, INACTIVE_FORMDATA_ID));
		assertFalse(service.canDelete(TB1_CONTROL_USER_ID, INACTIVE_FORMDATA_ID));
	}
	
	@Test 
	public void testCanCreate() {
		// Контролёр уровня банка может создавать формы в своём тербанке
		assertTrue(service.canCreate(TB1_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, TB1_ID));
		assertFalse(service.canCreate(TB1_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, TB2_ID));
		assertFalse(service.canCreate(TB1_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));

		// Контролёр уровня банка может создавать формы в банке и тербанках
		assertTrue(service.canCreate(BANK_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, TB1_ID));
		assertTrue(service.canCreate(BANK_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, TB2_ID));
		assertTrue(service.canCreate(BANK_CONTROL_USER_ID, 1, FormDataKind.SUMMARY, Department.ROOT_BANK_ID));
	}
	
	@Test
	public void testGetAvailableMoves() {
		
		// Контролёр ТБ может утрверждать и отменять утверждение
		assertArrayEquals(
			new Object[] { WorkflowMove.CREATED_TO_APPROVED }, 
			service.getAvailableMoves(TB1_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID).toArray()
		);		
		assertArrayEquals(
			new Object[] { WorkflowMove.APPROVED_TO_CREATED }, 
			service.getAvailableMoves(TB1_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID).toArray()
		);		
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID).size());
		
		// Контролёр ТБ не может изменять статус в чужом тербанке
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, TB2_ACCEPTED_FORMDATA_ID).size());
		
		// Контролёр Банка может выполнять все переходы на уровне тербанка 
		assertArrayEquals(
			new Object[] { WorkflowMove.CREATED_TO_APPROVED }, 
			service.getAvailableMoves(BANK_CONTROL_USER_ID, TB1_CREATED_FORMDATA_ID).toArray()
		);
		assertArrayEquals(
			new Object[] { WorkflowMove.APPROVED_TO_CREATED, WorkflowMove.APPROVED_TO_ACCEPTED }, 
			service.getAvailableMoves(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID).toArray()
		);
		assertArrayEquals(
			new Object[] { WorkflowMove.ACCEPTED_TO_APPROVED }, 
			service.getAvailableMoves(BANK_CONTROL_USER_ID, TB1_ACCEPTED_FORMDATA_ID).toArray()
		);
		
		// Контролёр Банка может выполнять все переходы на уровне банка, но там ЖЦ состоит только из двух стадий 
		assertArrayEquals(
			new Object[] { WorkflowMove.CREATED_TO_ACCEPTED }, 
			service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_CREATED_FORMDATA_ID).toArray()
		);
		assertArrayEquals(
			new Object[] { WorkflowMove.ACCEPTED_TO_CREATED }, 
			service.getAvailableMoves(BANK_CONTROL_USER_ID, BANK_ACCEPTED_FORMDATA_ID).toArray()
		);

		// Никто не может выполнять переходы, если отчетный период неактивен
		assertEquals(0, service.getAvailableMoves(BANK_CONTROL_USER_ID, INACTIVE_FORMDATA_ID).size());
		assertEquals(0, service.getAvailableMoves(TB1_CONTROL_USER_ID, INACTIVE_FORMDATA_ID).size());
	}
	
	@Test
	public void testGetAccessParams() {
		// Проверяем только один случай, так как этот метод просто аггрегирует результаты других методов,
		// а мы их уже оттестировали отдельно
		FormDataAccessParams params = service.getFormDataAccessParams(BANK_CONTROL_USER_ID, TB1_APPROVED_FORMDATA_ID);
		assertTrue(params.isCanRead());
		assertTrue(params.isCanEdit());
		assertFalse(params.isCanDelete());
		assertArrayEquals(
			new Object[] { WorkflowMove.APPROVED_TO_CREATED, WorkflowMove.APPROVED_TO_ACCEPTED }, 
			params.getAvailableWorkflowMoves().toArray()
		);

	}
}
