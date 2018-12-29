package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookAsnuDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookAsnuDaoTest {
    private final static int ASNU_COUNT = 15;
    private final static int ASNU_BY_IDS_COUNT = 7;

    @Autowired
    private RefBookAsnuDao refBookAsnuDao;

    //Проверка получения всех значений справочника
    @Test
    public void testFindAll() {
        List<RefBookAsnu> asnuList = refBookAsnuDao.findAll();
        assertEquals(asnuList.size(), ASNU_COUNT);
    }

    //Проверка получения значений справочника по id
    @Test
    public void testFetchByIds() {
        List<RefBookAsnu> asnuList = refBookAsnuDao.findAllByIdIn(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        assertEquals(asnuList.size(), ASNU_BY_IDS_COUNT);
    }

    public void testFetchById() {
        RefBookAsnu refBookAsnu = refBookAsnuDao.findById(1L);
        assertNotNull(refBookAsnu);
        assertEquals("1000", refBookAsnu.getCode());
    }

    @Test
    public void fetchByName() {
        RefBookAsnu refBookAsnu = refBookAsnuDao.findByName("АИС \"Дивиденд\"");
        assertNotNull(refBookAsnu);
        assertEquals("2000", refBookAsnu.getCode());
    }
}