package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LogBusinessDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogBusinessDaoTest {

    @Autowired
    private LogBusinessDao logBusinessDao;

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
        assertEquals(Long.valueOf(4), logBusiness.getId());
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
        logBusiness.setId(5L);
        logBusiness.setLogDate(new Date());
        logBusiness.setDeclarationDataId(1L);
        logBusiness.setEventId(3);
        logBusiness.setUserLogin("Контролёр Банка");
        logBusiness.setRoles("operator");
        logBusiness.setUserDepartmentName("Б - департамент");
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
}
