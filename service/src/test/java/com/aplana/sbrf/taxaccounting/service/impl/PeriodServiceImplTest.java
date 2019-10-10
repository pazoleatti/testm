package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.OpenCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.model.builder.DepartmentReportPeriodBuilder;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import com.aplana.sbrf.taxaccounting.model.result.ClosePeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.DeletePeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.OpenPeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.ReopenPeriodResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookFormTypeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.mock.UserMockUtils.mockUser;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("PeriodServiceImplTest.xml")
public class PeriodServiceImplTest {
    @Autowired
    PeriodService periodService;
    @Autowired
    ReportPeriodDao reportPeriodDao;
    @Autowired
    RefBookDataProvider provider;
    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    CommonRefBookService commonRefBookService;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    DepartmentDao departmentDao;
    @Autowired
    ReportPeriodService reportPeriodService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    DeclarationTemplateService declarationTemplateService;
    @Autowired
    LockDataService lockDataService;
    @Autowired
    RefBookFormTypeService refBookFormTypeService;

    @Captor
    private ArgumentCaptor<ArrayList<LogEntry>> logEntriesArgumentCaptor;
    @Captor
    private ArgumentCaptor<ArrayList<Integer>> integerListArgumentCaptor;

    private final static String LOCAL_IP = "127.0.0.1";
    private static final int CONTROL_USER_ID = 2;
    private TAUserInfo userInfo = new TAUserInfo();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        reset(departmentReportPeriodService, logEntryService);

        userInfo.setIp(LOCAL_IP);
        userInfo.setUser(mockUser(CONTROL_USER_ID, 1, TARole.N_ROLE_CONTROL_NS));

        when(declarationDataSearchService.getDeclarationData(any(DeclarationDataFilter.class), any(DeclarationDataSearchOrdering.class), anyBoolean()))
                .thenReturn(new ArrayList<DeclarationData>());

