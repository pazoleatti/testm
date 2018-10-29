package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookSimpleDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;

public class RefBookSimpleDataProviderTest {

    private static final long TEST_RECORD_ID = 122934L;
    private static final long RFB_ID = RefBook.Id.ASNU.getId();
    private static final String RFB_TABLE_NAME = "REF_BOOK_ASNU";
    private static final Long RFB_NOT_VERSIONED_ID = 100L;

    private RefBookSimpleDataProvider provider;
    private RefBookSimpleDaoImpl daoMock;
    private RefBookDao refBookDaoMock;
    private Logger logger;

    @Before
    public void setUp() {
        provider = new RefBookSimpleDataProvider();

        daoMock = mock(RefBookSimpleDaoImpl.class);
        refBookDaoMock = mock(RefBookDao.class);
        RefBookSimpleReadOnly simpleReadOnlyMock = mock(RefBookSimpleReadOnly.class);
        CommonRefBookService commonRefBookServiceMock = mock(CommonRefBookService.class);
        logger = mock(Logger.class);
        RefBookSimpleDataProviderHelper helper = mock(RefBookSimpleDataProviderHelper.class);
        when(refBookDaoMock.get(anyLong())).thenReturn(getRefBookStub());
        when(refBookDaoMock.get(RFB_NOT_VERSIONED_ID)).thenReturn(getRefBookNotVersionedStub());

        ReflectionTestUtils.setField(provider, "dao", daoMock);
        ReflectionTestUtils.setField(provider, "refBookDao", refBookDaoMock);
        ReflectionTestUtils.setField(provider, "readOnlyProvider", simpleReadOnlyMock);
        ReflectionTestUtils.setField(provider, "commonRefBookService", commonRefBookServiceMock);
        ReflectionTestUtils.setField(provider, "helper", helper);
        provider.setRefBookId(RefBook.Id.ASNU);
    }

    private RefBook getRefBookStub() {
        RefBook refBook = new RefBook();
        refBook.setId(RFB_ID);
        refBook.setTableName(RFB_TABLE_NAME);
        refBook.setVersioned(true);
        return refBook;
    }

    private RefBook getRefBookNotVersionedStub() {
        RefBook refBook = new RefBook();
        refBook.setId(RFB_ID);
        refBook.setTableName(RFB_TABLE_NAME);
        return refBook;
    }

    @Test
    public void getRecordsInvokesDao() {
        Date version = new GregorianCalendar(1983, Calendar.JULY, 22).getTime();
        provider.getRecords(version, null, null, null);

        verify(daoMock, atLeastOnce()).getRecords(
                any(RefBook.class), eq(version), any(PagingParams.class),
                anyString(), any(RefBookAttribute.class), eq(true));
    }

    @Test
    public void getRowNumInvokesDao() {
        provider.getRowNum(null, TEST_RECORD_ID, null, null, true);

        verify(daoMock, atLeastOnce()).getRowNum(any(RefBook.class), any(Date.class), anyLong(), anyString(),
                any(RefBookAttribute.class), anyBoolean());
    }

    @Test
    public void getRecordDataInvokesDao() {
        provider.getRecordData(TEST_RECORD_ID);

        verify(daoMock).getRecordData(any(RefBook.class), eq(TEST_RECORD_ID));
    }

    @Test
    public void getUniqueRecordIdsInvokesDao() {
        Date version = new GregorianCalendar(1983, Calendar.JULY, 22).getTime();
        String filter = "FILTER";
        provider.getUniqueRecordIds(version, filter);

        verify(daoMock).getUniqueRecordIds(any(RefBook.class), eq(version), eq(filter));
    }

    @Test
    public void getInactiveRecordsInPeriodInvokesDao() {
        List<Long> recordIds = new ArrayList<>();
        Date startDate = new Date(0);
        Date endDate = new Date();
        provider.getInactiveRecordsInPeriod(recordIds, startDate, endDate);

        verify(refBookDaoMock).getInactiveRecords(RFB_TABLE_NAME, recordIds);

    }

    @Test
    public void getRecordVersionInfoInvokesDao() {
        provider.getRecordVersionInfo(3L);

        verify(daoMock).getRecordVersionInfo(any(RefBook.class), eq(3L));
    }

    @Test
    public void getVersionsInvokesDao() {
        Date versionStart = new Date(0);
        Date versionEnd = new Date();
        provider.getVersions(versionStart, versionEnd);

        verify(daoMock).getVersions(any(RefBook.class), eq(versionStart), eq(versionEnd));
    }

    @Test
    public void getRecordVersionsCountInvokesDao() {
        provider.getRecordVersionsCount(4L);

        verify(daoMock).getRecordVersionsCount(any(RefBook.class), eq(4L));
    }

