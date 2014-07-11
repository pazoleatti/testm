package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("FormDataServiceTest.xml")
@DirtiesContext
public class FormDataServiceTest {

    @Autowired
    FormDataService formDataService;

    private Map<Long, Map<String, Long>> recordCache;
    private Map<Long, RefBookDataProvider> providerCache;
    private Map<Long, Map<String, RefBookValue>> refBookCache;

    private static final Long REF_BOOK_ID = 1L;
    private static final Long REF_BOOK_RECORD_ID = 1L;
    private static final String REF_BOOK_ALIAS = "alias";
    private static final Date DATE = new Date();
    private static final int ROW_INDEX = 1;
    private static final int COL_INDEX = 1;
    private static final String COLUMN_NAME = "Имя колонки";

    @Before
    public void init() {
            RefBookFactory refBookFactory = mock(RefBookFactory.class);

            RefBookDataProvider refBookDataProvider = mock(RefBookDataProvider.class);

            PagingResult<Map<String, RefBookValue>> records1 = new PagingResult<Map<String, RefBookValue>>();
            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
            RefBookValue refBookValue = new RefBookValue(RefBookAttributeType.NUMBER, REF_BOOK_RECORD_ID);
            map.put(RefBook.RECORD_ID_ALIAS, refBookValue);
            records1.add(map);
            PagingResult<Map<String, RefBookValue>> records2 = new PagingResult<Map<String, RefBookValue>>();
            records2.add(new HashMap<String, RefBookValue>());
            records2.add(new HashMap<String, RefBookValue>());

            when(refBookDataProvider.getRecords(any(Date.class), any(PagingParams.class),
                    eq("LOWER(alias) = LOWER('oneResult')"), any(RefBookAttribute.class)))
                    .thenReturn(records1);
            when(refBookDataProvider.getRecords(any(Date.class), any(PagingParams.class),
                    eq("LOWER(alias) = LOWER('twoResult')"), any(RefBookAttribute.class)))
                    .thenReturn(records2);

            RefBookAttribute refBookAttribute = new RefBookAttribute();
            refBookAttribute.setAlias(REF_BOOK_ALIAS);
            refBookAttribute.setAttributeType(RefBookAttributeType.STRING);


            RefBook refBook = new RefBook();
            refBook.setAttributes(asList(refBookAttribute));
            when(refBookFactory.get(REF_BOOK_ID)).thenReturn(refBook);

            recordCache = new HashMap<Long, Map<String, Long>>();

            providerCache = new HashMap<Long, RefBookDataProvider>();
            providerCache.put(REF_BOOK_ID, refBookDataProvider);

            refBookCache = new HashMap<Long, Map<String, RefBookValue>>();

            ReflectionTestUtils.setField(formDataService, "refBookFactory", refBookFactory);
    }

    @Test
    public void getRefBookRecordTest() {
        Logger logger = new Logger();

        // не найдено
        Map<String, RefBookValue> result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache,
                refBookCache, REF_BOOK_ALIAS, "noResult", DATE, ROW_INDEX, COLUMN_NAME, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);

