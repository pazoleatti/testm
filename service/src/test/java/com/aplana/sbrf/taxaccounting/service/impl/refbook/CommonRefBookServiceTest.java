package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("CommonRefBookServiceTest.xml")
public class CommonRefBookServiceTest {

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private RefBookSimpleDao refBookSimpleDao;

    @Autowired
    private CommonRefBookService commonRefBookService;

    @Autowired
    private AsyncManager asyncManager;

    private static Method createSearchFilterMethod;

    @BeforeClass
    public static void setUp() throws NoSuchMethodException {
        Class<? extends CommonRefBookService> clazz = CommonRefBookServiceImpl.class;
        createSearchFilterMethod = clazz.getDeclaredMethod("createSearchFilter", Long.class, Map.class, String.class, Boolean.class);
        createSearchFilterMethod.setAccessible(true);
    }

    @Test
    public void test_findAllVisible() {
        commonRefBookService.findAllVisible();
        verify(refBookDao).findAllVisible();
    }

    @Test
    public void test_findAllVisibleByName() {
        PagingParams pagingParams = any(PagingParams.class);
        commonRefBookService.findAllShortInfo(anyString(), pagingParams);
        verify(refBookDao).findAllVisibleShortInfo(anyString(), same(pagingParams));
    }

    @Test
    public void testFetchAllRecords() {
        commonRefBookService.fetchAllRecords(0L, anyListOf(String.class), "", "", null, null);
        verify(refBookSimpleDao, times(1)).getRecords(any(RefBook.class),
                any(RefBookAttribute.class),
                anyString(),
                any(PagingParams.class),
                anyListOf(String.class),
                anyString(),
                anyString(),
                any(Date.class));
    }

    @Test
    public void createSearchFilterTest() throws InvocationTargetException, IllegalAccessException {

        CommonRefBookService commonRefBookService = spy(new CommonRefBookServiceImpl());
        Long refBookId = 0L;
        String searchPattern = "any text";
        Map<String, String> extraParams = new HashMap<>();

        doReturn("").when(commonRefBookService).getSearchQueryStatement(searchPattern, refBookId, false);
        doReturn("").when(commonRefBookService).getSearchQueryStatementWithAdditionalStringParameters(extraParams, searchPattern, refBookId, false);


        createSearchFilterMethod.invoke(commonRefBookService, refBookId, extraParams, searchPattern, false);
        verify(commonRefBookService, times(1)).getSearchQueryStatement(searchPattern, refBookId, false);

        extraParams.put("A", "A");

        createSearchFilterMethod.invoke(commonRefBookService, refBookId, extraParams, searchPattern, false);
        verify(commonRefBookService, times(1)).getSearchQueryStatementWithAdditionalStringParameters(extraParams, searchPattern, refBookId, false);
    }

    @Test
    public void test_createReport() throws AsyncTaskException {
        AsyncTaskType reportType = AsyncTaskType.EXCEL_REF_BOOK;
        TAUserInfo userInfo = mock(TAUserInfo.class);
        RefBook refBook = mock(RefBook.class);

        when(refBookDao.get(0L)).thenReturn(refBook);

        commonRefBookService.createReport(userInfo, 0L, mock(Date.class), mock(PagingParams.class), "", false, new HashMap<String, String>(), AsyncTaskType.EXCEL_REF_BOOK);
        verify(asyncManager).executeTask(anyString(), eq(reportType), eq(userInfo), any(Map.class), any(Logger.class), anyBoolean(), any(AbstractStartupAsyncTaskHandler.class));
    }
}