    @Test
    public void isRefBookSupportedReturnsTrueIfEditableAndVersioned() {
        RefBook refBook = new RefBook();
        refBook.setReadOnly(false);
        refBook.setVersioned(true);

        assertTrue(provider.isRefBookSupported(refBook));
    }

    @Test
    public void isRefBookSupportedReturnsTrue() {
        assertTrue(provider.isRefBookSupported(new RefBook()));
    }

    @Test
    public void isRefBookSupportedByIdReturnsTrue() {
        assertTrue(provider.isRefBookSupported(99L));
    }

    @Test
    public void getRecordIdInvokesDao() {
        provider.getRecordId(5L);
        verify(daoMock).getRecordId(any(RefBook.class), eq(5L));
    }

    @Test
    public void getRecordVersionsByRecordIdInvokesDao() {
        provider.getRecordVersionsByRecordId(4L, null, null, null);
        verify(daoMock).getRecordVersionsByRecordId(any(RefBook.class), eq(4L), any(PagingParams.class), anyString(), any(RefBookAttribute.class));
    }

    @Test
    public void dereferenceValuesExecutes() {
        RefBook refBook = new RefBook();
        List<RefBookAttribute> attributes = new ArrayList<>();
        RefBookAttribute refBookAttribute = new RefBookAttribute();
        refBookAttribute.setId(50L);
        refBookAttribute.setAlias("alias");
        attributes.add(refBookAttribute);
        refBook.setAttributes(attributes);
        provider.setRefBook(refBook);
        provider.dereferenceValues(50L, new ArrayList<>(Collections.singletonList(4L)));
    }

    @Test
    public void getRecordData2InvokesDao() {
        provider.getRecordData(Arrays.asList(1L, 2L));
        verify(daoMock).getRecordData(any(RefBook.class), anyListOf(Long.class));
    }

    @Test
    public void getRecordDataWhereClause() {
        provider.getRecordDataWhere("id in (1, 2)");
        verify(daoMock).getRecordDataWhere(any(RefBook.class), eq("id in (1, 2)"));
    }

    @Test
    public void getFirstRecordIdInvokesDao() {
        provider.getFirstRecordId(5L);

        verify(daoMock).getFirstRecordId(any(RefBook.class), eq(5L));
    }

    @Test
    public void test_updateRecordsWithoutLock_notVersionedRefBook() {
        RefBook refBook = getRefBookNotVersionedStub();
        Map<String, RefBookValue> record = new HashMap<>();
        provider.setRefBook(refBook);

        RefBookRecordVersion refBookRecordVersion = mock(RefBookRecordVersion.class);
        when(daoMock.getRecordVersionInfo(refBook, 1L)).thenReturn(refBookRecordVersion);
        provider.updateRecordVersionWithoutLock(logger, 1L, new Date(0), null, record);
        verify(refBookRecordVersion, never()).getVersionStart();
        verify(daoMock).updateRecordVersion(refBook, 1L, record);
    }

    @Test
    public void test_updateRecordsWithoutLock_versionedRefBookWithNullStartDateAndNullEndDate() {
        RefBook refBook = getRefBookStub();
        Map<String, RefBookValue> record = new HashMap<>();
        provider.setRefBook(refBook);

        RefBookRecordVersion refBookRecordVersion = mock(RefBookRecordVersion.class);
        when(daoMock.getRecordVersionInfo(refBook, 1L)).thenReturn(refBookRecordVersion);
        provider.updateRecordVersionWithoutLock(logger, 1L, null, null, record);
        verify(refBookRecordVersion, never()).getVersionStart();
        verify(daoMock).updateRecordVersion(refBook, 1L, record);
    }

    @Test
    public void test_updateRecordsWithoutLock_versionedRefBookWithNotNullStartDateAndNullEndDate() {
        RefBook refBook = getRefBookStub();
        Map<String, RefBookValue> record = new HashMap<>();
        provider.setRefBook(refBook);

        RefBookRecordVersion refBookRecordVersion = mock(RefBookRecordVersion.class);
        when(daoMock.getRecordVersionInfo(refBook, 1L)).thenReturn(refBookRecordVersion);
        provider.updateRecordVersionWithoutLock(logger, 1L, new Date(0), null, record);
        verify(refBookRecordVersion, atLeast(1)).getVersionStart();
        verify(daoMock).updateRecordVersion(refBook, 1L, record);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRefBookThrowsExceptionOnNullRefBook() {
        provider.setRefBook(null);
        provider.getRefBook();
    }

    @Test
    public void setRefBook() {
        provider.setRefBook(getRefBookStub());

        assertNotNull(provider.getRefBook());
    }

    @Test
    public void getRefBookId() {
        provider.setRefBook(getRefBookStub());

        assertEquals(Long.class, provider.getRefBookId().getClass());
    }
}