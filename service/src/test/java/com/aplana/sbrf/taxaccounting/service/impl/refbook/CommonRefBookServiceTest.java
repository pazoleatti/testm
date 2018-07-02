package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("CommonRefBookServiceTest.xml")
public class CommonRefBookServiceTest {

    @Autowired
    private RefBookSimpleDao refBookSimpleDao;

    @Autowired
    private CommonRefBookService commonRefBookService;

    private static Method createSearchFilterMethod;

    @BeforeClass
    public static void setUp() throws NoSuchMethodException {
        Class<? extends CommonRefBookService> clazz = CommonRefBookServiceImpl.class;
        createSearchFilterMethod = clazz.getDeclaredMethod("createSearchFilter", Long.class, Map.class, String.class, Boolean.class);
        createSearchFilterMethod.setAccessible(true);
    }

    @Test
    public void testFetchAllRecords() {
        commonRefBookService.fetchAllRecords(0L, anyListOf(String.class), "", "", null);
        Mockito.verify(refBookSimpleDao, Mockito.times(1)).getRecords(any(RefBook.class), any(RefBookAttribute.class), anyString(), any(PagingParams.class), anyListOf(String.class), anyString(), anyString());
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
        verify(commonRefBookService, Mockito.times(1)).getSearchQueryStatement(searchPattern, refBookId, false);

        extraParams.put("A", "A");

        createSearchFilterMethod.invoke(commonRefBookService, refBookId, extraParams, searchPattern, false);
        verify(commonRefBookService, Mockito.times(1)).getSearchQueryStatementWithAdditionalStringParameters(extraParams, searchPattern, refBookId, false);
    }
}
