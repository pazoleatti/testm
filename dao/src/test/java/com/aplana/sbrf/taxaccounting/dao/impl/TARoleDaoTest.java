package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TARoleDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TARoleDaoTest {

    @Autowired
    TARoleDao roleDao;

    @Test
    public void testGetRole() {
        TARole role = roleDao.getRole(1);
        assertThat(role.getId()).isEqualTo(1);
        assertThat(role.getName()).isEqualTo("Контролёр");
        assertThat(role.getAlias()).isEqualTo("N_ROLE_CONTROL");
    }

    @Test(expected = DaoException.class)
    public void testGetNonexistentRoleThrowsException() {
        roleDao.getRole(4);
    }

    @Test
    public void testGetAllSbrfRoles() {
        List<TARole> roles = roleDao.getAllSbrfRoles();
        assertThat(roles).hasSize(3);
    }

    @Test
    public void testGetAllNdflRoles() {
        List<TARole> roles = roleDao.getAllNdflRoles();
        assertThat(roles).hasSize(3);
    }

    @Test
    public void testGetAllRoleIds() {
        assertThat(roleDao.getAllSbrfRoleIds()).hasSize(3);
    }

    @Test
    public void testGetRoleByAlias() {
        TARole role = roleDao.getRoleByAlias("N_ROLE_OPER");
        assertThat(role.getId()).isEqualTo(2);
        assertThat(role.getAlias()).isEqualTo("N_ROLE_OPER");
        assertThat(role.getName()).isEqualTo("Оператор");
    }

    @Test(expected = DaoException.class)
    public void testGetRoleByWrongAliasThrowsException() {
        roleDao.getRoleByAlias("NOT_A_ROLE");
    }
}
