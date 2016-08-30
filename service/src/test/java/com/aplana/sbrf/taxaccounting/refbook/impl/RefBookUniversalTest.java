package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

public class RefBookUniversalTest {

    private RefBookUniversal provider;
    private RefBookDao refBookDao;
    private LockDataService lockService;
    private Logger logger;
    private RefBook refBook;
    private BDUtils dbUtils;
    private RefBookRecord refBookRecord;
    private List<RefBookAttribute> attributes;

    @Before
    public void init() {
        provider = new RefBookUniversal();
        provider.setRefBookId(13L);

        refBookRecord = new RefBookRecord();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        values.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test"));
        refBookRecord.setRecordId(1L);
        refBookRecord.setValues(values);

        RefBookFactory refBookFactory = mock(RefBookFactoryImpl.class);
        ReflectionTestUtils.setField(provider, "refBookFactory", refBookFactory);

        refBookDao = mock(RefBookDao.class);
        ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);

        LogEntryService logEntryService = mock(LogEntryService.class);
        ReflectionTestUtils.setField(provider, "logEntryService", logEntryService);

        dbUtils = mock(BDUtils.class);
        when(dbUtils.getNextIds(any(BDUtils.Sequence.class), eq(1L))).thenReturn(Arrays.asList(1L));
        ReflectionTestUtils.setField(provider, "dbUtils", dbUtils);

        lockService = mock(LockDataService.class);
        ReflectionTestUtils.setField(provider, "lockService", lockService);

        FormDataService formDataService = mock(FormDataService.class);
        ReflectionTestUtils.setField(provider, "formDataService", formDataService);

        RefBookHelper refBookHelper = mock(RefBookHelper.class);
        ReflectionTestUtils.setField(provider, "refBookHelper", refBookHelper);

        logger = new Logger();
        TAUserInfo taUserInfo = new TAUserInfo();
        taUserInfo.setUser(new TAUser());
        logger.setTaUserInfo(taUserInfo);

        refBook = mock(RefBook.class);
        when(refBookDao.get(any(Long.class))).thenReturn(refBook);
        when(refBook.getName()).thenReturn("Виды услуг");
        when(refBook.isVersioned()).thenReturn(true);
        when(refBook.isHierarchic()).thenReturn(false);

        attributes = new ArrayList<RefBookAttribute>();
        RefBookAttribute attributeName = new RefBookAttribute();
        attributeName.setName("Название");
        attributeName.setAlias("NAME");
        attributeName.setRequired(true);
        attributeName.setId(1L);
        attributeName.setMaxLength(10);
        attributeName.setAttributeType(RefBookAttributeType.STRING);
        attributes.add(attributeName);
        when(refBook.getAttributes()).thenReturn(attributes);

