package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TAUserDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TAUserDaoTest {

    @Autowired
    private TAUserDao userDao;

    @Test
    public void testGetById() {
        TAUser user = userDao.getUser(1);
        assertEquals(1, user.getId());
        assertEquals("controlBank", user.getLogin());
        assertEquals(1, user.getDepartmentId());
        assertEquals("controlBank@bank.ru", user.getEmail());
        assertTrue(user.hasRole("N_ROLE_CONTROL_NS"));
        assertTrue(user.isActive());
    }

    @Test(expected = DaoException.class)
    public void testGetByIncorrectId() {
        userDao.getUser(-1);
    }

    @Test
    public void testGetUserIdByLogin() {
        assertEquals(1, userDao.getUserIdByLogin("controlBank"));
    }

    @Test
    public void testGetUserIds() {
        assertThat(userDao.getAllUserIds()).hasSize(3);
    }

    @Test
    public void testCheckUserRole() {
        assertEquals(1, userDao.checkUserRole("N_ROLE_OPER"));
    }

    @Test
    public void testGetUserIdsByNullFilter() {
        List<Integer> idsNotFiltered = userDao.getUserIdsByFilter(null);
        assertThat(idsNotFiltered).hasSize(3);
    }

    @Test
    public void testGetUserIdsByFilterWithUserName() {
        MembersFilterData filter = new MembersFilterData();

        filter.setUserName("Контролёр Банка");
        List<Integer> idsByName = userDao.getUserIdsByFilter(filter);
        assertThat(idsByName).hasSize(1);

        Integer userId = idsByName.get(0);
        TAUser user = userDao.getUser(userId);
        assertThat(user.getName()).isEqualTo("Контролёр Банка");
    }

    @Test
    public void testGetUserIdsByFilterActive() {
        MembersFilterData filter = new MembersFilterData();

        filter.setActive(false);
        List<Integer> inactiveIds = userDao.getUserIdsByFilter(filter);
        assertThat(inactiveIds).hasSize(1);

        Integer userId = inactiveIds.get(0);
        assertThat(userId).isEqualTo(3);
    }

    @Test
    public void testGetUserIdsByFilterWithDepartments() {
        MembersFilterData filter = new MembersFilterData();
        Set<Integer> departmentIds = new HashSet<>();
        departmentIds.add(2);
        departmentIds.add(3);

        filter.setDepartmentIds(departmentIds);
        List<Integer> idsByDepartments = userDao.getUserIdsByFilter(filter);
        assertThat(idsByDepartments).hasSize(2);

        filter.setActive(false);
        List<Integer> inactiveIdsByDepartments = userDao.getUserIdsByFilter(filter);
        assertThat(inactiveIdsByDepartments).hasSize(1);
    }

    @Test
    public void testGetUserIdsByFilterWithRoles() {
        MembersFilterData filter = new MembersFilterData();

        filter.setRoleIds(Arrays.asList(2L, 3L));
        List<Integer> idsByRoles = userDao.getUserIdsByFilter(filter);
        assertThat(idsByRoles).hasSize(0);

        filter.setRoleIds(Collections.singletonList(1L));
        List<Integer> idsByOneRole = userDao.getUserIdsByFilter(filter);
        assertThat(idsByOneRole).hasSize(3);

        filter.setActive(true);
        List<Integer> activeIdsByRole = userDao.getUserIdsByFilter(filter);
        assertThat(activeIdsByRole).hasSize(2);
    }
}
