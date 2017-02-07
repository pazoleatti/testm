package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookSimpleDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
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
    private RefBookSimpleReadOnly SimpleReadOnlyMock;

    @Before
    public void setUp() throws Exception {
        provider = new RefBookSimpleDataProvider();

        daoMock = mock(RefBookSimpleDaoImpl.class);
        refBookDaoMock = mock(RefBookDao.class);
        SimpleReadOnlyMock = mock(RefBookSimpleReadOnly.class);
        when(refBookDaoMock.get(anyLong())).thenReturn(getRefBookStub());
        when(refBookDaoMock.get(RFB_NOT_VERSIONED_ID)).thenReturn(getRefBookNotVersionedStub());

        ReflectionTestUtils.setField(provider, "dao", daoMock);
        ReflectionTestUtils.setField(provider, "refBookDao", refBookDaoMock);
        ReflectionTestUtils.setField(provider, "readOnlyProvider", SimpleReadOnlyMock);

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
    public void getRecordsInvokesDao() throws Exception {
        Date version = new GregorianCalendar(1983, Calendar.JULY, 22).getTime();
        provider.getRecords(version, null, null, null);

        verify(daoMock, atLeastOnce()).getRecords(
                any(RefBook.class), eq(version), any(PagingParams.class),
                anyString(), any(RefBookAttribute.class), eq(true));
    }

    @Test
    @Ignore
    public void getChildrenRecordsInvokesDao() throws Exception {
        provider.getChildrenRecords(1L, null, null, null, null);

        verify(daoMock, atLeastOnce()).getChildrenRecords(any(RefBook.class),
                any(Date.class), anyLong(), any(PagingParams.class), anyString(), any(RefBookAttribute.class));
    }

    @Test
    public void getRowNumInvokesDao() throws Exception {
        provider.getRowNum(null, TEST_RECORD_ID, null, null, true);

        verify(daoMock, atLeastOnce()).getRowNum(any(RefBook.class), any(Date.class), anyLong(), anyString(),
                any(RefBookAttribute.class), anyBoolean());
    }

    @Test
    public void getRecordDataInvokesDao() throws Exception {
        provider.getRecordData(TEST_RECORD_ID);

        verify(daoMock).getRecordData(any(RefBook.class), eq(TEST_RECORD_ID));
    }

    @Test
    public void getUniqueRecordIdsInvokesDao() throws Exception {
        Date version = new GregorianCalendar(1983, Calendar.JULY, 22).getTime();
        String filter = "FILTER";
        provider.getUniqueRecordIds(version, filter);

        verify(daoMock).getUniqueRecordIds(any(RefBook.class), eq(version), eq(filter));
    }

    @Test
    public void dereferenceValuesInvokesDao() throws Exception {
        List<Long> recordIds = new ArrayList<Long>();
        provider.dereferenceValues(42L, recordIds);

        verify(refBookDaoMock).dereferenceValues(RFB_TABLE_NAME, 42L, recordIds);
    }

    @Test
    public void getInactiveRecordsInPeriodInvokesDao() throws Exception {
        List<Long> recordIds = new ArrayList<Long>();
        Date startDate = new Date(0);
        Date endDate = new Date();
        provider.getInactiveRecordsInPeriod(recordIds, startDate, endDate);

        verify(refBookDaoMock).getInactiveRecords(RFB_TABLE_NAME, recordIds);

    }

    @Test
    public void getRecordVersionInfoInvokesDao() throws Exception {
        provider.getRecordVersionInfo(3L);

        verify(daoMock).getRecordVersionInfo(any(RefBook.class), eq(3L));
    }

    @Test
    public void getVersionsInvokesDao() throws Exception {
        Date versionStart = new Date(0);
        Date versionEnd = new Date();
        provider.getVersions(versionStart, versionEnd);

        verify(daoMock).getVersions(any(RefBook.class), eq(versionStart), eq(versionEnd));
    }

    @Test
    public void getRecordVersionsCountInvokesDao() throws Exception {
        provider.getRecordVersionsCount(4L);

        verify(daoMock).getRecordVersionsCount(any(RefBook.class), eq(4L));
    }

    @Test
    public void isRefBookSupportedReturnsTrueIfEditableAndVersioned() throws Exception {
        RefBook refBook = new RefBook();
        refBook.setReadOnly(false);
        refBook.setVersioned(true);

        assertEquals(true, provider.isRefBookSupported(refBook));
    }

    @Test
    public void isRefBookSupportedReturnsTrue() throws Exception {
        assertEquals(true, provider.isRefBookSupported(new RefBook()));
    }

    @Test
    public void isRefBookSupportedByIdReturnsTrue() throws Exception {
        assertEquals(true, provider.isRefBookSupported(99L));
    }

    @Test
    public void getRecordIdInvokesDao() throws Exception {
        provider.getRecordId(5L);
        verify(daoMock).getRecordId(any(RefBook.class), eq(5L));
    }

    @Test
    public void getRecordVersionsByRecordIdInvokesDao() throws Exception {
        provider.getRecordVersionsByRecordId(4L, null, null, null);
        verify(daoMock).getRecordVersionsByRecordId(any(RefBook.class), eq(4L), any(PagingParams.class), anyString(), any(RefBookAttribute.class));
    }

    @Test
    public void dereferenceValuesExecutes() throws Exception {
        provider.dereferenceValues(50L, new ArrayList<Long>(Arrays.asList(4L)));
    }

    @Test
    public void getRecordVersionsByIdInvokesDao() throws Exception {
        provider.getRecordVersionsById(5L, null, null, null);
        verify(daoMock).getRecordVersions(any(RefBook.class), eq(5L), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class), eq(true));
    }

    @Test
    public void getRecordData2InvokesDao() throws Exception {
        provider.getRecordData(Arrays.asList(1L,2L));
        verify(daoMock).getRecordData(any(RefBook.class), anyListOf(Long.class));
    }

    @Test
    public void getRecords() throws Exception {

    }

    @Test
    public void getRecords1() throws Exception {

    }

    @Test
    public void getChildrenRecords() throws Exception {

    }

    @Test
    public void getRecordIdPairs() throws Exception {

    }

    @Test
    public void getNextVersion() throws Exception {

    }

    @Test
    public void getEndVersion() throws Exception {

    }

    @Test
    public void getUniqueRecordIds() throws Exception {

    }

    @Test
    public void getRecordsCount() throws Exception {

    }

    @Test
    public void checkRecordExistence() throws Exception {

    }

    @Test
    public void isRecordsExist() throws Exception {

    }

    @Test
    public void getRowNum() throws Exception {

    }

    @Test
    public void getParentsHierarchy() throws Exception {

    }

    @Test
    public void getRecordData() throws Exception {

    }

    @Test
    public void getRecordData1() throws Exception {

    }

    @Test
    public void getValue() throws Exception {

    }

    @Test
    public void getVersions() throws Exception {

    }

    @Test
    public void getRecordVersionsById() throws Exception {

    }

    @Test
    public void getRecordVersionsByRecordId() throws Exception {

    }

    @Test
    public void getRecordVersionInfo() throws Exception {

    }

    @Test
    public void getRecordsVersionStart() throws Exception {

    }

    @Test
    public void getRecordVersionsCount() throws Exception {

    }

    @Test
    public void createRecordVersion() throws Exception {

    }

    @Test
    public void createRecordVersionWithoutLock() throws Exception {

    }

    @Test
    public void getUniqueAttributeValues() throws Exception {

    }

    @Test
    public void updateRecordVersion() throws Exception {

    }

    @Test
    public void updateRecordVersionWithoutLock() throws Exception {

    }

    @Test
    public void updateRecordsVersionEnd() throws Exception {

    }

    @Test
    public void updateRecordsVersionEndWithoutLock() throws Exception {

    }

    @Test
    public void deleteAllRecords() throws Exception {

    }

    @Test
    public void deleteAllRecordsWithoutLock() throws Exception {

    }

    @Test
    public void deleteRecordVersions() throws Exception {

    }

    @Test
    public void deleteRecordVersionsWithoutLock() throws Exception {

    }

    @Test
    public void deleteRecordVersions1() throws Exception {

    }

    @Test
    public void getFirstRecordIdInvokesDao() throws Exception {
        provider.getFirstRecordId(5L);

        verify(daoMock).getFirstRecordId(any(RefBook.class), eq(5L));
    }

    @Test
    public void getRecordId() throws Exception {

    }

    @Test
    public void getAttributesValues() throws Exception {

    }

    @Test
    public void getInactiveRecordsInPeriod() throws Exception {

    }

    @Test
    public void insertRecords() throws Exception {

    }

    @Test
    public void insertRecordsWithoutLock() throws Exception {

    }

    @Test
    public void updateRecords() throws Exception {

    }

    @Test
    public void updateRecordsWithoutLock() throws Exception {

    }

    @Test
    public void dereferenceValues() throws Exception {

    }

    @Test
    public void getMatchedRecords() throws Exception {

    }

    @Test(expected = IllegalArgumentException.class)
    public void getRefBookThrowsExceptionOnNullRefBook() throws Exception {
        provider.setRefBook(null);
        provider.getRefBook();
    }

    @Test
    public void setRefBook() throws Exception {
        provider.setRefBook(getRefBookStub());

        assertNotNull(provider.getRefBook());
    }

    @Test
    public void getRefBookId() throws Exception {
        provider.setRefBook(getRefBookStub());

        assertEquals(Long.class, provider.getRefBookId().getClass());
    }
}