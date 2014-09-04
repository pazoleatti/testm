package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookTaxOrganDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookTaxOrganDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookTaxOrganDaoTest {

    @Autowired
    RefBookTaxOrganDao dao;

    @Test
    public void getRecordsTest() {
        // Код
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(RefBookTaxOrganDao.REF_BOOK_CODE_ID);
        assertEquals(2, records.size());
        assertEquals(2, records.getTotalCount());
        assertEquals("1", records.get(0).get("TAX_ORGAN_CODE").getStringValue());
        assertEquals("2", records.get(1).get("TAX_ORGAN_CODE").getStringValue());

        assertEquals(1, dao.getRecords(RefBookTaxOrganDao.REF_BOOK_CODE_ID, "DECLARATION_REGION_ID = 1").size());
        assertEquals(2,dao.getRecords(RefBookTaxOrganDao.REF_BOOK_CODE_ID, null).size());

        // КПП
        records = dao.getRecords(RefBookTaxOrganDao.REF_BOOK_KPP_ID);
        assertEquals(4, records.size());
        assertEquals(4, records.getTotalCount());
        assertEquals("11", records.get(0).get("KPP").getStringValue());

        assertEquals(3, dao.getRecords(RefBookTaxOrganDao.REF_BOOK_KPP_ID, "TAX_ORGAN_CODE = '1'").size());
        assertEquals(3, dao.getRecords(RefBookTaxOrganDao.REF_BOOK_KPP_ID, "TAX_ORGAN_CODE = '2'").size());
        assertTrue(dao.getRecords(RefBookTaxOrganDao.REF_BOOK_KPP_ID, "TAX_ORGAN_CODE = '3'").isEmpty());
        assertEquals(4,dao.getRecords(RefBookTaxOrganDao.REF_BOOK_KPP_ID, null).size());
    }

    @Test
    public void getRecordsCountTest() {
        // Код
        assertEquals(1, dao.getRecordsCount(RefBookTaxOrganDao.REF_BOOK_CODE_ID,  "DECLARATION_REGION_ID = 1"));
        assertEquals(2, dao.getRecordsCount(RefBookTaxOrganDao.REF_BOOK_CODE_ID,  null));

        // КПП
        assertEquals(3, dao.getRecordsCount(RefBookTaxOrganDao.REF_BOOK_KPP_ID,  "TAX_ORGAN_CODE = '2'"));
        assertEquals(4, dao.getRecordsCount(RefBookTaxOrganDao.REF_BOOK_KPP_ID,  null));
    }
}
