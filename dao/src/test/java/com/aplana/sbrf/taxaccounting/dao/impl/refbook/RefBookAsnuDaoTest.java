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

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookAsnuDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookAsnuDaoTest {
    private final static int ASNU_COUNT = 15;

    @Autowired
    private RefBookAsnuDao refBookAsnuDao;

    //Проверка получения всех значений справочника
    @Test
    public void testFetchAll() {
        List<RefBookAsnu> asnuList = refBookAsnuDao.fetchAll();
        assertTrue(asnuList.size() == ASNU_COUNT);
    }
}