        // 1 результат
        logger.clear();
        result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache,
                refBookCache, REF_BOOK_ALIAS, "oneResult", DATE, ROW_INDEX, COLUMN_NAME, logger, true);
        assertEquals(result.size(), 1);
        assertEquals(result.get(RefBook.RECORD_ID_ALIAS).getNumberValue(), REF_BOOK_RECORD_ID);
        assertTrue(logger.getEntries().isEmpty());

        // 2 результата (с warn)
        result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache, refBookCache,
                REF_BOOK_ALIAS, "twoResult", DATE, ROW_INDEX, COLUMN_NAME, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);
        assertTrue(logger.containsLevel(LogLevel.WARNING));

        // 2 результата (с Exception)
        // getRefBookRecord1ExcTest()
    }

    @Test(expected = ServiceException.class)
    public void getRefBookRecordExcTest() {
        Logger logger = new Logger();
        Map<String, RefBookValue> result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache,
                refBookCache, REF_BOOK_ALIAS, "twoResult", DATE, ROW_INDEX, COLUMN_NAME, logger, true);
        assertNull(result);
        assertTrue(logger.getEntries().isEmpty());
    }

    @Test
    public void getRefBookRecordIdTest() {
        Logger logger = new Logger();

        // не найдено
        Long result = formDataService.getRefBookRecordId(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "noResult", DATE, ROW_INDEX, COLUMN_NAME, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);

        // 1 результат
        logger.clear();
        result = formDataService.getRefBookRecordId(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "oneResult", DATE, ROW_INDEX, COLUMN_NAME, logger, true);
        assertEquals(result, Long.valueOf(REF_BOOK_RECORD_ID));
        assertTrue(logger.getEntries().isEmpty());

        // 2 результата (с warn)
        result = formDataService.getRefBookRecordId(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "twoResult", DATE, ROW_INDEX, COLUMN_NAME, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);
        assertTrue(logger.containsLevel(LogLevel.WARNING));

        // 2 результата (с Exception)
        // getRefBookRecord1ExcTest()
    }

    @Test(expected = ServiceException.class)
    public void getRefBookRecordIdExcTest() {
        Logger logger = new Logger();
        Long result = formDataService.getRefBookRecordId(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "twoResult", DATE, ROW_INDEX, COLUMN_NAME, logger, true);
        assertNull(result);
        assertTrue(logger.getEntries().isEmpty());
    }

    @Test
    public void getRefBookRecordImportTest() {
        Logger logger = new Logger();

        // не найдено
        Map<String, RefBookValue> result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache,
                refBookCache, REF_BOOK_ALIAS, "noResult", DATE, ROW_INDEX, COLUMN_NAME, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);

        // 1 результат
        logger.clear();
        result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache,
                refBookCache, REF_BOOK_ALIAS, "oneResult", DATE, ROW_INDEX, COLUMN_NAME, logger, true);
        assertEquals(result.size(), 1);
        assertEquals(result.get(RefBook.RECORD_ID_ALIAS).getNumberValue(), REF_BOOK_RECORD_ID);
        assertTrue(logger.getEntries().isEmpty());

        // 2 результата (с warn)
        result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache, refBookCache,
                REF_BOOK_ALIAS, "twoResult", DATE, ROW_INDEX, COLUMN_NAME, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);
        assertTrue(logger.containsLevel(LogLevel.WARNING));

        // 2 результата (с Exception)
        // getRefBookRecord1ExcTest()
    }

    @Test(expected = ServiceException.class)
    public void getRefBookRecordImportExcTest() {
        Logger logger = new Logger();
        Map<String, RefBookValue> result = formDataService.getRefBookRecord(REF_BOOK_ID, recordCache, providerCache,
                refBookCache, REF_BOOK_ALIAS, "twoResult", DATE, ROW_INDEX, COLUMN_NAME, logger, true);
        assertNull(result);
        assertTrue(logger.getEntries().isEmpty());
    }

    @Test
    public void getRefBookRecordIdImportTest() {
        Logger logger = new Logger();

        // не найдено
        Long result = formDataService.getRefBookRecordIdImport(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "noResult", DATE, ROW_INDEX, COL_INDEX, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);

        // 1 результат
        logger.clear();
        result = formDataService.getRefBookRecordIdImport(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "oneResult", DATE, ROW_INDEX, COL_INDEX, logger, true);
        assertEquals(result, Long.valueOf(REF_BOOK_RECORD_ID));
        assertTrue(logger.getEntries().isEmpty());

        // 2 результата (с warn)
        result = formDataService.getRefBookRecordIdImport(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "twoResult", DATE, ROW_INDEX, COL_INDEX, logger, false);
        assertNull(result);
        assertEquals(logger.getEntries().size(), 1);
        assertTrue(logger.containsLevel(LogLevel.WARNING));

        // 2 результата (с Exception)
        // getRefBookRecord1ExcTest()
    }

    @Test(expected = ServiceException.class)
    public void getRefBookRecordIdImportExcTest() {
        Logger logger = new Logger();
        Long result = formDataService.getRefBookRecordIdImport(REF_BOOK_ID, recordCache, providerCache, REF_BOOK_ALIAS,
                "twoResult", DATE, ROW_INDEX, COL_INDEX, logger, true);
        assertNull(result);
        assertTrue(logger.getEntries().isEmpty());
    }

    public void checkReferenceValueTest() {
        Logger logger = new Logger();
        formDataService.checkReferenceValue(REF_BOOK_ID, "test", "test", ROW_INDEX, COL_INDEX, logger, true);
        assertTrue(logger.getEntries().isEmpty());
        formDataService.checkReferenceValue(REF_BOOK_ID, "test", "test", ROW_INDEX, COL_INDEX, logger, false);
        assertTrue(logger.getEntries().isEmpty());
        formDataService.checkReferenceValue(REF_BOOK_ID, "test_1", "test_2", ROW_INDEX, COL_INDEX, logger, false);
        assertFalse(logger.getEntries().isEmpty());
    }

    @Test(expected = ServiceException.class)
    public void checkReferenceValue2Test() {
        Logger logger = new Logger();
        formDataService.checkReferenceValue(REF_BOOK_ID, "test_1", "test_2", ROW_INDEX, COL_INDEX, logger, true);
    }

    @Test
    public void test() {
        FormData formData1 = new FormData();
        formData1.setId(1L);
        FormData formData2 = new FormData();
        formData2.setId(2L);
        formDataService.getDataRowHelper(formData1);
        formDataService.getDataRowHelper(formData2);
        formDataService.getDataRowHelper(formData1);
        formDataService.getDataRowHelper(formData2);
    }

    @Test(expected = ServiceException.class)
    public void testException() {
        formDataService.getDataRowHelper(new FormData());
    }
}
