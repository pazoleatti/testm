package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsSearchDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"BookerStatementsDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BookerStatementsDaoTest {
    @Autowired
    BookerStatementsSearchDao bookerStatementsSearchDao;

    @Test
    public void findPageAll() {
        BookerStatementsFilter filter = new BookerStatementsFilter();

        PagingResult<BookerStatementsSearchResultItem> result = bookerStatementsSearchDao.findPage(filter, BookerStatementsSearchOrdering.DEPARTMENT_NAME, false, new PagingParams(0, 14));

        assertEquals(4, result.getTotalCount());
        assertEquals(4, result.size());
        assertEquals(2, result.get(0).getDepartmentId().intValue());
        assertEquals(2, result.get(1).getDepartmentId().intValue());
        assertEquals(1, result.get(2).getDepartmentId().intValue());
        assertEquals(1, result.get(3).getDepartmentId().intValue());
    }

    @Test
    public void findPageByType() {
        BookerStatementsFilter filter = new BookerStatementsFilter();
        filter.setBookerStatementsType(BookerStatementsType.INCOME101);

        PagingResult<BookerStatementsSearchResultItem> result = bookerStatementsSearchDao.findPage(filter, BookerStatementsSearchOrdering.DEPARTMENT_NAME, true, new PagingParams(0, 14));
        filter.setBookerStatementsType(BookerStatementsType.INCOME102);
        PagingResult<BookerStatementsSearchResultItem> result2 = bookerStatementsSearchDao.findPage(filter, BookerStatementsSearchOrdering.DEPARTMENT_NAME, true, new PagingParams(0, 14));

        assertEquals(2, result.getTotalCount());
        assertEquals(2, result2.getTotalCount());
        assertEquals(0, result.get(0).getBookerStatementsTypeId().intValue());
        assertEquals(0, result.get(1).getBookerStatementsTypeId().intValue());
        assertEquals(1, result2.get(0).getBookerStatementsTypeId().intValue());
        assertEquals(1, result2.get(1).getBookerStatementsTypeId().intValue());
    }

    @Test
    public void findPage() {
        BookerStatementsFilter filter = new BookerStatementsFilter();
        filter.setBookerStatementsType(BookerStatementsType.INCOME101);
        filter.setDepartmentIds(Collections.singletonList(2));

        PagingResult<BookerStatementsSearchResultItem> result = bookerStatementsSearchDao.findPage(filter, BookerStatementsSearchOrdering.DEPARTMENT_NAME, true, new PagingParams(0, 14));

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.get(0).getBookerStatementsTypeId().intValue());
        assertEquals(2, result.get(0).getDepartmentId().intValue());
    }
}