        mockDecalrationTemplate();
        mockDepartments();
        mockTaxFormTypes();
    }

    private void mockDecalrationTemplate() {
        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(1);
        DeclarationType type = new DeclarationType();
        type.setName("formName");
        declarationTemplate.setType(type);
        when(declarationTemplateService.get(1)).thenReturn(declarationTemplate);
    }

    private void mockDepartments() {
        when(departmentDao.findAllChildrenIdsById(1)).thenReturn(asList(1, 2, 3));
        Department department = new Department();
        department.setId(1);
        department.setName("dep1Name");
        when(departmentDao.getDepartment(1)).thenReturn(department);
        when(departmentService.getDepartment(1)).thenReturn(department);
    }

    private void mockTaxFormTypes() {
        RefBookFormType ndfl6FormType = RefBookFormType.NDFL_6;
        ndfl6FormType.setCode("6НДФЛ");
        when(refBookFormTypeService.findOne(anyInt())).thenReturn(ndfl6FormType);
    }

    @Test
    public void open() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(reportPeriodService.fetchOrCreate(any(TaxPeriod.class), any(ReportPeriodType.class), eq(RefBookFormType.NDFL_6.getId().intValue())))
                .thenReturn(departmentReportPeriod.getReportPeriod());

        periodService.open(departmentReportPeriod, null);

        verify(departmentReportPeriodService, times(1)).create(any(DepartmentReportPeriod.class), eq(asList(1, 2, 3)));
        verify(logEntryService, times(1)).save(logEntriesArgumentCaptor.capture());
        assertEquals(1, logEntriesArgumentCaptor.getValue().size());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" открыт для \"dep1Name\" и всех дочерних подразделений", logEntriesArgumentCaptor.getValue().get(0).getMessage());
    }

    @Test
    public void openExists() {
        DepartmentReportPeriodBuilder departmentReportPeriodBuilder = new DepartmentReportPeriodBuilder()
                .department(1)
                .reportPeriodTaxFormTypeId(RefBookFormType.NDFL_6.getId().intValue())
                .year(2018)
                .dictTaxPeriodId(1L);

        DepartmentReportPeriod existDepartmentReportPeriod = departmentReportPeriodBuilder.but()
                .active(true)
                .reportPeriodId(1)
                .reportPeriodName("reportPeriodName")
                .build();
        when(reportPeriodService.fetchOrCreate(any(TaxPeriod.class), any(ReportPeriodType.class), eq(RefBookFormType.NDFL_6.getId().intValue())))
                .thenReturn(existDepartmentReportPeriod.getReportPeriod());
        when(departmentReportPeriodService.fetchOneByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(existDepartmentReportPeriod);

        OpenPeriodResult result = periodService.open(departmentReportPeriodBuilder.build(), null);
        verify(departmentReportPeriodService, never()).create(any(DepartmentReportPeriod.class), anyListOf(Integer.class));
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" уже существует и открыт для подразделения \"dep1Name\" и всех дочерних подразделений", result.getError());
    }

    @Test(expected = ServiceException.class)
    public void openDaoException() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(reportPeriodService.fetchOrCreate(any(TaxPeriod.class), any(ReportPeriodType.class), eq(RefBookFormType.NDFL_6.getId().intValue())))
                .thenReturn(departmentReportPeriod.getReportPeriod());
        doThrow(new DaoException("123")).when(departmentReportPeriodService).create(any(DepartmentReportPeriod.class), anyListOf(Integer.class));

        try {
            periodService.open(departmentReportPeriod, null);
        } catch (ServiceException e) {
            assertEquals("Ошибка при открытии периода \"2018:reportPeriodName:6НДФЛ\" для подразделения \"dep1Name\" и всех дочерних подразделений. Обратитесь к администратору.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void openCorrection() {
        DepartmentReportPeriodBuilder mainPeriodBuilder = getDepartmentReportPeriodBuilder(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(mainPeriodBuilder.build());
        DepartmentReportPeriod lastPeriod = mainPeriodBuilder.but().build();
        when(departmentReportPeriodService.fetchLast(anyInt(), anyInt())).thenReturn(lastPeriod);

        periodService.openCorrectionPeriod(new OpenCorrectionPeriodAction(123, new Date(2018 - 1900, Calendar.JANUARY, 1)));

        verify(departmentReportPeriodService, times(1)).create(any(DepartmentReportPeriod.class), eq(asList(1, 2, 3)));
        verify(logEntryService, times(1)).save(logEntriesArgumentCaptor.capture());
        assertEquals(1, logEntriesArgumentCaptor.getValue().size());
        assertEquals("Корректирующий период \"2018:reportPeriodName:6НДФЛ\" с периодом сдачи корректировки 01.01.2018 открыт для \"dep1Name\" и всех дочерних подразделений", logEntriesArgumentCaptor.getValue().get(0).getMessage());
    }

    @Test
    public void openCorrectionExists() {
        DepartmentReportPeriodBuilder mainPeriodBuilder = getDepartmentReportPeriodBuilder(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(mainPeriodBuilder.build());
        DepartmentReportPeriod lastPeriod = mainPeriodBuilder.but().correctionDate(new Date(2018 - 1900, 0, 1)).build();
        when(departmentReportPeriodService.fetchLast(anyInt(), anyInt())).thenReturn(lastPeriod);
        when(departmentReportPeriodService.fetchOneByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(lastPeriod);

        OpenPeriodResult result = periodService.openCorrectionPeriod(new OpenCorrectionPeriodAction(123, new Date(2018 - 1900, 0, 1)));
        verify(departmentReportPeriodService, never()).create(any(DepartmentReportPeriod.class), anyListOf(Integer.class));
        assertEquals("Корректирующий период \"2018:reportPeriodName:6НДФЛ\" с периодом сдачи корректировки 01.01.2018 уже существует и закрыт для подразделения \"dep1Name\" и всех дочерних подразделений", result.getError());
    }

    @Test
    public void openCorrectionWhenOpenedCorrectionPeriodExists() {
        DepartmentReportPeriodBuilder mainPeriodBuilder = getDepartmentReportPeriodBuilder(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(mainPeriodBuilder.build());
        DepartmentReportPeriod lastPeriod = mainPeriodBuilder.but().correctionDate(new Date(2018 - 1900, 11, 1)).active(true).build();
        when(departmentReportPeriodService.fetchLast(anyInt(), anyInt())).thenReturn(lastPeriod);

        OpenPeriodResult result = periodService.openCorrectionPeriod(new OpenCorrectionPeriodAction(123, new Date(2018 - 1900, 0, 1)));
        verify(departmentReportPeriodService, never()).create(any(DepartmentReportPeriod.class), anyListOf(Integer.class));
        assertEquals("Корректирующий период \"2018:reportPeriodName:6НДФЛ\" с периодом сдачи корректировки 01.01.2018 не может быть открыт, т.к уже открыт другой корректирующий период!", result.getError());
    }

    @Test
    public void openCorrectionWhenLaterCorrectionPeriodExists() {
        DepartmentReportPeriodBuilder mainPeriodBuilder = getDepartmentReportPeriodBuilder(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(mainPeriodBuilder.build());
        DepartmentReportPeriod lastPeriod = mainPeriodBuilder.but().build();
        when(departmentReportPeriodService.fetchLast(anyInt(), anyInt())).thenReturn(lastPeriod);
        when(departmentReportPeriodService.isLaterCorrectionPeriodExists(any(DepartmentReportPeriod.class))).thenReturn(true);

        OpenPeriodResult result = periodService.openCorrectionPeriod(new OpenCorrectionPeriodAction(123, new Date(2018 - 1900, Calendar.JANUARY, 1)));
        verify(departmentReportPeriodService, never()).create(any(DepartmentReportPeriod.class), anyListOf(Integer.class));
        assertEquals("Корректирующий период \"2018:reportPeriodName:6НДФЛ\" с периодом сдачи корректировки 01.01.2018 не может быть открыт, т.к. для него существует более поздние корректирующие периоды!", result.getError());
    }

    @Test(expected = ServiceException.class)
    public void openCorrectionDaoException() {
        DepartmentReportPeriodBuilder mainPeriodBuilder = getDepartmentReportPeriodBuilder(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(mainPeriodBuilder.build());
        DepartmentReportPeriod lastPeriod = mainPeriodBuilder.but().build();
        when(departmentReportPeriodService.fetchLast(anyInt(), anyInt())).thenReturn(lastPeriod);
        doThrow(new DaoException("123")).when(departmentReportPeriodService).create(any(DepartmentReportPeriod.class), anyListOf(Integer.class));

        try {
            periodService.openCorrectionPeriod(new OpenCorrectionPeriodAction(123, new Date(2018 - 1900, 0, 1)));
        } catch (ServiceException e) {
            assertEquals("Ошибка при открытии корректирующего периода \"2018:reportPeriodName:6НДФЛ\" с периодом сдачи корректировки 01.01.2018 для подразделения \"dep1Name\" и всех дочерних подразделений. Обратитесь к администратору.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void close() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(true);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(asList(1, 2, 3));

        periodService.close(123, false);

        verify(departmentReportPeriodService, times(1)).updateActive(integerListArgumentCaptor.capture(), eq(1), eq(false));
        assertEquals(integerListArgumentCaptor.getValue(), asList(1, 2, 3));
        verify(logEntryService, times(1)).save(logEntriesArgumentCaptor.capture());
        assertEquals(1, logEntriesArgumentCaptor.getValue().size());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" закрыт для подразделения \"dep1Name\" и всех дочерних подразделений", logEntriesArgumentCaptor.getValue().get(0).getMessage());
    }

    @Test
    public void closeClosed() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);

        ClosePeriodResult result = periodService.close(123, false);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" не может быть закрыт для подразделения \"dep1Name\" и всех дочерних подразделений, поскольку он уже закрыт", result.getError());
    }

    @Test
    public void closeHasBlockedForms() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(true);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        when(declarationDataSearchService.getDeclarationData(any(DeclarationDataFilter.class), any(DeclarationDataSearchOrdering.class), anyBoolean()))
                .thenReturn(singletonList(declarationData));

        ClosePeriodResult result = periodService.close(123, false);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" не может быть закрыт для подразделения \"dep1Name\" и всех дочерних подразделений, т.к. в нём существуют заблокированные налоговые или отчетные формы. Перечень форм приведен в списке уведомлений", result.getError());
    }

    @Test
    public void closeHasNotAcceptedForms() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(true);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        when(declarationDataSearchService.getDeclarationData(any(DeclarationDataFilter.class), any(DeclarationDataSearchOrdering.class), anyBoolean()))
                .thenReturn(new ArrayList<DeclarationData>(), singletonList(declarationData), new ArrayList<DeclarationData>());
        LockDataDTO lockDataItem = new LockDataDTO();
        lockDataItem.setKey("DECLARATION_DATA_1");
        when(lockDataService.fetchAllByKeySet(anySetOf(String.class))).thenReturn(singletonList(lockDataItem));

        ClosePeriodResult result = periodService.close(123, false);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("В периоде \"2018:reportPeriodName:6НДФЛ\" существуют налоговые или отчетные формы в состоянии отличном от \"Принято\". Перечень форм приведен в списке уведомлений. Все равно закрыть период?", result.getError());
        assertFalse(result.isFatal());
    }

    @Test(expected = ServiceException.class)
    public void closeDaoException() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(true);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenAnswer(new Answer<List<Integer>>() {
            @Override
            public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                Boolean isActive = ((DepartmentReportPeriodFilter) invocation.getArguments()[0]).isActive();
                return isActive != null && !isActive ? Collections.<Integer>emptyList() : asList(1, 2, 3);
            }
        });
        doThrow(new DaoException("123")).when(departmentReportPeriodService).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());

        try {
            periodService.close(123, false);
        } catch (ServiceException e) {
            assertEquals("Ошибка при закрытии периода \"2018:reportPeriodName:6НДФЛ\" для подразделения \"dep1Name\" и всех дочерних подразделений. Обратитесь к администратору.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void reopen() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenAnswer(new Answer<List<Integer>>() {
            @Override
            public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                DepartmentReportPeriodFilter filterArg = ((DepartmentReportPeriodFilter) invocation.getArguments()[0]);
                return filterArg.isCorrection() != null && filterArg.isCorrection() ? Collections.<Integer>emptyList() : asList(1, 2, 3);
            }
        });

        periodService.reopen(123);

        verify(departmentReportPeriodService, times(1)).updateActive(integerListArgumentCaptor.capture(), eq(1), eq(true));
        assertEquals(integerListArgumentCaptor.getValue(), asList(1, 2, 3));
        verify(logEntryService, times(1)).save(logEntriesArgumentCaptor.capture());
        assertEquals(1, logEntriesArgumentCaptor.getValue().size());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" переоткрыт для подразделения \"dep1Name\" и всех дочерних подразделений", logEntriesArgumentCaptor.getValue().get(0).getMessage());
    }

    @Test(expected = ServiceException.class)
    public void reopenDaoException() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenAnswer(new Answer<List<Integer>>() {
            @Override
            public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                DepartmentReportPeriodFilter filterArg = ((DepartmentReportPeriodFilter) invocation.getArguments()[0]);
                return filterArg.isCorrection() != null && filterArg.isCorrection() ? Collections.<Integer>emptyList() : asList(1, 2, 3);
            }
        });
        doThrow(new DaoException("123")).when(departmentReportPeriodService).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());

        try {
            periodService.reopen(123);
        } catch (ServiceException e) {
            assertEquals("Ошибка при переоткрытии периода \"2018:reportPeriodName:6НДФЛ\" для подразделения " +
                    "\"dep1Name\" и всех дочерних подразделений. Обратитесь к администратору.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void reopenOpened() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(true);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);

        ReopenPeriodResult result = periodService.reopen(123);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" уже открыт для подразделения \"dep1Name\" и всех дочерних подразделений", result.getError());
    }

    @Test
    public void reopenWhenCorrectionPeriodExists() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(asList(1, 2, 3));

        ReopenPeriodResult result = periodService.reopen(123);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" не может быть переоткрыт, т.к. для него созданы корректирующие периоды!", result.getError());
    }

    @Test
    public void reopenWhenLaterCorrectionPeriodExists() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriodWithCorrectionDate();
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.isLaterCorrectionPeriodExists(any(DepartmentReportPeriod.class))).thenReturn(true);

        ReopenPeriodResult result = periodService.reopen(123);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ (корр. 01.01.2018)\" не может быть переоткрыт, т.к. для него существуют более поздние корректирующие периоды!", result.getError());
    }

    @Test
    public void delete() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenAnswer(new Answer<List<Integer>>() {
            @Override
            public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                DepartmentReportPeriodFilter filterArg = ((DepartmentReportPeriodFilter) invocation.getArguments()[0]);
                return filterArg.isCorrection() != null && filterArg.isCorrection() ? Collections.<Integer>emptyList() : asList(1, 2, 3);
            }
        });

        periodService.delete(123);

        verify(departmentReportPeriodService, times(1)).delete(eq(asList(1, 2, 3)));
        verify(logEntryService, times(1)).save(logEntriesArgumentCaptor.capture());
        assertEquals(1, logEntriesArgumentCaptor.getValue().size());
        assertEquals("Период \"2018:reportPeriodName:6НДФЛ\" удалён для подразделения \"dep1Name\" " +
                "и всех дочерних подразделений", logEntriesArgumentCaptor.getValue().get(0).getMessage());
    }

    @Test(expected = ServiceException.class)
    public void deleteDaoException() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenAnswer(new Answer<List<Integer>>() {
            @Override
            public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                DepartmentReportPeriodFilter filterArg = ((DepartmentReportPeriodFilter) invocation.getArguments()[0]);
                return filterArg.isCorrection() != null && filterArg.isCorrection() ? Collections.<Integer>emptyList() : asList(1, 2, 3);
            }
        });
        doThrow(new DaoException("123")).when(departmentReportPeriodService).delete(anyListOf(Integer.class));

        try {
            periodService.delete(123);
        } catch (ServiceException e) {
            assertEquals("Ошибка при удалении периода \"2018:reportPeriodName:6НДФЛ\" для подразделения " +
                    "\"dep1Name\" и всех дочерних подразделений. Обратитесь к администратору.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void deleteWhenCorrectionPeriodExists() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriod(false);
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.fetchAllIdsByFilter(any(DepartmentReportPeriodFilter.class))).thenReturn(asList(1, 2, 3));

        DeletePeriodResult result = periodService.delete(123);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Удаление периода \"2018:reportPeriodName:6НДФЛ\" для подразделения \"dep1Name\" " +
                "и всех дочерних подразделений невозможно, т.к. для него существует корректирующий период.",
                result.getError());
    }

    @Test
    public void deleteWhenLaterCorrectionPeriodExists() {
        DepartmentReportPeriod departmentReportPeriod = getTestDataForDepartmentReportPeriodWithCorrectionDate();
        when(departmentReportPeriodService.fetchOne(123)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.isLaterCorrectionPeriodExists(any(DepartmentReportPeriod.class))).thenReturn(true);

        DeletePeriodResult result = periodService.delete(123);
        verify(departmentReportPeriodService, never()).updateActive(anyListOf(Integer.class), anyInt(), anyBoolean());
        assertEquals("Удаление корректирующего периода \"2018:reportPeriodName:6НДФЛ (корр. 01.01.2018)\" " +
                "для подразделения \"dep1Name\" и всех дочерних подразделений невозможно, т.к. для него существует " +
                "более поздний корректирующий период.", result.getError());
    }

    private DepartmentReportPeriod getTestDataForDepartmentReportPeriodWithCorrectionDate() {
        return new DepartmentReportPeriodBuilder()
                .active(false)
                .correctionDate(new Date(2018 - 1900, Calendar.JANUARY, 1))
                .reportPeriodId(1)
                .reportPeriodTaxFormTypeId(RefBookFormType.NDFL_6.getId().intValue())
                .reportPeriodName("reportPeriodName")
                .department(1)
                .year(2018)
                .build();
    }

    private DepartmentReportPeriod getTestDataForDepartmentReportPeriod(boolean active) {
        return getDepartmentReportPeriodBuilder(active).build();
    }

    private DepartmentReportPeriodBuilder getDepartmentReportPeriodBuilder(boolean active) {
        return new DepartmentReportPeriodBuilder()
                .active(active)
                .reportPeriodTaxFormTypeId(RefBookFormType.NDFL_6.getId().intValue())
                .reportPeriodId(1)
                .reportPeriodName("reportPeriodName")
                .department(1)
                .year(2018)
                .taxPeriodId(1)
                .dictTaxPeriodId(1L);
    }
}
