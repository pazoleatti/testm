package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class PrintingServiceImplTest {

    @InjectMocks
    private PrintingServiceImpl printingService;
    @Mock
    private PersonService personService;
    @Mock
    private CommonRefBookService commonRefBookService;
    @Mock
    private BlobDataService blobDataService;
    @Mock
    private RefBookDepartmentDao refBookDepartmentDao;
    @Captor
    private ArgumentCaptor<String> infoPath;
    @Captor
    private ArgumentCaptor<String> fileName;
    private long refBookId;
    @Mock
    private Date version;
    private String searchPattern;
    private boolean exactSearch;
    @Mock
    private Map<String, String> extraParams;
    @Mock
    private RefBookAttribute sortAttribute;
    private String direction;
    @Mock
    private LockStateLogger lockStateLogger;
    @Mock
    private RefBook refBook;
    @Mock
    private Map<String, RefBookValue> record;
    private List<RefBookDepartment> departments = new ArrayList<>();
    @Mock
    private PagingResult<Map<String, RefBookValue>> records;
    @Mock
    private RefBookValue refBookValue;
    @Mock
    private Iterator<Map<String, RefBookValue>> iterator;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        MockitoAnnotations.initMocks(this);
        refBookId = 0L;
        searchPattern = "searchPattern";
        exactSearch = false;
        direction = "asc";
    }

    @Test
    public void test_generateRefBookCsv_hier() {

        when(commonRefBookService.get(0L)).thenReturn(refBook);
        when(refBookDepartmentDao.findAllByNameAsTree(searchPattern, exactSearch)).thenReturn(departments);
        when(refBook.isHierarchic()).thenReturn(true);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(record);
        when(records.iterator()).thenReturn(iterator);
        when(record.get(RefBook.RECORD_PARENT_ID_ALIAS)).thenReturn(refBookValue);
        when(refBookValue.getReferenceValue()).thenReturn(0L);
        when(refBook.getName()).thenReturn("refbookName");
        when(refBook.isVersioned()).thenReturn(false);

        printingService.generateRefBookCSV(refBookId, version, searchPattern, exactSearch, extraParams, sortAttribute, direction, lockStateLogger);
        Mockito.verify(blobDataService).create(infoPath.capture(), fileName.capture());
    }

    @Test
    public void test_generateRefBookCsv_linear() {
        when(commonRefBookService.get(0L)).thenReturn(refBook);
        when(refBook.isHierarchic()).thenReturn(false);

        when(refBook.getName()).thenReturn("refbookName");
        when(commonRefBookService.fetchAllRecords(refBookId, null, version, searchPattern, exactSearch, extraParams, null, sortAttribute, direction)).thenReturn(records);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(record);
        when(records.iterator()).thenReturn(iterator);

        printingService.generateRefBookCSV(refBookId, version, searchPattern, exactSearch, extraParams, sortAttribute, direction, lockStateLogger);
        Mockito.verify(blobDataService).create(infoPath.capture(), fileName.capture());
    }

    @Test
    public void test_generateRefBookExcel_hier() {
        when(commonRefBookService.get(0L)).thenReturn(refBook);
        when(refBook.isHierarchic()).thenReturn(true);
        when(refBook.getName()).thenReturn("refbookName");
        when(refBookDepartmentDao.findAllByNameAsTree(searchPattern, exactSearch)).thenReturn(departments);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(record);
        when(records.iterator()).thenReturn(iterator);
        when(record.get(RefBook.RECORD_PARENT_ID_ALIAS)).thenReturn(refBookValue);

        printingService.generateRefBookExcel(refBookId, version, searchPattern, exactSearch, extraParams, sortAttribute, direction, lockStateLogger);
        Mockito.verify(blobDataService).create(infoPath.capture(), fileName.capture());
    }

    @Test
    public void test_generateRefBookExcel_linear() {
        ArgumentCaptor<PagingParams> pagingParams = ArgumentCaptor.forClass(PagingParams.class);

        when(commonRefBookService.get(0L)).thenReturn(refBook);
        when(refBook.isHierarchic()).thenReturn(false);
        when(refBook.getName()).thenReturn("refbookName");
        when(commonRefBookService.fetchAllRecords(eq(refBookId), anyLong(), eq(version), eq(searchPattern), eq(exactSearch), eq(extraParams), pagingParams.capture(), eq(sortAttribute), eq(direction))).thenReturn(records);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(record);
        when(records.iterator()).thenReturn(iterator);

        printingService.generateRefBookExcel(refBookId, version, searchPattern, exactSearch, extraParams, sortAttribute, direction, lockStateLogger);
        Mockito.verify(blobDataService).create(infoPath.capture(), fileName.capture());
    }

}
