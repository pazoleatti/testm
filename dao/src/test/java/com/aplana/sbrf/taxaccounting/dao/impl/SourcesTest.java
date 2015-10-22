package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.model.Months;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
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

import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore("Включать только локально, со включенным тестом не коммитить!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "SourcesTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SourcesTest {

    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    SourceDao sourceDao;

    @Autowired
    FormTemplateDao formTemplateDao;

    @Autowired
    DeclarationTemplateDao declarationTemplateDao;

    @Before
    public void init() {
        String script = "classpath:data/Sources.sql";
        Resource resource = ctx.getResource(script);
        JdbcTestUtils.executeSqlScript((JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations(), resource, true);
    }


    /*****************************  Получение нф-источников нф ***************************/

    @Test
    public void test1() {
        //2 источника для консолидированной, один из них не создан. Результат: 2 записи
        List<Relation> relations = sourceDao.getSourcesInfo(1, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        //Исключаем несозданные
        relations = sourceDao.getSourcesInfo(1, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getSourcesInfo(1, true, false, WorkflowState.CREATED);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        //Нф без источников
        relations = sourceDao.getSourcesInfo(2, true, false, null);
        assertEquals(0, relations.size());
        //Полное получение модели
        relations = sourceDao.getSourcesInfo(1, false, false, null);
        assertEquals(2, relations.size());
        assertEquals("РНУ-1", relations.get(0).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test2() {
        //2 источника для консолидированной, один из них не создан, а у другого не активен макет. Результат: обе записи (у одной статус макета "не активен").
        formTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 1);
        List<Relation> relations = sourceDao.getSourcesInfo(1, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test3() {
        //2 источника для консолидированной, один из них в другом подразделении. Результат: 2 записи + 1 из предыдущего теста (не создана)
        List<Relation> relations = sourceDao.getSourcesInfo(3, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("4, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("5, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test4() {
        //3 источника для консолидированной, два из них не созданы, а у другого период действия назначения не пересекается с периодом формы-исходника. Результат: 2 записи (не созданы)
        List<Relation> relations = sourceDao.getSourcesInfo(6, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test5() {
        //4 источника для консолированной, один из них не создан, а 3 ежемесячных (их них 1 не создан). Результат: 4 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        List<Relation> relations = sourceDao.getSourcesInfo(8, true, false, null);
        for (Relation relation : relations) {
            System.out.println(getShortFormInfo(relation));
        }
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("9, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("10, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test6() {
        //2 источника для консолидированной, один из них не создан (ежемесячный),а другой не ежемесячный. Исходная нф создана как ежемесячная. Результат: 2 записи (рну-1 без месяца и несозданная рну-4 за январь) + РНУ-11 из другого теста
        List<Relation> relations = sourceDao.getSourcesInfo(11, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("12, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-11\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test7() {
        //2 источника для консолидированной, один из них создан (ежемесячный),а другой не ежемесячный. Исходная нф создана как ежемесячная. Результат: 2 записи (рну-1 без месяца и созданная рну-4 за январь) + РНУ-11 из другого теста
        List<Relation> relations = sourceDao.getSourcesInfo(13, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("14, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-11\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("15, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test8() {
        //2 источника для консолидированной, один из них не создан. Исходная нф создана в корректирующем периоде. Результат: 2 записи (для созданной записи отображается корр. период - 08.01.2005, для несозданной корр.период пустой)
        List<Relation> relations = sourceDao.getSourcesInfo(16, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("17, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"08.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test9() {
        //1 источник для консолидированной. Исходная нф создана в корректирующем периоде. Результат: 1 запись (отображается несозданный источник с пустой датой корректировки) + РНУ-2 из предыдущих тестов
        List<Relation> relations = sourceDao.getSourcesInfo(18, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test10() {
        //2 источника для консолидированной с разными датами корректировки. Исходная нф создана в корректирующем периоде. Результат: 1 запись (с датой корректировки - 10.01.2007) + РНУ-2 из предыдущих тестов
        List<Relation> relations = sourceDao.getSourcesInfo(20, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("21, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test11() {
        //2 источника для консолидированной с разными датами корректировки. Исходная нф создана в обычном периоде. Результат: 1 запись (без даты корректировки) + несозданная РНУ-2 без корректировки
        List<Relation> relations = sourceDao.getSourcesInfo(23, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("24, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test45() {
        //Нф-Источник только в корректирующем периоде. Результат: 2 несозданные нф в обычном периоде
        List<Relation> relations = sourceDao.getSourcesInfo(223, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test12() {
        //1 источник, но с периодом сравнения. Исходная нф создана в обычном периоде. Результат: 1 запись (возвращаются все источники из периода без учета периода сравнения) Не существующий пример.
        List<Relation> relations = sourceDao.getSourcesInfo(26, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("27, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"первый квартал 2009\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test13() {
        //1 источник, но с признаком нарастающего итога. Исходная нф создана в обычном периоде, Результат: 1 запись (возвращаются все источники из периода без признака) Не существующий пример
        List<Relation> relations = sourceDao.getSourcesInfo(28, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("29, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2010\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test14() {
        //2 источника для консолидированной с разными периодами сравнения. Результат: 1 запись (источник не создан, период сравнения - 3 квартал 2010)
        List<Relation> relations = sourceDao.getSourcesInfo(30, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2011\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test15() {
        //2 источника для консолидированной с разными периодами сравнения. Результат: 1 запись (3 квартал 2012)
        List<Relation> relations = sourceDao.getSourcesInfo(33, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("34, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2012\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2012\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test30() {
        //2 источника, но без периода сравнения. Исходная нф создана с периодом сравнения. Результат: 2 записи - несозданная РНУ-89 без периода сравнения и несозданная РНУ-7 с периодом сравнения
        List<Relation> relations = sourceDao.getSourcesInfo(133, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал (9 месяцев) 2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-89\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }


    /*****************************  Получение нф-источников декларации ***************************/

    @Test
    public void test16() {
        //2 источника для декларации с разными периодами сравнения. Результат: 1 запись (3 квартал 2012)
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(1, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        //Исключаем несозданные
        relations = sourceDao.getDeclarationSourcesInfo(1, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getDeclarationSourcesInfo(1, true, false, WorkflowState.CREATED);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        //Полное получение модели
        relations = sourceDao.getDeclarationSourcesInfo(1, false, false, null);
        assertEquals(3, relations.size());
        assertEquals("РНУ-1", relations.get(0).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test17() {
        //2 источника для декларации, один из них не создан, а у другого не активен макет. Результат: обе записи (у одной статус макета "не активен").
        formTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 1);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(1, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test18() {
        //2 источника для декларации, один из них в другом подразделении. Результат: 2 записи
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(2, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("3, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("37, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test19() {
        //2 источника для декларации, один из них не создан, а у другого период действия назначения не пересекается с периодом декларации-исходника. Результат: 1 запись (не созданная) + РНУ-1 создана
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(3, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("6, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test20() {
        //4 источников для декларации, 1 обычный (создан), а 3 ежемесячных (их них 1 не создан). Результат: 5 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(4, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("9, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("10, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(2)));
        assertEquals("8, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test21() {
        //2 источника для декларации, один из них не создан. Исходная декларация, создана в корректирующем периоде. Результат: 2 запись (для созданной записи отображается корр. период - 10.01.2005, для несозданной корр.период пустой)  + РНУ-5 и РНУ-18 из предыдущих тестов
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(5, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("16, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("77, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(1))); //запись из другого теста
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test22() {
        //1 источник для декларации, Исходная декларации, создана в корректирующем периоде. Результат: 1 запись (отображается несозданный источник с пустой датой корректировки) + РНУ-5 из предыдущих тестов
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(6, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test23() {
        //2 источника для декларации, с разными датами корректировки. Исходная декларация, создана в корректирующем периоде. Результат: 1 запись (с датой корректировки - 10.01.2007) + РНУ-5 и РНУ-18 из предыдущих тестов
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(7, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("20, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test24() {
        //2 источника для декларации, с разными датами корректировки. Исходная декларация, создана в обычном периоде. Результат: 1 запись (без даты корректировки) + РНУ-5 из предыдущих тестов
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(8, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("23, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test44() {
        //нф-источник только в корректирующем периоде. Результат: 1 несозданная нф в обычном периоде
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(9, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("76, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    /*****************************  Получение нф-приемников НФ ***************************/

    @Test
    public void test25() {
        //3 приемника для первичной, два из них не созданы. Результат: 3 записи
        List<Relation> relations = sourceDao.getDestinationsInfo(2, true, false, null);
        assertEquals(5, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(3)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(4)));
        //Исключаем несозданные
        relations = sourceDao.getDestinationsInfo(2, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getDestinationsInfo(2, true, false, WorkflowState.CREATED);
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));
        //Полное получение модели
        relations = sourceDao.getDestinationsInfo(2, false, false, null);
        assertEquals(5, relations.size());
        assertEquals("РНУ-1", relations.get(0).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test26() {
        //3 приемника для первичной два из них не созданы, а у другого не активен макет.  Результат: обе записи (у одной статус макета "не активен")
        formTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 9);
        List<Relation> relations = sourceDao.getDestinationsInfo(2, true, false, null);
        assertEquals(5, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(3)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Не создана\"", getShortFormInfo(relations.get(4)));
    }

    @Test
    public void test27() {
        //2 приемника для первичной один из них в другом подразделении. Результат: 2 записи
        List<Relation> relations = sourceDao.getDestinationsInfo(41, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("42, Тип: \"Консолидированная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("43, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test28() {
        //1 приемник для первичной. Период действия назначения приемника не пересекается с периодом источника. Результат: пусто
        List<Relation> relations = sourceDao.getDestinationsInfo(7, true, false, null);
        assertEquals(0, relations.size());
    }

    @Test
    public void test29() {
        //4 приемника для первичной один из них не создан, а 3 ежемесячных (их них 1 не создан). Результат: 4 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        List<Relation> relations = sourceDao.getDestinationsInfo(44, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("45, Тип: \"Консолидированная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("46, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("47, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test31() {
        //4 приемника для первичной один из них не создан, а 3 ежемесячных (их них 1 не создан). Результат: 4 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        List<Relation> relations = sourceDao.getDestinationsInfo(15, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("13, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("48, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test32() {
        //1 приемник для первичной с совпадающей датой корректировки. Результат: 1 запись + 1 несозданная с максимальной датой корректировки
        List<Relation> relations = sourceDao.getDestinationsInfo(49, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("50, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2005\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test33() {
        //1 приемник для первичной с не совпадающей датой корректировки. Результат: 1 запись (создана, дата корректировки 15.10.2006) + 1 несозданная с максимальной датой корректировки
        List<Relation> relations = sourceDao.getDestinationsInfo(51, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("52, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test34() {
        //1 приемник для первичной с не совпадающей датой корректировки. 2 несозданные с максимальной датой корректировки
        List<Relation> relations = sourceDao.getDestinationsInfo(53, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test35() {
        //Назначено 5 приемников, но фактически ими являются только 3 созданных экземпляра (РНУ-13 за 05.01, РНУ-13 за 10.01 и РНУ-33), т.к остальные созданные экземпляры являются приемниками для второго экземпляра РНУ-12.
        //3 созданных экземпляра (РНУ-13 за 05.01, РНУ-13 за 10.01 и РНУ-33). Нф Волго-вятского банка исключается т.к для нее источником является другая нф
        List<Relation> relations = sourceDao.getDestinationsInfo(55, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("57, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("58, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2008\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("61, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(3)));

        //2 созданных экземпляра (РНУ-13 за 15.01 для Байкальского и Воло-вятского банка)
        relations = sourceDao.getDestinationsInfo(56, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("59, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("60, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test36() {
        //1 приемник, но с периодом сравнения. Исходная нф создана в обычном периоде. Результат: 1 запись (возвращаются все источники из периода без учета периода сравнения)
        List<Relation> relations = sourceDao.getDestinationsInfo(62, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("63, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"первый квартал 2009\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test37() {
        //1 приемник но с признаком нарастающего итога. Исходная нф создана в обычном периоде/ Результат: 1 запись (возвращаются все приемники из периода без признака)
        List<Relation> relations = sourceDao.getDestinationsInfo(64, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("65, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2010\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test38() {
        //2 приемника для консолидированной с разными периодами сравнения. Результат: 1 запись приемник не создан, период сравнения - 1 квартал 2010)
        List<Relation> relations = sourceDao.getDestinationsInfo(66, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2011\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test39() {
        //2 приемника для консолидированной с разными периодами сравнения. Результат: 1 запись (период сравнения - 3 квартал 2012)
        List<Relation> relations = sourceDao.getDestinationsInfo(69, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("70, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2012\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2012\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test40() {
        //3 приемника но без периода сравнения. Исходная нф создана с периодом сравнения. Результат: 2 созданные записи (возвращаются все приемники из периода без учета других полей) + несозданная РНУ-33 + несозданная РНУ-13 в обычном периоде
        List<Relation> relations = sourceDao.getDestinationsInfo(72, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("73, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("74, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2026\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2026\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test53() {
        //Результат: РНУ-20 (05.01) и обычная + несозданая РНУ-21 без корр периода
        List<Relation> relations = sourceDao.getDestinationsInfo(124, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("126, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("127, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-21\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(2)));

        //Результат: несозданная РНУ-20 с макс. Корр. Периодом + несозданная РНУ-21 с макс. Корр. Периодом
        relations = sourceDao.getDestinationsInfo(125, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-21\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(1)));
    }

    /*****************************  Получение деклараций-приемников НФ ***************************/

    @Test
    public void test41() {
        //2 приемника для консолидированной, один из них не создан. Результат: 2 записи
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(1, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(1)));

        //Исключаем несозданные
        relations = sourceDao.getDeclarationDestinationsInfo(1, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getDeclarationDestinationsInfo(1, true, false, WorkflowState.CREATED);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(0)));
        //Нф без приемников-деклараций
        relations = sourceDao.getDeclarationDestinationsInfo(2, true, false, null);
        assertEquals(0, relations.size());
        //Полное получение модели
        relations = sourceDao.getDeclarationDestinationsInfo(1, false, false, null);
        assertEquals(2, relations.size());
        assertEquals("Д-1", relations.get(0).getDeclarationType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test42() {
        //2 приемника для консолидированной, один из них не создан. Результат: 2 записи  (у одной статус макета "не активен").
        declarationTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 1);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(1, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test43() {
        //Источник декларации из другого подразделения. Результат: 1 запись
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(37, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("2, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
    }

    @Test
    public void test46() {
        //Один из приемников нф находится в другом подразделении. Результат: 2 записи
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(76, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("9, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("10, Тип: \"Д-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test47() {
        //Период действия назначения источника не пересекается с периодом декларации. Результат: пусто
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(38, true, false, null);
        assertEquals(0, relations.size());
    }

    @Test
    public void test48() {
        //нф-источник ежемесячная. Результат: 1 запись
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(9, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("4, Тип: \"Д-3\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
    }

    @Test
    public void test49() {
        //Декларации-приемники с разными датами корректировки. Результат: 2 записи (Д-1 15.01, Д-2 15.01)
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(77, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("5, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"15.01.2005\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("13, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"15.01.2005\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test50() {
        //Декларации-приемники с разными датами корректировки. Результат: 2 несозданные записи (Д-1 15.01, Д-2 15.01)
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(18, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test51() {
        //Декларации-приемники с разными датами корректировки. Результат: 2 несозданные записи (Д-1 15.01, Д-2 15.01)
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(18, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test52() {
        //Д-1 в обычном и корр.(05.01) периодах + несозданная Д-2 в макс. Корр.периоде
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(23, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("8, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("14, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"05.01.2008\"", getShortDeclarationInfo(relations.get(1)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortDeclarationInfo(relations.get(2)));
        //несозданные Д-1 и Д-2 в макс.корр. Периоде
        relations = sourceDao.getDeclarationDestinationsInfo(40, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortDeclarationInfo(relations.get(1)));
    }


    public String getShortFormInfo(Relation relation) {
        StringBuilder info = new StringBuilder();
        info.append(relation.getFormDataId())
                .append(", Тип: \"").append(relation.getFormDataKind().getTitle())
                .append("\", Вид: \"").append(relation.getFormTypeName())
                .append("\", Подразделение: \"").append(relation.getFullDepartmentName())
                .append("\", Период: \"").append(relation.getPeriodName()).append(" ").append(relation.getYear()).append("\"")
                .append(", Макет: \"").append(relation.isStatus()).append("\"")
                .append(", Статус: \"").append(relation.getState().getTitle()).append("\"");
        if (relation.getMonth() != null) {
            info.append(", Месяц: \"").append(Months.fromId(relation.getMonth()).getTitle()).append("\"");
        }
        if (relation.getCorrectionDate() != null) {
            info.append(", Дата сдачи корректировки: \"").append(df.format(relation.getCorrectionDate())).append("\"");
        }
        if (relation.getComparativePeriodName() != null) {
            info.append(", Период сравнения: \"").append(relation.getComparativePeriodName()).append(" ").append(relation.getComparativePeriodYear()).append("\"");
        }
        return info.toString();
    }

    public String getShortDeclarationInfo(Relation relation) {
        StringBuilder info = new StringBuilder();
        info.append(relation.getDeclarationDataId())
                .append(", Тип: \"").append(relation.getDeclarationTypeName())
                .append("\", Подразделение: \"").append(relation.getFullDepartmentName())
                .append("\", Период: \"").append(relation.getPeriodName()).append(" ").append(relation.getYear()).append("\"")
                .append(", Макет: \"").append(relation.isStatus()).append("\"")
                .append(", Налоговый орган: \"").append(relation.getTaxOrganCode()).append("\"")
                .append(", КПП: \"").append(relation.getKpp()).append("\"")
                .append(", Статус: \"").append(relation.getState().getTitle()).append("\"");
        if (relation.getMonth() != null) {
            info.append(", Месяц: \"").append(Months.fromId(relation.getMonth()).getTitle()).append("\"");
        }
        if (relation.getCorrectionDate() != null) {
            info.append(", Дата сдачи корректировки: \"").append(df.format(relation.getCorrectionDate())).append("\"");
        }
        return info.toString();
    }
    /*for (Relation relation : relations) {
        System.out.println(getShortFormInfo(relation));
    }*/
}
