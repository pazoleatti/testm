package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
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

/**
 * Тест для справочника "Типы налоговых форм"
 *
 * @autor auldanov on 13.08.2014.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookFormDataKindTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookFormDataKindTest {
    @Autowired
    RefBookDao refBookDao;

    private static final String ATTRIBUTE_NAME = "NAME";
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

    @Test
    public void test3() {
        PagingParams pagingParams = new PagingParams();
        pagingParams.setStartIndex(4);
        pagingParams.setCount(2);
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecords(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, pagingParams, null, null, null);
        assertEquals(2, records.size());
        assertEquals(FormDataKind.UNP.getId(), records.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
        assertEquals(FormDataKind.UNP.getTitle(), records.get(0).get(ATTRIBUTE_NAME).getStringValue());
        assertEquals(FormDataKind.ADDITIONAL.getId(), records.get(1).get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
        assertEquals(FormDataKind.ADDITIONAL.getTitle(), records.get(1).get(ATTRIBUTE_NAME).getStringValue());
    }

    @Test
    public void test4() {
        PagingParams pagingParams = new PagingParams();
        pagingParams.setStartIndex(5);
        pagingParams.setCount(3);
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecords(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, pagingParams, null, null, null);
        assertEquals(1, records.size());
        assertEquals(FormDataKind.ADDITIONAL.getId(), records.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
        assertEquals(FormDataKind.ADDITIONAL.getTitle(), records.get(0).get(ATTRIBUTE_NAME).getStringValue());
    }

    @Test
    public void test5() {
        PagingParams pagingParams = new PagingParams();
        pagingParams.setStartIndex(-1);
        pagingParams.setCount(3);
    }

    @Test
    public void test6() {
        assertEquals(5, refBookDao.getRecordsCount(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, null));
    }

    @Test
    public void test7() {
        assertEquals(2, refBookDao.getRecordsCount(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, "id = 2 or id = 4"));
    }

    @Test
    public void test8() {
        assertEquals(1, refBookDao.getRecordsCount(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, "LOWER(NAME) like '%ичн%'"));
    }

    @Test
    public void test9() {
        assertEquals(4, refBookDao.getRecordsCount(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, "LOWER(NAME) like '%ная%'"));
    }

    @Test
    public void test10() {
        assertEquals(0, refBookDao.getRecordsCount(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, "LOWER(NAME) like '%qw1%'"));
    }

    @Test
    public void test11() {
        assertEquals(1, refBookDao.getRecordsCount(FORM_DATA_KIND_REF_BOOK_ID, FORM_DATA_KIND_TABLE_NAME, "LOWER(NAME) like '%форма%'"));
    }
}