        when(lockService.lock(any(String.class), any(Integer.class), any(String.class), any(String.class))).thenReturn(new LockData());
    }

    /**************** Создание версии **************************/

    @Test
    public void createRecordVersionTest1() {
        //Успешно
        provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest2() {
        //Не заполнен обязательный атрибут
        refBookRecord.getValues().get("NAME").setValue(null);
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Поля [Название] являются обязательными для заполнения"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest3() {
        //Превышен размер текстового значения атрибута
        refBookRecord.getValues().get("NAME").setValue("12345678901234567");
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Обнаружено некорректное значение атрибута"));
            assertTrue(hasError(logger, "значение атрибута превышает максимально допустимое"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest4() {
        //Слишком большое число
        attributes.get(0).setAttributeType(RefBookAttributeType.NUMBER);
        attributes.get(0).setPrecision(1);
        refBookRecord.getValues().put("NAME", new RefBookValue(RefBookAttributeType.NUMBER, 1234567890123.2));
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Обнаружено некорректное значение атрибута"));
            assertTrue(hasError(logger, "значение атрибута не соответствует формату: максимальное количество цифр"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest5() {
        //Слишком большая часть после запятой
        attributes.get(0).setAttributeType(RefBookAttributeType.NUMBER);
        attributes.get(0).setPrecision(1);
        refBookRecord.getValues().put("NAME", new RefBookValue(RefBookAttributeType.NUMBER, 1.123));
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Обнаружено некорректное значение атрибута"));
            assertTrue(hasError(logger, "значение атрибута не соответствует формату: максимальное количество цифр"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest6() {
        //Не пройдена проверка использования
        List<RefBookRecord> records = Arrays.asList(refBookRecord);
        List<Pair<Long,String>> matchedRecords = new ArrayList<Pair<Long, String>>();
        matchedRecords.add(new Pair<Long, String>(3L, "test1"));
        matchedRecords.add(new Pair<Long, String>(4L, "test2"));
        when(refBookDao.getMatchedRecordsByUniqueAttributes(any(Long.class), any(Long.class), eq(attributes), eq(records))).thenReturn(matchedRecords);
        when(refBookDao.checkConflictValuesVersions(eq(matchedRecords), any(Date.class), any(Date.class))).thenReturn(Arrays.asList(3L));
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, records);
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Нарушено требование к уникальности, уже существуют записи"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest7() {
        //Не пройдена проверка справочных значений
        List<RefBookRecord> records = Arrays.asList(refBookRecord);
        List<Pair<Long,String>> matchedRecords = new ArrayList<Pair<Long, String>>();
        matchedRecords.add(new Pair<Long, String>(3L, "test1"));
        matchedRecords.add(new Pair<Long, String>(4L, "test2"));
        when(refBookDao.getMatchedRecordsByUniqueAttributes(any(Long.class), any(Long.class), eq(attributes), eq(records))).thenReturn(matchedRecords);
        when(refBookDao.checkConflictValuesVersions(eq(matchedRecords), any(Date.class), any(Date.class))).thenReturn(Arrays.asList(3L));
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, records);
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Нарушено требование к уникальности, уже существуют записи"));
            throw new TestException();
        }
    }

    /**************** Проверка пересечения **************************/

    @Test(expected = TestException.class)
    public void createRecordVersionTest8() {
        //Фатальное пересечение
        List<CheckCrossVersionsResult> results = new ArrayList<CheckCrossVersionsResult>();
        results.add(new CheckCrossVersionsResult(1, 1L, new Date(), VersionedObjectStatus.NORMAL, null, null, CrossResult.FATAL_ERROR));
        when(refBookDao.checkCrossVersions(any(Long.class), any(Long.class), any(Date.class), any(Date.class), any(Long.class))).thenReturn(results);
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Обнаружено пересечение указанного срока актуальности с существующей версией!"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest9() {
        //Пересечение с проверкой использования
        List<CheckCrossVersionsResult> results = new ArrayList<CheckCrossVersionsResult>();
        results.add(new CheckCrossVersionsResult(1, 1L, new Date(), VersionedObjectStatus.NORMAL, null, null, CrossResult.NEED_CHECK_USAGES));
        when(refBookDao.checkCrossVersions(any(Long.class), any(Long.class), any(Date.class), any(Date.class), any(Long.class))).thenReturn(results);
        when(refBookDao.isVersionUsedInRefBooks(any(Long.class), eq(Arrays.asList(1L)), any(Date.class), any(Date.class), any(Boolean.class), any(List.class))).thenReturn(Arrays.asList("error"));
        try {
            provider.createRecordVersion(logger, getDate(1,1,2015), null, Arrays.asList(refBookRecord));
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Обнаружено пересечение указанного срока актуальности с существующей версией!"));
            throw new TestException();
        }
    }

    /**************** Проверка правильности установки даты начала и окончания **************************/

    @Test
    public void createRecordVersionTest10() {
        //Дата окончания не указана и не изменилась
        Date dateFrom = getDate(1, 1, 2015);
        provider.createRecordVersion(logger, dateFrom, null, Collections.singletonList(refBookRecord));
        verify(refBookDao).createRecordVersion(any(Long.class), eq(dateFrom), eq(VersionedObjectStatus.NORMAL), any(List.class));
        verify(refBookDao, never()).createFakeRecordVersion(any(Long.class), any(Long.class), any(Date.class));
    }

    @Test
    public void createRecordVersionTest11() {
        //Дата окончания указана и не изменилась
        Date dateFrom = getDate(1, 1, 2015);
        Date dateTo = getDate(1, 1, 2016);
        provider.createRecordVersion(logger, dateFrom, dateTo, Arrays.asList(refBookRecord));
        verify(refBookDao).createRecordVersion(any(Long.class), eq(dateFrom), eq(VersionedObjectStatus.NORMAL), any(List.class));
        verify(refBookDao).createFakeRecordVersion(any(Long.class), any(Long.class), eq(SimpleDateUtils.addDayToDate(dateTo, 1)));
    }

    @Test
    public void createRecordVersionTest12() {
        //Дата окончания указана и изменилась на 28.02.2015
        Date dateFrom = getDate(1, 1, 2015);
        Date dateTo = getDate(1, 1, 2016);
        Date realDateTo = getDate(28, 2, 2015);

        RefBookRecordVersion nextVersion = new RefBookRecordVersion();
        nextVersion.setVersionStart(getDate(1, 3, 2015));
        nextVersion.setVersionEnd(getDate(1, 3, 2016));
        nextVersion.setVersionEndFake(true);
        when(refBookDao.getNextVersion(any(Long.class), any(Long.class), any(Date.class))).thenReturn(nextVersion);

        provider.createRecordVersion(logger, dateFrom, dateTo, Arrays.asList(refBookRecord));
        verify(refBookDao).createRecordVersion(any(Long.class), eq(dateFrom), eq(VersionedObjectStatus.NORMAL), any(List.class));
        verify(refBookDao, never()).createFakeRecordVersion(any(Long.class), any(Long.class), any(Date.class));
    }

    @Test
    public void createRecordVersionTest13() {
        //Дата окончания не указана и изменилась на 28.02.2015
        Date dateFrom = getDate(1, 1, 2015);
        Date realDateTo = getDate(28, 2, 2015);

        RefBookRecordVersion nextVersion = new RefBookRecordVersion();
        nextVersion.setVersionStart(getDate(1, 3, 2015));
        nextVersion.setVersionEnd(getDate(1, 3, 2016));
        nextVersion.setVersionEndFake(true);
        when(refBookDao.getNextVersion(any(Long.class), any(Long.class), any(Date.class))).thenReturn(nextVersion);

        provider.createRecordVersion(logger, dateFrom, null, Arrays.asList(refBookRecord));
        verify(refBookDao).createRecordVersion(any(Long.class), eq(dateFrom), eq(VersionedObjectStatus.NORMAL), any(List.class));
        verify(refBookDao, never()).createFakeRecordVersion(any(Long.class), any(Long.class), any(Date.class));
    }

    /**************** Обновление версии **************************/

    @Test
    public void createRecordVersionTest14() {
        //Успешно только значения
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        provider.updateRecordVersion(logger, 1L, null, null, records);
        verify(refBookDao, never()).updateVersionRelevancePeriod(any(String.class), any(Long.class), any(Date.class));
        verify(refBookDao, times(1)).updateRecordVersion(any(Long.class), any(Long.class), eq(records));
    }

    @Test
    public void createRecordVersionTest15() {
        //Успешно только период
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        when(refBookDao.getRecordData(any(Long.class), any(Long.class))).thenReturn(records);

        Date oldVersionFrom = getDate(1, 1, 2015);
        Date oldVersionTo = getDate(1, 1, 2016);
        Date versionFrom = getDate(1, 3, 2015);
        Date versionTo = getDate(1, 3, 2016);

        //Предыдущий период версии
        RefBookRecordVersion oldVersionPeriod = new RefBookRecordVersion();
        oldVersionPeriod.setVersionStart(oldVersionFrom);
        oldVersionPeriod.setVersionEnd(oldVersionTo);
        oldVersionPeriod.setVersionEndFake(true);
        when(refBookDao.getRecordVersionInfo(any(Long.class))).thenReturn(oldVersionPeriod);
        when(refBookDao.getRelatedVersions(any(List.class))).thenReturn(Arrays.asList(31L));

        provider.updateRecordVersion(logger, 1L, versionFrom, versionTo, records);
        //Значения не изменились
        verify(refBookDao).isVersionUsedInRefBooks(any(Long.class), any(List.class), eq(versionFrom), eq(versionTo), any(Boolean.class), any(List.class));
        verify(refBookDao).updateVersionRelevancePeriod(any(String.class), any(Long.class), eq(versionFrom));
        //Удаление предыдущей даты окончания - нет
        verify(refBookDao, never()).deleteRecordVersions(any(String.class), any(List.class), any(Boolean.class));
        verify(refBookDao, never()).createFakeRecordVersion(any(Long.class), any(Long.class), any(Date.class));
        verify(refBookDao, times(1)).updateRecordVersion(any(Long.class), any(Long.class), eq(records));
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest16() {
        //Отредактирован период - пересечение со следующей версией
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        when(refBookDao.getRecordData(any(Long.class), any(Long.class))).thenReturn(records);

        Date oldVersionFrom = getDate(1, 1, 2015);
        Date oldVersionTo = getDate(1, 1, 2016);
        Date versionFrom = getDate(1, 3, 2015);
        Date versionTo = getDate(1, 3, 2016);

        //Предыдущий период версии
        RefBookRecordVersion oldVersionPeriod = new RefBookRecordVersion();
        oldVersionPeriod.setVersionStart(oldVersionFrom);
        oldVersionPeriod.setVersionEnd(oldVersionTo);
        oldVersionPeriod.setVersionEndFake(true);
        when(refBookDao.getRecordVersionInfo(any(Long.class))).thenReturn(oldVersionPeriod);
        when(refBookDao.isVersionsExist(any(Long.class), any(List.class), any(Date.class))).thenReturn(true);
        //Следующая версия
        RefBookRecordVersion nextVersion = new RefBookRecordVersion();
        nextVersion.setVersionStart(getDate(1, 4, 2015));
        nextVersion.setVersionEnd(getDate(1, 4, 2016));
        nextVersion.setVersionEndFake(true);
        when(refBookDao.getNextVersion(any(Long.class), any(Long.class), any(Date.class))).thenReturn(nextVersion);

        try {
            provider.updateRecordVersion(logger, 1L, versionFrom, versionTo, records);
        } catch (ServiceLoggerException e) {
            assertTrue(e.getMessage().contains("Запись не сохранена, обнаружены фатальные ошибки!"));
            assertTrue(hasError(logger, "Обнаружено пересечение указанного срока актуальности с существующей версией!"));
            throw new TestException();
        }
    }

    @Test(expected = TestException.class)
    public void createRecordVersionTest17() {
        //Отредактирован период - пересечение с предыдущей версией
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        when(refBookDao.getRecordData(any(Long.class), any(Long.class))).thenReturn(records);

        Date oldVersionFrom = getDate(1, 3, 2015);
        Date oldVersionTo = getDate(1, 3, 2016);
        Date versionFrom = getDate(1, 1, 2015);
        Date versionTo = getDate(1, 1, 2016);

        //Предыдущий период версии
        RefBookRecordVersion oldVersionPeriod = new RefBookRecordVersion();
        oldVersionPeriod.setVersionStart(oldVersionFrom);
        oldVersionPeriod.setVersionEnd(oldVersionTo);
        oldVersionPeriod.setVersionEndFake(true);
        when(refBookDao.getRecordVersionInfo(any(Long.class))).thenReturn(oldVersionPeriod);
        when(refBookDao.isVersionsExist(any(Long.class), any(List.class), any(Date.class))).thenReturn(true);
        //Предыдущая версия
        RefBookRecordVersion previousVersion = new RefBookRecordVersion();
        previousVersion.setVersionStart(getDate(1, 2, 2014));
        previousVersion.setVersionEnd(getDate(1, 2, 2015));
        previousVersion.setVersionEndFake(true);
        when(refBookDao.getPreviousVersion(any(Long.class), any(Long.class), any(Date.class))).thenReturn(previousVersion);

        try {
            provider.updateRecordVersion(logger, 1L, versionFrom, versionTo, records);
        } catch (ServiceLoggerException e) {
            assertTrue(e.getMessage().contains("Запись не сохранена, обнаружены фатальные ошибки!"));
            assertTrue(hasError(logger, "Обнаружено пересечение указанного срока актуальности с существующей версией!"));
            throw new TestException();
        }
    }

    @Test
    public void createRecordVersionTest18() {
        //Успешно - дата окончания предыдущей версии удаляется
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        when(refBookDao.getRecordData(any(Long.class), any(Long.class))).thenReturn(records);

        Date oldVersionFrom = getDate(1, 3, 2015);
        Date oldVersionTo = getDate(1, 3, 2016);
        Date versionFrom = getDate(1, 1, 2015);
        Date versionTo = getDate(1, 1, 2016);

        //Предыдущий период версии
        RefBookRecordVersion oldVersionPeriod = new RefBookRecordVersion();
        oldVersionPeriod.setVersionStart(oldVersionFrom);
        oldVersionPeriod.setVersionEnd(oldVersionTo);
        oldVersionPeriod.setVersionEndFake(true);
        when(refBookDao.getRecordVersionInfo(any(Long.class))).thenReturn(oldVersionPeriod);
        when(refBookDao.isVersionsExist(any(Long.class), any(List.class), any(Date.class))).thenReturn(true);
        //Предыдущая версия
        RefBookRecordVersion previousVersion = new RefBookRecordVersion();
        previousVersion.setVersionStart(getDate(1, 1, 2014));
        previousVersion.setVersionEnd(getDate(31, 12, 2014));
        previousVersion.setVersionEndFake(true);
        when(refBookDao.getPreviousVersion(any(Long.class), any(Long.class), any(Date.class))).thenReturn(previousVersion);
        when(refBookDao.findRecord(any(Long.class), any(Long.class), any(Date.class))).thenReturn(123L);

        provider.updateRecordVersion(logger, 1L, versionFrom, versionTo, records);
        //Удаление даты окончания предыдущей версии
        verify(refBookDao).findRecord(any(Long.class), any(Long.class), eq(versionFrom));
        verify(refBookDao).deleteRecordVersions(any(String.class), eq(Arrays.asList(123L)), eq(false));
    }

    @Test
    public void createRecordVersionTest19() {
        //Успешно - создается фиктивная запись для даты окончания
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        when(refBookDao.getRecordData(any(Long.class), any(Long.class))).thenReturn(records);

        Date oldVersionFrom = getDate(1, 1, 2015);
        Date oldVersionTo = null;
        Date versionFrom = getDate(1, 3, 2015);
        Date versionTo = getDate(1, 3, 2016);

        //Предыдущий период версии
        RefBookRecordVersion oldVersionPeriod = new RefBookRecordVersion();
        oldVersionPeriod.setVersionStart(oldVersionFrom);
        oldVersionPeriod.setVersionEnd(oldVersionTo);
        oldVersionPeriod.setVersionEndFake(true);
        when(refBookDao.getRecordVersionInfo(any(Long.class))).thenReturn(oldVersionPeriod);
        //Дата окончания не задана
        when(refBookDao.getRelatedVersions(any(List.class))).thenReturn(new ArrayList<Long>());

        provider.updateRecordVersion(logger, 1L, versionFrom, versionTo, records);
        //Значения не изменились
        verify(refBookDao).isVersionUsedInRefBooks(any(Long.class), any(List.class), eq(versionFrom), eq(versionTo), any(Boolean.class), any(List.class));
        verify(refBookDao).updateVersionRelevancePeriod(any(String.class), any(Long.class), eq(versionFrom));
        //Удаление предыдущей даты окончания - нет
        verify(refBookDao, never()).deleteRecordVersions(any(String.class), any(List.class), any(Boolean.class));
        verify(refBookDao).createFakeRecordVersion(any(Long.class), any(Long.class), eq(SimpleDateUtils.addDayToDate(versionTo, 1)));
        verify(refBookDao, times(1)).updateRecordVersion(any(Long.class), any(Long.class), eq(records));
    }

    @Test
    public void createRecordVersionTest20() {
        //Успешно - удаляется фиктивная запись даты окончания
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();
        records.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test2"));
        when(refBookDao.getRecordData(any(Long.class), any(Long.class))).thenReturn(records);

        Date oldVersionFrom = getDate(1, 1, 2015);
        Date oldVersionTo = getDate(1, 1, 2016);
        Date versionFrom = getDate(1, 3, 2015);
        Date versionTo = null;

        //Предыдущий период версии
        RefBookRecordVersion oldVersionPeriod = new RefBookRecordVersion();
        oldVersionPeriod.setVersionStart(oldVersionFrom);
        oldVersionPeriod.setVersionEnd(oldVersionTo);
        oldVersionPeriod.setVersionEndFake(true);
        when(refBookDao.getRecordVersionInfo(any(Long.class))).thenReturn(oldVersionPeriod);
        //Дата окончания задана
        when(refBookDao.getRelatedVersions(any(List.class))).thenReturn(Arrays.asList(111L));

        provider.updateRecordVersion(logger, 1L, versionFrom, versionTo, records);
        //Значения не изменились
        verify(refBookDao).isVersionUsedInRefBooks(any(Long.class), any(List.class), eq(versionFrom), eq(versionTo), any(Boolean.class), any(List.class));
        verify(refBookDao).updateVersionRelevancePeriod(any(String.class), any(Long.class), eq(versionFrom));
        //Удаление предыдущей даты окончания - да
        verify(refBookDao).deleteRecordVersions(any(String.class), eq(Arrays.asList(111L)), any(Boolean.class));
        verify(refBookDao, never()).createFakeRecordVersion(any(Long.class), any(Long.class), any(Date.class));
        verify(refBookDao, times(1)).updateRecordVersion(any(Long.class), any(Long.class), eq(records));
    }

    private boolean hasError(Logger logger, String text) {
        boolean hasError = false;
        for (LogEntry entry : logger.getEntries()) {
            if (entry.getMessage().contains(text)) {
                hasError = true;
            }
        }
        return hasError;
    }

    private Date getDate(int day, int month, int year) {
        return new GregorianCalendar(year, month - 1, day, 0, 0, 0).getTime();
    }

    private class TestException extends RuntimeException{

    }
}
