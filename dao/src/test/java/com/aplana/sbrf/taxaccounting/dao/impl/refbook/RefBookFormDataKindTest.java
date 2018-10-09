package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Тест для справочника "Типы налоговых форм"
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"RefBookFormDataKindTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookFormDataKindTest {

    @Autowired
    RefBookDao refBookDao;

    private static final Long FORM_DATA_KIND_REF_BOOK_ID = 94L;
    private static final String FORM_DATA_KIND_TABLE_NAME = "FORM_KIND";

    @Test
    public void test1() {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecords(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, null, null, null, null);
        assertEquals(5, records.size());
    }

    @Test
    public void test2() {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecords(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, null, "id > 0 and id < 4", null, null);
        assertEquals(3, records.size());
    }
}
