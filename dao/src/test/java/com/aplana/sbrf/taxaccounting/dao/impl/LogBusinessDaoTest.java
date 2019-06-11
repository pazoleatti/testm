package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LogBusinessDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogBusinessDaoTest {

    @Autowired
    private LogBusinessDao logBusinessDao;

    @Before
    public void before() {
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(logBusinessDao, "departmentDao", departmentDao);
        when(departmentDao.getParentsHierarchyShortNames(anyInt())).thenReturn("Б - департамент");
    }

    @Test
    public void testFindAllByDeclarationId() {
        LogBusinessDTO logBusiness = logBusinessDao.findAllByDeclarationId(1, new PagingParams("log_date", "asc")).get(0);
        assertEquals(Long.valueOf(1), logBusiness.getId());
        assertEquals("Создать", logBusiness.getEventName());
        assertEquals("Контролёр Банка (controlBank)", logBusiness.getUserName());
        assertEquals("operator", logBusiness.getRoles());
        assertEquals(Long.valueOf(1), logBusiness.getDeclarationDataId());
        assertEquals("А - департамент", logBusiness.getUserDepartmentName());
        assertEquals("the best note", logBusiness.getNote());
    }

    @Test
    public void testFindAllByPersonId() {
        LogBusinessDTO logBusiness = logBusinessDao.findAllByPersonId(1, new PagingParams("log_date", "desc")).get(0);
        assertEquals(Long.valueOf(5), logBusiness.getId());
        assertEquals("Удалить", logBusiness.getEventName());
        assertEquals("Контролёр Банка (controlBank)", logBusiness.getUserName());
        assertEquals("operator", logBusiness.getRoles());
        assertEquals(Long.valueOf(1), logBusiness.getPersonId());
        assertEquals("B - департамент", logBusiness.getUserDepartmentName());
        assertEquals("the best note", logBusiness.getNote());
    }

    @Test
    public void testCreate() {
        LogBusiness logBusiness = new LogBusiness();
        logBusiness.setId(1000L);
        logBusiness.setLogDate(new Date());
        logBusiness.setDeclarationDataId(1L);
        logBusiness.setEvent(FormDataEvent.CALCULATE);
        TAUser user = new TAUser();
        user.setName("Контролёр Банка");
        user.setDepartmentId(1);
        TARole role = new TARole();
        role.setName("operator");
        user.setRoles(asList(role));
        logBusiness.setUser(user);
        logBusiness.setNote("the best note");
        logBusinessDao.create(logBusiness);

        LogBusinessDTO logBusinessDTO = logBusinessDao.findAllByDeclarationId(1, new PagingParams("log_date", "desc")).get(0);
        assertNotNull(logBusinessDTO.getId());
        assertEquals("Рассчитать", logBusinessDTO.getEventName());
        assertEquals("Контролёр Банка", logBusinessDTO.getUserName());
        assertEquals("operator", logBusinessDTO.getRoles());
        assertEquals(Long.valueOf(1), logBusinessDTO.getDeclarationDataId());
        assertEquals("Б - департамент", logBusinessDTO.getUserDepartmentName());
        assertEquals("the best note", logBusinessDTO.getNote());
    }

    @Test
    public void testGetMaxLogDateByDeclarationIdAndEvent() {
        assertEquals(new LocalDate(2013, 3, 1).toDate(), logBusinessDao.getMaxLogDateByDeclarationIdAndEvent(1L, FormDataEvent.CALCULATE));
    }
}
