package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookPersonDaoTest {

    public static final long RUS_COUNTRY_ID = 262254399L;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookPersonDaoImpl refBookPersonDao;

    @Autowired
    private RefBookSimpleDao dao;


    @Test
    public void getRecordsTest() {
        //...
    }


}
