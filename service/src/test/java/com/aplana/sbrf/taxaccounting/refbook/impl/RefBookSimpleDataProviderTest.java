package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class RefBookSimpleDataProviderTest {

    private static final long TEST_RECORD_ID = 122934L;
    private static final long RFB_ID = RefBook.Id.ASNU.getId();
    private static final String RFB_TABLE_NAME = RefBook.Table.ASNU.getTable();
    private static final Long RFB_NOT_VERSIONED_ID = 100L;

    private RefBookSimpleDataProvider provider;
    private RefBookSimpleDao daoMock;
    private RefBookDao refBookDaoMock;
    private RefBookSimpleReadOnly SimpleReadOnlyMock;

    @Before
    public void setUp() throws Exception {
        provider = new RefBookSimpleDataProvider();

        daoMock = mock(RefBookSimpleDao.class);
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
                eq(RFB_TABLE_NAME), eq(RFB_ID), eq(version), any(PagingParams.class),
                anyString(), any(RefBookAttribute.class), eq(true));
    }

//    @Test
    public void getChildrenRecordsInvokesDao() throws Exception {
        provider.getChildrenRecords(1L, null, null, null, null);

        verify(daoMock, atLeastOnce()).getChildrenRecords(eq(RFB_TABLE_NAME), eq(RFB_ID),
                any(Date.class), anyLong(), any(PagingParams.class), anyString(), any(RefBookAttribute.class));
    }

    @Test
    public void getRowNumInvokesDao() throws Exception {
        provider.getRowNum(null, TEST_RECORD_ID, null, null, true);

        verify(daoMock, atLeastOnce()).getRowNum(eq(RFB_ID), any(Date.class), anyLong(), anyString(),
                any(RefBookAttribute.class), anyBoolean());
    }

    @Test
    public void getRecordDataInvokesDao() throws Exception {
        provider.getRecordData(TEST_RECORD_ID);

        verify(refBookDaoMock).getRecordData(RFB_ID, RFB_TABLE_NAME, TEST_RECORD_ID);
    }

    @Test
    public void getUniqueRecordIdsInvokesDao() throws Exception {
        Date version = new GregorianCalendar(1983, Calendar.JULY, 22).getTime();
        String filter = "FILTER";
        provider.getUniqueRecordIds(version, filter);

        verify(daoMock).getUniqueRecordIds(RFB_ID, RFB_TABLE_NAME, version, filter);
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

        verify(daoMock).getRecordVersionInfo(RFB_TABLE_NAME, 3L);
    }

    @Test
    public void getRecordVersionInfoNotInvokesDaoOnNotVersionedRefBook() throws Exception {
        provider.setRefBook(getRefBookNotVersionedStub());
        provider.getRecordVersionInfo(3L);

        verify(daoMock, never()).getRecordVersionInfo(anyString(), anyLong());
    }

    @Test
    public void getRecordVersionInfoNotInvokesFromReadOnly() throws Exception {
        provider.setRefBook(getRefBookNotVersionedStub());
        provider.getRecordVersionInfo(3L);

        verify(SimpleReadOnlyMock).getRecordVersionInfo(3L);
    }

    @Test
    public void getVersionsInvokesDao() throws Exception {
        Date versionStart = new Date(0);
        Date versionEnd = new Date();
        provider.getVersions(versionStart, versionEnd);

        verify(daoMock).getVersions(RFB_TABLE_NAME, versionStart, versionEnd);
    }

    @Test
    public void getVersionsNotInvokesDaoOnNotVersionedRefBook() throws Exception {
        provider.setRefBook(getRefBookNotVersionedStub());
        Date versionStart = new Date(0);
        Date versionEnd = new Date();
        provider.getVersions(versionStart, versionEnd);

        verify(daoMock, never()).getVersions(anyString(), any(Date.class), any(Date.class));
    }

    @Test
    public void getRecordVersionsCountInvokesDao() throws Exception {
        provider.getRecordVersionsCount(4L);

        verify(daoMock).getRecordVersionsCount(RFB_TABLE_NAME, 4L);
    }

    @Test
    public void getRecordVersionsCountNotInvokesDaoOnNotVersionedRefBook() throws Exception {
        provider.setRefBook(getRefBookNotVersionedStub());
        provider.getRecordVersionsCount(4L);

        verify(daoMock, never()).getRecordVersionsCount(RFB_TABLE_NAME, 4L);
    }
}