package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TARoleServiceTest.xml")
public class TARoleServiceTest {
    @Autowired
    TARoleDao taRoleDao;
    @Autowired
    TARoleService taRoleService;

    @Before
    public void init(){
        List<Integer> ids = Arrays.asList(1, 2, 3);
        when(taRoleDao.getAll()).thenReturn(ids);
        for (Integer i : ids) {
            final int id = i;
            when(taRoleDao.getRole(i)).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    TARole role = new TARole();
                    role.setId(id);
                    role.setName(String.valueOf(id));
                    role.setAlias(String.valueOf(id));
                    role.setTaxType(TaxType.NDFL);
                    return role;
                }
            });
        }
    }

    @Test
    public void getAll() {
        List<TARole> roles = taRoleService.getAll();
        assertEquals(3, roles.size());
        assertEquals(1, roles.get(0).getId());
        assertEquals(2, roles.get(1).getId());
        assertEquals(3, roles.get(2).getId());
    }
}
