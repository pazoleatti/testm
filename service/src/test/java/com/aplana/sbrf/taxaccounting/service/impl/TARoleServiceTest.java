package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TARoleServiceTest.xml")
public class TARoleServiceTest {

    @Autowired
    TARoleDao roleDao;

    @Autowired
    TARoleService roleService;

    @Test
    public void testGetRoleByAlias() {
        roleService.getRoleByAlias("ROLE");
        verify(roleDao).getRoleByAlias("ROLE");
    }

    @Test
    public void testGetAllSbrfRoles() {
        roleService.getAllSbrfRoles();
        verify(roleDao).getAllSbrfRoles();
    }

    @Test
    public void testGetAllNdflRoles() {
        roleService.getAllNdflRoles();
        verify(roleDao).getAllNdflRoles();
    }
}
