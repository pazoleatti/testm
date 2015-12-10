package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.FormLink;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@Ignore("Включать только локально, со включенным тестом не коммитить!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"../ForOracleTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookOracleTest {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    RefBookDao refBookDao;

    @Before
    public void init() {
        //TODO: для выполнения теста на тестовой бд должен быть заполнен справочник с кодами периодов, т.к каждый раз его заполнять слишком долго. Пример заполнения в data/RefBookOraclePeriodCodes.sql
        String script = "classpath:data/RefBookOracle.sql";
        Resource resource = ctx.getResource(script);
        JdbcTestUtils.executeSqlScript((JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations(), resource, true);
    }

    @Test
    public void getInactiveRecordsInPeriodTest() throws ParseException {
        Map<Long, CheckResult> result = refBookDao.getInactiveRecordsInPeriod(Arrays.asList(18L, 19L, 20L, 22L, 24L, 25L, 28L),
                sdf.parse("01.01.2016"), sdf.parse("31.12.2016"));
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals(null, result.get(18L));
        assertEquals(CheckResult.NOT_CROSS, result.get(19L));
        assertEquals(CheckResult.NOT_CROSS, result.get(20L));
        assertEquals(null, result.get(22L));
        assertEquals(CheckResult.NOT_EXISTS, result.get(24L));
        assertEquals(CheckResult.NOT_LAST, result.get(25L));
        assertEquals(CheckResult.NOT_EXISTS, result.get(28L));

        //Проверяем результат только с несуществующими записями
        result = refBookDao.getInactiveRecordsInPeriod(Arrays.asList(12345L),
                sdf.parse("01.01.2016"), sdf.parse("31.12.2016"));
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals(CheckResult.NOT_EXISTS, result.get(12345L));
    }

    @Test
    public void getRecordsTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        PagingResult<Map<String, RefBookValue>> result = refBookDao.getRecords(13L, getDate(1, 1, 2016), null, null, null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, "запись_18"));
        assertTrue(findRecord(result, "запись_22"));
        assertTrue(findRecord(result, "запись_30"));

        result = refBookDao.getRecords(13L, getDate(1, 1, 2017), null, null, null);
        assertTrue(result.size() == 4);
        assertTrue(findRecord(result, "запись_18"));
        assertTrue(findRecord(result, "запись_19"));
        assertTrue(findRecord(result, "запись_27"));
        assertTrue(findRecord(result, "запись_30"));

        result = refBookDao.getRecords(13L, getDate(1, 1, 2010), null, null, null);
        assertTrue(result.size() == 0);

        result = refBookDao.getRecords(13L, getDate(1, 1, 2016), null, "NAME = 'запись_18'", null);
        assertTrue(result.size() == 1);
        assertTrue(findRecord(result, "запись_18"));

        result = refBookDao.getRecords(13L, getDate(1, 1, 2016), null, "CODE = 123", null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, "запись_18"));
        assertTrue(findRecord(result, "запись_22"));

        result = refBookDao.getRecords(13L, null, null, null, null);
        assertTrue(result.size() == 8);
        assertTrue(findRecord(result, "запись_18"));
        assertTrue(findRecord(result, "запись_19"));
        assertTrue(findRecord(result, "запись_20"));
        assertTrue(findRecord(result, "запись_22"));
        assertTrue(findRecord(result, "запись_25"));
        assertTrue(findRecord(result, "запись_27"));
        assertTrue(findRecord(result, "запись_29"));
        assertTrue(findRecord(result, "запись_30"));
    }

    @Test
    public void getRecordIdPairsTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        List<Pair<Long, Long>> result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2016), false, null);
        assertTrue(result.size() == 3);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));
        assertTrue(result.contains(new Pair<Long, Long>(22L, 14L)));
        assertTrue(result.contains(new Pair<Long, Long>(30L, 17L)));

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2017), false, null);
        assertTrue(result.size() == 4);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));
        assertTrue(result.contains(new Pair<Long, Long>(19L, 12L)));
        assertTrue(result.contains(new Pair<Long, Long>(27L, 16L)));
        assertTrue(result.contains(new Pair<Long, Long>(30L, 17L)));

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2010), false, null);
        assertTrue(result.size() == 0);

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2016), false, "NAME = 'запись_18'");
        assertTrue(result.size() == 1);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2016), false, "CODE = 123");
        assertTrue(result.size() == 2);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));
        assertTrue(result.contains(new Pair<Long, Long>(22L, 14L)));

        result = refBookDao.getRecordIdPairs(13L, null, false, null);
        assertTrue(result.size() == 8);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));
        assertTrue(result.contains(new Pair<Long, Long>(19L, 12L)));
        assertTrue(result.contains(new Pair<Long, Long>(20L, 13L)));
        assertTrue(result.contains(new Pair<Long, Long>(22L, 14L)));
        assertTrue(result.contains(new Pair<Long, Long>(25L, 16L)));
        assertTrue(result.contains(new Pair<Long, Long>(27L, 16L)));
        assertTrue(result.contains(new Pair<Long, Long>(29L, 17L)));
        assertTrue(result.contains(new Pair<Long, Long>(30L, 17L)));

        //точное совпадение даты
        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2015), true, null);
        assertTrue(result.size() == 5);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));
        assertTrue(result.contains(new Pair<Long, Long>(20L, 13L)));
        assertTrue(result.contains(new Pair<Long, Long>(22L, 14L)));
        assertTrue(result.contains(new Pair<Long, Long>(25L, 16L)));
        assertTrue(result.contains(new Pair<Long, Long>(29L, 17L)));

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 4, 2016), true, null);
        assertTrue(result.size() == 1);
        assertTrue(result.contains(new Pair<Long, Long>(27L, 16L)));

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 4, 2018), true, null);
        assertTrue(result.size() == 0);

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2015), true, "NAME = 'запись_18'");
        assertTrue(result.size() == 1);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));

        result = refBookDao.getRecordIdPairs(13L, getDate(1, 1, 2015), true, "CODE = 123");
        assertTrue(result.size() == 2);
        assertTrue(result.contains(new Pair<Long, Long>(18L, 11L)));
        assertTrue(result.contains(new Pair<Long, Long>(22L, 14L)));
    }

    @Test
    public void getNextVersionTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        Date result = refBookDao.getNextVersion(13L, getDate(1, 1, 2015), "CODE = 25");
        assertTrue(result != null);
        assertEquals(getDate(1, 4, 2016), result);

        result = refBookDao.getNextVersion(13L, getDate(1, 1, 2017), "CODE = 25");
        assertTrue(result == null);
    }

    @Test
    public void getRecordDataTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        Map<Long, Map<String, RefBookValue>> result = refBookDao.getRecordData(13L, Arrays.asList(18L, 19L, 24L, 26L));
        assertTrue(result != null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, 18L, "NAME", RefBookAttributeType.STRING, "запись_18"));
        assertTrue(findRecord(result, 18L, "OKP_CODE", RefBookAttributeType.REFERENCE, 1L));
        assertTrue(findRecord(result, 19L, "NAME", RefBookAttributeType.STRING, "запись_19"));
        assertTrue(findRecord(result, 24L, "NAME", RefBookAttributeType.STRING, "запись_24"));
    }

    @Test
    public void isVersionsExistTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        assertTrue(refBookDao.isVersionsExist(13L, Arrays.asList(11L), getDate(1, 1, 2015)));
        assertTrue(refBookDao.isVersionsExist(13L, Arrays.asList(12L), getDate(1, 1, 2017)));
        assertTrue(refBookDao.isVersionsExist(13L, Arrays.asList(13L), getDate(30, 3, 2015)));
        assertFalse(refBookDao.isVersionsExist(13L, Arrays.asList(12L), getDate(1, 1, 2018)));
        assertFalse(refBookDao.isVersionsExist(13L, Arrays.asList(15L), getDate(1, 1, 2015)));
    }

    @Test
    public void getRecordVersionInfoTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        RefBookRecordVersion result = refBookDao.getRecordVersionInfo(18L);
        assertEquals(getDate(1, 1, 2015), result.getVersionStart());
        assertEquals(null, result.getVersionEnd());

        result = refBookDao.getRecordVersionInfo(20L);
        assertEquals(getDate(1, 1, 2015), result.getVersionStart());
        assertEquals(getDate(29, 3, 2015), result.getVersionEnd());

        result = refBookDao.getRecordVersionInfo(25L);
        assertEquals(getDate(1, 1, 2015), result.getVersionStart());
        assertEquals(getDate(27, 2, 2015), result.getVersionEnd());

        result = refBookDao.getRecordVersionInfo(27L);
        assertEquals(getDate(1,4,2016), result.getVersionStart());
        assertEquals(null, result.getVersionEnd());

        result = refBookDao.getRecordVersionInfo(29L);
        assertEquals(getDate(1, 1, 2015), result.getVersionStart());
        assertEquals(getDate(31, 12, 2015), result.getVersionEnd());
    }

    @Test
    public void getRecordVersionsByIdTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        PagingResult<Map<String, RefBookValue>> result = refBookDao.getRecordVersionsById(13L, 18L, null, null, null);
        assertTrue(result.size() == 1);
        assertTrue(findRecord(result, "запись_18"));

        result = refBookDao.getRecordVersionsById(13L, 22L, null, null, null);
        assertTrue(result.size() == 1);
        assertTrue(findRecord(result, "запись_22"));

        result = refBookDao.getRecordVersionsById(13L, 25L, null, null, null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, "запись_25"));
        assertTrue(findRecord(result, "запись_27"));

        result = refBookDao.getRecordVersionsById(13L, 27L, null, null, null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, "запись_25"));
        assertTrue(findRecord(result, "запись_27"));

        result = refBookDao.getRecordVersionsById(13L, 29L, null, null, null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, "запись_29"));
        assertTrue(findRecord(result, "запись_30"));

        result = refBookDao.getRecordVersionsById(13L, 25L, null, "NAME = 'запись_25'", null);
        assertTrue(result.size() == 1);
        assertTrue(findRecord(result, "запись_25"));

        result = refBookDao.getRecordVersionsById(13L, 29L, null, "CODE = 29", null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, "запись_29"));
        assertTrue(findRecord(result, "запись_30"));
    }

    @Test
    public void getMatchedRecordsByUniqueAttributesTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        List<RefBookAttribute> attributes = refBookDao.getAttributes(13L);
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        RefBookRecord record = new RefBookRecord();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        record.setValues(values);
        records.add(record);

        values.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 123));
        List<Pair<Long,String>> result = refBookDao.getMatchedRecordsByUniqueAttributes(13L, null, attributes, records);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, 18L, "Код"));
        assertTrue(findRecord(result, 22L, "Код"));

        result = refBookDao.getMatchedRecordsByUniqueAttributes(13L, 18L, attributes, records);
        assertTrue(result.size() == 1);
        assertTrue(findRecord(result, 22L, "Код"));

        values.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 24));
        result = refBookDao.getMatchedRecordsByUniqueAttributes(13L, null, attributes, records);
        assertTrue(result.size() == 0);
    }

    @Test
    public void checkCrossVersionsTest() {
        //TODO: надо больше тестов по каждому случаю из http://conf.aplana.com/pages/viewpage.action?pageId=11383200
        //Берем данные из getInactiveRecordsInPeriodTest
        List<CheckCrossVersionsResult> result = refBookDao.checkCrossVersions(13L, 16L, getDate(1,1,2014), getDate(25,12,2014), null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, 25L, CrossResult.OK));
        assertTrue(findRecord(result, 27L, CrossResult.OK));
        assertTrue(findRecord(result, 26L, CrossResult.OK));

        result = refBookDao.checkCrossVersions(13L, 16L, getDate(1,1,2014), getDate(25,2,2015), null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, 25L, CrossResult.FATAL_ERROR));
        assertTrue(findRecord(result, 27L, CrossResult.OK));
        assertTrue(findRecord(result, 26L, CrossResult.OK));

        result = refBookDao.checkCrossVersions(13L, 16L, getDate(1,1,2014), getDate(25,3,2015), null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, 25L, CrossResult.FATAL_ERROR));
        assertTrue(findRecord(result, 27L, CrossResult.OK));
        assertTrue(findRecord(result, 26L, CrossResult.NEED_CHANGE));

        result = refBookDao.checkCrossVersions(13L, 16L, getDate(27,2,2015), getDate(25,3,2015), null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, 25L, CrossResult.FATAL_ERROR));
        assertTrue(findRecord(result, 27L, CrossResult.OK));
        assertTrue(findRecord(result, 26L, CrossResult.NEED_CHANGE));

        result = refBookDao.checkCrossVersions(13L, 16L, getDate(1,3,2015), getDate(25,3,2015), null);
        assertTrue(result.size() == 3);
        assertTrue(findRecord(result, 25L, CrossResult.OK));
        assertTrue(findRecord(result, 27L, CrossResult.OK));
        assertTrue(findRecord(result, 26L, CrossResult.OK));

        result = refBookDao.checkCrossVersions(13L, 17L, getDate(1,3,2015), getDate(25,3,2015), null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, 29L, CrossResult.FATAL_ERROR));
        assertTrue(findRecord(result, 30L, CrossResult.OK));

        result = refBookDao.checkCrossVersions(13L, 17L, getDate(1,3,2016), getDate(25,3,2016), null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, 29L, CrossResult.OK));
        assertTrue(findRecord(result, 30L, CrossResult.NEED_CHECK_USAGES));

        result = refBookDao.checkCrossVersions(13L, 13L, getDate(1,2,2015), getDate(1,1,2016), null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, 21L, CrossResult.NEED_DELETE));

        result = refBookDao.checkCrossVersions(13L, 13L, getDate(1,2,2015), null, null);
        assertTrue(result.size() == 2);
        assertTrue(findRecord(result, 21L, CrossResult.NEED_DELETE));
    }

    @Test
    public void checkConflictValuesVersionsTest() {
        //Берем данные из getInactiveRecordsInPeriodTest
        List<Long> result = refBookDao.checkConflictValuesVersions(Arrays.asList(new Pair<Long, String>(25L,"")), getDate(1,1,2014), getDate(31,12,2014));
        assertEquals(0, result.size());

        result = refBookDao.checkConflictValuesVersions(Arrays.asList(new Pair<Long, String>(25L,"")), getDate(1,1,2014), getDate(31,2,2015));
        assertEquals(1, result.size());
        assertTrue(result.contains(25L));

        result = refBookDao.checkConflictValuesVersions(Arrays.asList(new Pair<Long, String>(25L,""), new Pair<Long, String>(19L,"")), getDate(1,1,2014), getDate(31,2,2015));
        assertEquals(1, result.size());
        assertTrue(result.contains(25L));

        result = refBookDao.checkConflictValuesVersions(Arrays.asList(new Pair<Long, String>(25L,"")), getDate(1,1,2016), getDate(31,12,2016));
        assertEquals(0, result.size());

        result = refBookDao.checkConflictValuesVersions(Arrays.asList(new Pair<Long, String>(27L,"")), getDate(1,1,2018), null);
        assertEquals(1, result.size());
        assertTrue(result.contains(27L));
    }

    @Test
    public void isVersionUsedInDepartmentConfigsTest() {
        //Берем данные из getInactiveRecordsInPeriodTest

        /********************* Пересекается ***********************/

        List<String> result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2013), getDate(1,1,2014), true, null);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2015), null, true, null);
        assertEquals(6, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));

        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2017), null, true, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2014), null, true, null);
        assertEquals(6, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,3,2015), getDate(1,5,2015), true, null);
        assertEquals(4, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,3,2016), getDate(1,5,2017), true, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,3,2016), null, true, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));


        /********************* Не пересекается ***********************/

        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2013), getDate(1,1,2014), false, null);
        assertEquals(6, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2015), null, false, null);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2017), null, false, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2014), null, false, null);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,3,2015), getDate(1,5,2015), false, null);
        assertEquals(2, result.size());
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,3,2016), getDate(1,5,2017), false, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));


        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,3,2016), null, false, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));

        /********************* Вообще все независимо от периода ***********************/

        result = refBookDao.isVersionUsedInDepartmentConfigs(37L, Arrays.asList(32L), getDate(1,1,2013), getDate(1,1,2014), null, null);
        assertEquals(6, result.size());
        assertTrue(result.contains("В настройке подразделения \"Байкальский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Открытое акционерное общество \"Сбербанк России\"\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Волго-Вятский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2015\" указана ссылка на версию!"));
        assertTrue(result.contains("В настройке подразделения \"Среднерусский банк\" для налога \"Учета контролируемых сделок\" в периоде \"первый квартал 2016\" указана ссылка на версию!"));
    }

    @Test
    public void isVersionUsedInFormsTest() {

        /********************* Пересекается ***********************/

        List<FormLink> result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2013), getDate(1, 1, 2014), true);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2013), getDate(1, 1, 2015), true);
        assertEquals(1, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2016), getDate(1, 3, 2016), true);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2017), null, true);
        assertEquals(1, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2016), null, true);
        assertEquals(1, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2012), null, true);
        assertEquals(2, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2018), null, true);
        assertEquals(0, result.size());

        /********************* Не пересекается ***********************/

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2013), getDate(1, 1, 2014), false);
        assertEquals(2, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2013), getDate(1, 1, 2015), false);
        assertEquals(1, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2016), getDate(1, 3, 2016), false);
        assertEquals(2, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2017), null, false);
        assertEquals(1, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2016), null, false);
        assertEquals(1, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2012), null, false);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2018), null, false);
        assertEquals(2, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));

        /********************* Вообще все независимо от периода ***********************/

        result = refBookDao.isVersionUsedInForms(35L, Arrays.asList(32L), getDate(1, 1, 2018), null, null);
        assertEquals(2, result.size());
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2017\"."));
        assertTrue(findForm(result, "Существует экземпляр  формы, который содержит ссылку на запись! Тип: \"Первичная\", Вид: \"(РНУ-4) Простой регистр налогового учета \"доходы\"\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2015\"."));
    }

    @Test
    public void isVersionUsedInRefBooksTest() {

        /********************* Пересекается ***********************/

        List<String> result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2010), getDate(1, 1, 2011), true, null);
        assertEquals(0, result.size());

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2016), null, true, null);
        assertEquals(5, result.size());
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по 29-03-2016."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-04-2016 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2016 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"19\", действует с 01-01-2017 по -."));

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2015), getDate(1, 3, 2015), true, null);
        assertEquals(5, result.size());
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"20\", действует с 01-01-2015 по 29-03-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2015 по 31-12-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по 29-03-2016."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-01-2015 по 27-02-2015."));

        /********************* Не пересекается ***********************/

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2010), getDate(1, 1, 2011), false, null);
        assertEquals(8, result.size());
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"20\", действует с 01-01-2015 по 29-03-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2015 по 31-12-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по 29-03-2016."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-04-2016 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2016 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"19\", действует с 01-01-2017 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-01-2015 по 27-02-2015."));

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2016), getDate(1, 1, 2017), false, null);
        assertEquals(3, result.size());
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"20\", действует с 01-01-2015 по 29-03-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2015 по 31-12-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-01-2015 по 27-02-2015."));

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2018), null, false, null);
        assertEquals(4, result.size());
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"20\", действует с 01-01-2015 по 29-03-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2015 по 31-12-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по 29-03-2016."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-01-2015 по 27-02-2015."));

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2010), null, false, null);
        assertEquals(0, result.size());

        /********************* Вообще все независимо от периода ***********************/

        result = refBookDao.isVersionUsedInRefBooks(1L, Arrays.asList(1L), getDate(1, 1, 2018), null, null, null);
        assertEquals(8, result.size());
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"20\", действует с 01-01-2015 по 29-03-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2015 по 31-12-2015."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по 29-03-2016."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-04-2016 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"123\", действует с 01-01-2015 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"29\", действует с 01-01-2016 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"19\", действует с 01-01-2017 по -."));
        assertTrue(result.contains("Существует ссылка на запись справочника. Справочник \"Виды услуг\", запись: \"25\", действует с 01-01-2015 по 27-02-2015."));
    }

    @Test
    public void getNextVersionTest2() {
        RefBookRecordVersion result = refBookDao.getNextVersion(13L, 11L, getDate(1, 1, 2015));
        assertTrue(result == null);

        result = refBookDao.getNextVersion(13L, 13L, getDate(1, 1, 2015));
        assertTrue(result == null);

        result = refBookDao.getNextVersion(13L, 16L, getDate(1, 1, 2015));
        assertTrue(result != null);
        assertEquals(getDate(1,4,2016), result.getVersionStart());

        result = refBookDao.getNextVersion(13L, 16L, getDate(1, 1, 2015));
        assertTrue(result != null);
        assertEquals(getDate(1,4,2016), result.getVersionStart());

        result = refBookDao.getNextVersion(13L, 17L, getDate(1, 1, 2015));
        assertTrue(result != null);
        assertEquals(getDate(1,1,2016), result.getVersionStart());
    }

    @Test
    public void getPreviousVersionTest() {
        RefBookRecordVersion result = refBookDao.getPreviousVersion(13L, 11L, getDate(1, 1, 2015));
        assertTrue(result == null);

        result = refBookDao.getPreviousVersion(13L, 13L, getDate(1, 1, 2015));
        assertTrue(result == null);

        result = refBookDao.getPreviousVersion(13L, 16L, getDate(1, 4, 2016));
        assertTrue(result != null);
        assertEquals(getDate(1,1,2015), result.getVersionStart());

        result = refBookDao.getPreviousVersion(13L, 17L, getDate(1, 1, 2016));
        assertTrue(result != null);
        assertEquals(getDate(1,1,2015), result.getVersionStart());
    }

    @Test
    public void findRecordTest() {
        Long result = refBookDao.findRecord(14L, 11L, getDate(1, 1, 2015));
        assertTrue(result == null);

        result = refBookDao.findRecord(13L, 11L, getDate(1, 1, 2015));
        assertTrue(result == 18L);
    }

    @Test
    public void getRelatedVersionsTest() {
        List<Long> result = refBookDao.getRelatedVersions(Arrays.asList(18L));
        assertTrue(result.isEmpty());

        result = refBookDao.getRelatedVersions(Arrays.asList(20L));
        assertTrue(result.size() == 1);
        assertTrue(result.contains(21L));

        result = refBookDao.getRelatedVersions(Arrays.asList(25L));
        assertTrue(result.size() == 1);
        assertTrue(result.contains(26L));
    }

    @Test
    public void getFirstRecordIdTest() {
        Long result = refBookDao.getFirstRecordId(14L, 11L);
        assertTrue(result == null);

        result = refBookDao.getFirstRecordId(13L, 18L);
        assertTrue(result == null);

        result = refBookDao.getFirstRecordId(13L, 27L);
        assertTrue(result == 25L);
    }

    private boolean findForm(List<FormLink> result, String text) {
        for (FormLink formLink : result){
            if (formLink.getMsg().equals(text)) {
                return true;
            }
        }
        return false;
    }

    private Date getDate(int day, int month, int year) {
        return new GregorianCalendar(year, month - 1, day, 0, 0, 0).getTime();
    }

    boolean findRecord(List<CheckCrossVersionsResult> result, Long id, CrossResult status) {
        for (CheckCrossVersionsResult record : result) {
            if (record.getRecordId().equals(id) && record.getResult() == status) {
                return true;
            }
        }
        return false;
    }

    boolean findRecord(List<Pair<Long,String>> result, Long id, String attributeName) {
        for (Pair<Long,String> record : result) {
            if (record.getFirst().equals(id) && record.getSecond().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    private boolean findRecord(Collection<Map<String, RefBookValue>> records, String name) {
        for (Map<String, RefBookValue> record : records) {
            if (record.get("NAME").getStringValue().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean findRecord(Map<Long, Map<String, RefBookValue>> records, Long id, String attributeName, RefBookAttributeType attributeType, Object attributeValue) {
        for (Map.Entry<Long, Map<String, RefBookValue>> record : records.entrySet()) {
            if (record.getKey().equals(id)){
                switch (attributeType) {
                    case STRING:
                        if (record.getValue().get(attributeName).getStringValue().equals(attributeValue)) {
                            return true;
                        }
                        break;
                    case NUMBER:
                        if (record.getValue().get(attributeName).getDateValue().equals(attributeValue)) {
                            return true;
                        }
                        break;
                    case DATE:
                        if (record.getValue().get(attributeName).getNumberValue().equals(attributeValue)) {
                            return true;
                        }
                        break;
                    case REFERENCE:
                        if (record.getValue().get(attributeName).getReferenceValue().equals(attributeValue)) {
                            return true;
                        }
                        break;
                }
            }
        }
        return false;
    }
}
