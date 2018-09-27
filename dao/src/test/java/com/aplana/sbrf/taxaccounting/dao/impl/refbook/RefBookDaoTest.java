package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


//TODO: Необходимо добавить тесты для getRecords с фильтром (Marat Fayzullin 2013-08-31)

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDaoTest {

    @Autowired
    RefBookDao refBookDao;

    @Test
    public void testGet1() {
        RefBook refBook1 = refBookDao.get(1L);
        assertEquals(1, refBook1.getId().longValue());
        assertEquals(7, refBook1.getAttributes().size());
    }

    @Test
    public void testGet2() {
        RefBook refBook2 = refBookDao.get(2L);
        assertEquals(2, refBook2.getId().longValue());
        assertEquals(1, refBook2.getAttributes().size());
    }

    @Test
    public void testGet3() {
        RefBook refBook3 = refBookDao.get(3L);
        assertEquals("24af57ef-ec1c-455f-a4fa-f0fb29483066", refBook3.getScriptId());
    }

    @Test
    public void testFetchAll() {
        List<RefBook> refBooks = refBookDao.fetchAll();
        assertThat(refBooks).hasSize(6);
    }

    @Test
    public void testFetchAllVisible() {
        List<RefBook> refBooks = refBookDao.fetchAllVisible();
        assertThat(refBooks).hasSize(5);
    }

    @Test
    public void testFetchAllInvisible() {
        List<RefBook> refBooks = refBookDao.fetchAllInvisible();
        assertThat(refBooks).hasSize(1);
    }

    @Test
    public void testSearchVisibleByName() {
        List<RefBook> searchSpaceSymbol = refBookDao.searchVisibleByName(" ");
        assertThat(searchSpaceSymbol).hasSize(5);

        List<RefBook> searchVisible = refBookDao.searchVisibleByName("книга");
        assertThat(searchVisible).hasSize(1);

        List<RefBook> searchInvisible = refBookDao.searchVisibleByName("Библиотека");
        assertThat(searchInvisible).isEmpty();
    }

    @Test
    public void testGetByAttribute1() {
        assertEquals(2, refBookDao.getByAttribute(4L).getId().longValue());
    }

    @Test
    public void testGetByAttribute2() {
        assertEquals(1, refBookDao.getByAttribute(3L).getId().longValue());
    }

    @Test(expected = DaoException.class)
    public void testGetByAttribute3() {
        refBookDao.getByAttribute(-123123L);
    }

    @Test
    public void testSetScriptId() {

        RefBook refBook = refBookDao.get(3L);
        assertTrue(refBook.getScriptId().equals("24af57ef-ec1c-455f-a4fa-f0fb29483066"));

        refBookDao.updateScriptId(3L, null);
        RefBook refBook1 = refBookDao.get(3L);
        assertTrue(refBook1.getScriptId() == null);

        refBookDao.updateScriptId(3L, "24af57ef-ec1c-455f-a4fa-f0fb29483066");
        RefBook refBook2 = refBookDao.get(3L);
        assertTrue(refBook2.getScriptId().equals("24af57ef-ec1c-455f-a4fa-f0fb29483066"));
    }

    @Test(expected = Exception.class)
    public void testGetNull() {
        refBookDao.get(null);
    }

    @Test(expected = Exception.class)
    public void testGetByAttribute4() {
        refBookDao.getByAttribute(null);
    }

    @Test
    public void testDereferenceOKTMO() {
        Map<Long, RefBookValue> result;
        List<Long> recordIds = new ArrayList<Long>();
        result = refBookDao.dereferenceValues("REF_BOOK_OKTMO", 4L, recordIds);
        assertTrue(result.isEmpty());
        recordIds.add(2L);
        recordIds.add(3L);
        recordIds.add(5L);
        recordIds.add(6L);
        recordIds.add(7L);
        result = refBookDao.dereferenceValues("REF_BOOK_OKTMO", 4L, recordIds);
        assertEquals(5, result.size());
        assertEquals("Ароматненское", result.get(2L).getStringValue());
        assertEquals("Верхореченское", result.get(3L).getStringValue());
        assertEquals("Голубинское", result.get(5L).getStringValue());
    }
}
