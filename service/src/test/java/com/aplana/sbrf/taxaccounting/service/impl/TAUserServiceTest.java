package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TAUserServiceTest.xml")
public class TAUserServiceTest {

	@Autowired
	private TAUserService taUserService;

	@Autowired
	DepartmentService departmentService;
	@Autowired
	TAUserDao userDao;
	@Autowired
	TARoleDao roleDao;

	private final static int USER_OPERATOR_ID = 14;

	private final static int USER_DEPARTMENT_ID = 1;

	private final static String USER_ROLE = "ROLE_CONTROL_UNP";

	private final static String USER_LOGIN_CONTROL = "controlBank";
	private final static String USER_LOGIN_OPER = "operTB";
	private final static String USER_LOGIN_OPER_VIEW = "operTB_View";

	private static TAUser user;

	@Before
	public void init() {
		List<Integer> listUserIds = new ArrayList<Integer>();
		listUserIds.add(1);
		listUserIds.add(2);
		listUserIds.add(3);

		TARole role = new TARole();
		role.setAlias(USER_ROLE);
		role.setTaxType(TaxType.NDFL);
		List<TARole> listUserRoles = new ArrayList<TARole>();
		listUserRoles.add(role);

		user = new TAUser();
		user.setId(USER_OPERATOR_ID);
		user.setLogin(USER_LOGIN_CONTROL);
		user.setEmail("controlBank@bank.ru");
		user.setDepartmentId(USER_DEPARTMENT_ID);
		user.setRoles(listUserRoles);
		user.setActive(true);

		when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(user);
		when(userDao.getUser(1)).thenReturn(user);
		when(userDao.getUser(2)).thenReturn(user);
		when(userDao.getUser(3)).thenReturn(user);
		when(userDao.createUser(user)).thenReturn(user.getId());
		when(userDao.getUserIds()).thenReturn(listUserIds);
		when(userDao.checkUserRole(USER_ROLE)).thenReturn(1);
		when(taUserService.existsUser(USER_LOGIN_CONTROL)).thenReturn(false);
		when(taUserService.existsUser(USER_LOGIN_OPER)).thenReturn(true);

		when(departmentService.getDepartment(USER_DEPARTMENT_ID)).thenReturn(new Department());
		when(departmentService.existDepartment(USER_DEPARTMENT_ID)).thenReturn(true);
	}

	@Test
	public void testServiceGetUser() {
		assertEquals(user.getLogin(), taUserService.getUser(USER_OPERATOR_ID).getLogin());
	}

	@Test
	public void testServiceSetUserIsActive() {
		taUserService.setUserIsActive(user.getLogin(), false);
	}

	@Test(expected = WSException.class)
	public void testServiceUpdateUserInfo() {
		taUserService.updateUser(user);
	}

	@Test
	public void testServiceCreateUser() {
		assertEquals(user.getId(), taUserService.createUser(user));
	}

	@Test
	public void testListAllUsers() {
		assertEquals(3, taUserService.listAllUsers().size());
	}

	@Test
	public void testListAllFullUsers() {
		assertEquals(3, taUserService.listAllFullActiveUsers().size());
	}

	@Test(expected = WSException.class)
	public void testCreateUser() {
		TAUser user = new TAUser();
		user.setId(USER_OPERATOR_ID);
		user.setLogin(USER_LOGIN_OPER);
		user.setEmail("controlBank@bank.ru");
		user.setDepartmentId(USER_DEPARTMENT_ID);
		TARole role = new TARole();
		role.setAlias(USER_ROLE);
		role.setTaxType(TaxType.NDFL);
		List<TARole> listUserRoles = new ArrayList<TARole>();
		listUserRoles.add(role);
		user.setRoles(listUserRoles);
		user.setActive(true);
		taUserService.createUser(user);
	}

	@Test
	public void testCreateUserView() {
		final TAUserView user = new TAUserView();
		user.setId(USER_OPERATOR_ID);
		user.setLogin(USER_LOGIN_OPER_VIEW);
		user.setEmail("controlBank@bank.ru");
		user.setDepId(USER_DEPARTMENT_ID);
		user.setActive(true);
		user.setTaRoleIds(Arrays.asList(1L));

		final List<TAUser> taUserList = new ArrayList<TAUser>();
		when(userDao.createUser(any(TAUser.class))).thenAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				TAUser taUser = (TAUser)invocation.getArguments()[0];
				taUserList.add(taUser);
				return user.getId();
			}
		});
		when(roleDao.getRole(1)).thenReturn(new TARole(){{
			setId(1);
		}});
		taUserService.createUser(user);
		Assert.assertEquals(user.getLogin(), taUserList.get(0).getLogin());
		Assert.assertEquals(user.getEmail(), taUserList.get(0).getEmail());
		Assert.assertEquals(user.getDepId().intValue(), taUserList.get(0).getDepartmentId());
		Assert.assertEquals(user.getActive(), taUserList.get(0).isActive());
		Assert.assertEquals(user.getTaRoleIds().size(), taUserList.get(0).getRoles().size());
		Assert.assertEquals(user.getTaRoleIds().get(0).intValue(), taUserList.get(0).getRoles().get(0).getId());
		Assert.assertEquals(user.getAsnuIds(), taUserList.get(0).getAsnuIds());
	}
}
