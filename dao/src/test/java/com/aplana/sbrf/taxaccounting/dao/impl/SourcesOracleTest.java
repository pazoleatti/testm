package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.model.*;
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
@ContextConfiguration({"ForOracleTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SourcesOracleTest {

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

    @Autowired
    FormDataDao formDataDao;

    @Autowired
    DeclarationDataDao declarationDataDao;

    @Before
    public void init() {
        String script = "classpath:data/SourcesOracle.sql";
        Resource resource = ctx.getResource(script);
        JdbcTestUtils.executeSqlScript((JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations(), resource, true);
    }


    /*****************************  Получение нф-источников нф ***************************/

    @Test
    public void test1() {
        //2 источника для консолидированной, один из них не создан. Результат: 2 записи
        FormData formData = new FormData();
        formData.setId(1L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals('I', relations.get(1).getTaxType().getCode());
        //Исключаем несозданные
        relations = sourceDao.getSourcesInfo(formData, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getSourcesInfo(formData, true, false, WorkflowState.CREATED);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        //Нф без источников
        formData.setId(2L);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(0, relations.size());
        //Полное получение модели
        formData.setId(1L);
        relations = sourceDao.getSourcesInfo(formData, false, false, null);
        assertEquals(3, relations.size());

        assertEquals("НДС-200", relations.get(0).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());

        assertEquals("РНУ-1", relations.get(1).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(1).getDepartment().getName());
        assertEquals("первый квартал", relations.get(1).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(1).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test55() {
        //2 источника для консолидированной, один из них не создан. Результат: 2 записи + несозданная ндс-200 за полугодие из другого налога
        FormData formData = new FormData();
        formData.setId(200L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"полугодие 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("201, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"полугодие 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"полугодие 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test56() {
        //TODO тут тоже дублирование, потом буду разбираться
        //2 источника для консолидированной, один из них не создан. Результат: 2 записи + созданная ндс-200 за 1 квартал из другого налога
        FormData formData = new FormData();
        formData.setId(202L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        for (Relation relation : relations) {
            System.out.println(getShortFormInfo(relation));
        }
        assertEquals(3, relations.size());
        assertEquals("204, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"1 квартал 2050\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("203, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"1 квартал 2050\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"1 квартал 2050\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test57() {
        //2 источника для консолидированной, один из них не создан. Результат: 2 записи + несозданная ндс-200 за полугодие из другого налога. Периода "полугодие" нет в НДС, поэтому даже созданный экземпляр в 3 квартале не считается
        FormData formData = new FormData();
        formData.setId(205L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"полугодие 2050\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("206, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"полугодие 2050\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"полугодие 2050\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test2() {
        //2 источника для консолидированной, один из них не создан, а у другого не активен макет. Результат: обе записи (у одной статус макета "не активен").
        FormData formData = new FormData();
        formTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 1);
        formData.setId(1L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 1L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(1);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("2, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test3() {
        //2 источника для консолидированной, один из них в другом подразделении. Результат: 2 записи + 1 из предыдущего теста (не создана)
        FormData formData = new FormData();
        formData.setId(3L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("4, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("5, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));

        formDataDao.delete(1, 3L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(3);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("4, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("5, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test4() {
        //3 источника для консолидированной, два из них не созданы, а у другого период действия назначения не пересекается с периодом формы-исходника. Результат: 2 записи (не созданы)
        FormData formData = new FormData();
        formData.setId(6L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 6L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(4);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test5() {
        //TODO этот тест не работает, надо с ним разбираться. Займусь этим когда вернусь из отпуска, пока не обращайте на него внимания
        //4 источника для консолированной, один из них не создан, а 3 ежемесячных (их них 1 не создан). Результат: 4 записи.
        //У ежемесячных форм должен быть указан месяц (даже если не создана) + несозданные ндс (200 без месяца и 201 за январь, февраль, март) + созданная НДС-202 без месяца
        FormData formData = new FormData();
        formData.setId(8L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        for (Relation relation : relations) {
            System.out.println(getShortFormInfo(relation));
        }
        assertEquals(9, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-201\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-201\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-201\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
        assertEquals("208, Тип: \"Первичная\", Вид: \"НДС-202\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(4)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(5)));
        assertEquals("9, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(6)));
        assertEquals("10, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(7)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(8)));

        formDataDao.delete(1, 8L);
        formData.setId(null);
        formData.setFormTemplateId(5);
        formData.setDepartmentReportPeriodId(5);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(9, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-201\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-201\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-201\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
        assertEquals("208, Тип: \"Первичная\", Вид: \"НДС-202\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(4)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(5)));
        assertEquals("9, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(6)));
        assertEquals("10, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(7)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(8)));
    }

    @Test
    public void test6() {
        //2 источника для консолидированной, один из них не создан (ежемесячный),а другой не ежемесячный. Исходная нф создана как ежемесячная. Результат: 2 записи (рну-1 без месяца и несозданная рну-4 за январь) + РНУ-11 из другого теста
        FormData formData = new FormData();
        formData.setId(11L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("12, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-11\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 11L);
        formData.setId(null);
        formData.setFormTemplateId(4);
        formData.setDepartmentReportPeriodId(6);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("12, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-11\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2024\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test7() {
        //2 источника для консолидированной, один из них создан (ежемесячный),а другой не ежемесячный. Исходная нф создана как ежемесячная. Результат: 2 записи (рну-1 без месяца и созданная рну-4 за январь) + РНУ-11 из другого теста
        FormData formData = new FormData();
        formData.setId(13L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("14, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-11\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("15, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 13L);
        formData.setId(null);
        formData.setFormTemplateId(4);
        formData.setDepartmentReportPeriodId(7);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("14, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-11\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("15, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test8() {
        //2 источника для консолидированной, один из них не создан. Исходная нф создана в корректирующем периоде. Результат: 2 записи (для созданной записи отображается корр. период - 08.01.2005, для несозданной корр.период пустой)
        FormData formData = new FormData();
        formData.setId(16L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("17, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"08.01.2005\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 16L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(10);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("17, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"08.01.2005\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test9() {
        //1 источник для консолидированной. Исходная нф создана в корректирующем периоде. Результат: 1 запись (отображается несозданный источник с пустой датой корректировки) + РНУ-2 из предыдущих тестов
        FormData formData = new FormData();
        formData.setId(18L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 18L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(12);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test10() {
        //2 источника для консолидированной с разными датами корректировки. Исходная нф создана в корректирующем периоде. Результат: 1 запись (с датой корректировки - 10.01.2007) + РНУ-2 из предыдущих тестов
        FormData formData = new FormData();
        formData.setId(20L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("21, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 20L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(15);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("21, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test11() {
        //2 источника для консолидированной с разными датами корректировки. Исходная нф создана в обычном периоде. Результат: 1 запись (без даты корректировки) + несозданная РНУ-2 без корректировки
        FormData formData = new FormData();
        formData.setId(23L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("24, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));

        formDataDao.delete(1, 23L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(17);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("24, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test45() {
        //Нф-Источник только в корректирующем периоде. Результат: 2 несозданные нф в обычном периоде
        FormData formData = new FormData();
        formData.setId(223L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 223L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(117);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"НДС-200\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2088\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test12() {
        //1 источник, но с периодом сравнения. Исходная нф создана в обычном периоде. Результат: 1 запись (возвращаются все источники из периода без учета периода сравнения) Не существующий пример.
        FormData formData = new FormData();
        formData.setId(26L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("27, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"первый квартал 2009\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 26L);
        formData.setId(null);
        formData.setFormTemplateId(6);
        formData.setDepartmentReportPeriodId(19);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("27, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"первый квартал 2009\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test13() {
        //1 источник, но с признаком нарастающего итога. Исходная нф создана в обычном периоде, Результат: 1 запись (возвращаются все источники из периода без признака) Не существующий пример
        FormData formData = new FormData();
        formData.setId(28L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("29, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2010\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2010\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 28L);
        formData.setId(null);
        formData.setFormTemplateId(6);
        formData.setDepartmentReportPeriodId(20);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("29, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2010\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test14() {
        //2 источника для консолидированной с разными периодами сравнения. Результат: 1 запись (источник не создан, период сравнения - 3 квартал 2010)
        FormData formData = new FormData();
        formData.setId(30L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2011\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал 2010\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 30L);
        formData.setId(null);
        formData.setFormTemplateId(8);
        formData.setDepartmentReportPeriodId(21);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(20);
        formData.setAccruing(false);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2011\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test15() {
        //2 источника для консолидированной с разными периодами сравнения. Результат: 1 запись (3 квартал 2012)
        FormData formData = new FormData();
        formData.setId(33L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("34, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2012\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2012\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 33L);
        formData.setId(null);
        formData.setFormTemplateId(8);
        formData.setDepartmentReportPeriodId(22);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(22);
        formData.setAccruing(true);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("34, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2012\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2012\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test30() {
        //2 источника, но без периода сравнения. Исходная нф создана с периодом сравнения. Результат: 2 записи - несозданная РНУ-89 без периода сравнения и несозданная РНУ-7 с периодом сравнения
        FormData formData = new FormData();
        formData.setId(133L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2026\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал (9 месяцев) 2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-89\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 133L);
        formData.setId(null);
        formData.setFormTemplateId(88);
        formData.setDepartmentReportPeriodId(122);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(122);
        formData.setAccruing(true);
        relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-7\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2026\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал (9 месяцев) 2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-89\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test52() {
        //2 источника из разных подразделений. Результат: оба источника
        FormData formData = new FormData();
        formData.setId(78L);
        List<Relation> relations = sourceDao.getSourcesInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("79, Тип: \"Первичная\", Вид: \"РНУ-100\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал 2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("80, Тип: \"Первичная\", Вид: \"РНУ-100\", Подразделение: \"Волго-Вятский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал 2026\"", getShortFormInfo(relations.get(1)));
    }


    /*****************************  Получение нф-источников декларации ***************************/

    @Test
    public void test16() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        //2 источника для декларации с разными периодами сравнения. Результат: 1 запись (3 квартал 2012)
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        //Исключаем несозданные
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, WorkflowState.CREATED);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        //Полное получение модели
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, false, false, null);
        assertEquals(3, relations.size());
        assertEquals("РНУ-1", relations.get(0).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test17() {
        //2 источника для декларации, один из них не создан, а у другого не активен макет. Результат: обе записи (у одной статус макета "не активен").
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        formTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 1);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        declarationDataDao.delete(1L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(1);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test18() {
        //2 источника для декларации, один из них в другом подразделении. Результат: 2 записи
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(2L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("3, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("37, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));

        declarationDataDao.delete(2L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(2);
        declarationData.setDepartmentReportPeriodId(3);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("3, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("37, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test19() {
        //2 источника для декларации, один из них не создан, а у другого период действия назначения не пересекается с периодом декларации-исходника. Результат: 1 запись (не созданная) + РНУ-1 создана
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(3L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("6, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        declarationDataDao.delete(3L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(4);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("6, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2003\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test20() {
        //4 источников для декларации, 1 обычный (создан), а 3 ежемесячных (их них 1 не создан). Результат: 5 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(4L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("9, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("10, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(2)));
        assertEquals("8, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(3)));

        declarationDataDao.delete(4L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(3);
        declarationData.setDepartmentReportPeriodId(5);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("9, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("10, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Первичная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(2)));
        assertEquals("8, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test21() {
        //2 источника для декларации, один из них не создан. Исходная декларация, создана в корректирующем периоде. Результат: 2 запись (для созданной записи отображается корр. период - 10.01.2005, для несозданной корр.период пустой)  + РНУ-5 и РНУ-18 из предыдущих тестов
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(5L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("16, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("77, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(1))); //запись из другого теста
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));

        declarationDataDao.delete(5L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(23);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("16, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("77, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(1))); //запись из другого теста
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test22() {
        //1 источник для декларации, Исходная декларации, создана в корректирующем периоде. Результат: 1 запись (отображается несозданный источник с пустой датой корректировки) + РНУ-5 из предыдущих тестов
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(6L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        declarationDataDao.delete(6L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(24);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test23() {
        //2 источника для декларации, с разными датами корректировки. Исходная декларация, создана в корректирующем периоде. Результат: 1 запись (с датой корректировки - 10.01.2007) + РНУ-5 и РНУ-18 из предыдущих тестов
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(7L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("20, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        declarationDataDao.delete(7L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(15);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("20, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test24() {
        //2 источника для декларации, с разными датами корректировки. Исходная декларация, создана в обычном периоде. Результат: 1 запись (без даты корректировки) + РНУ-5 из предыдущих тестов
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(8L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("23, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        declarationDataDao.delete(8L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(17);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("23, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test44() {
        //нф-источник только в корректирующем периоде. Результат: 1 несозданная нф в обычном периоде
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(9L);
        List<Relation> relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("76, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));

        declarationDataDao.delete(9L);
        declarationData.setId(null);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentReportPeriodId(19);
        relations = sourceDao.getDeclarationSourcesInfo(declarationData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("76, Тип: \"Консолидированная\", Вид: \"РНУ-18\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
    }

    /*****************************  Получение нф-приемников НФ ***************************/

    @Test
    public void test25() {
        //3 приемника для первичной, два из них не созданы. Результат: 3 записи
        FormData formData = new FormData();
        formData.setId(2L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(5, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(4)));
        //Исключаем несозданные
        relations = sourceDao.getDestinationsInfo(formData, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getDestinationsInfo(formData, true, false, WorkflowState.CREATED);
        assertEquals(4, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(3)));
        //Полное получение модели
        relations = sourceDao.getDestinationsInfo(formData, false, false, null);
        assertEquals(5, relations.size());
        assertEquals("РНУ-1", relations.get(0).getFormType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test26() {
        //3 приемника для первичной два из них не созданы, а у другого не активен макет.  Результат: обе записи (у одной статус макета "не активен")
        FormData formData = new FormData();
        formData.setId(2L);
        formTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 9);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(5, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Не создана\"", getShortFormInfo(relations.get(4)));

        formDataDao.delete(1, 2L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(1);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(5, relations.size());
        assertEquals("1, Тип: \"Консолидированная\", Вид: \"РНУ-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Утверждена\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Статус: \"Не создана\"", getShortFormInfo(relations.get(4)));
    }

    @Test
    public void test27() {
        //2 приемника для первичной один из них в другом подразделении. Результат: 2 записи
        FormData formData = new FormData();
        formData.setId(41L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("42, Тип: \"Консолидированная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("43, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 41L);
        formData.setId(null);
        formData.setFormTemplateId(10);
        formData.setDepartmentReportPeriodId(3);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("42, Тип: \"Консолидированная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("43, Тип: \"Консолидированная\", Вид: \"РНУ-9\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test28() {
        //1 приемник для первичной. Период действия назначения приемника не пересекается с периодом источника. Результат: пусто
        FormData formData = new FormData();
        formData.setId(7L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(0, relations.size());

        formDataDao.delete(1, 7L);
        formData.setId(null);
        formData.setFormTemplateId(3);
        formData.setDepartmentReportPeriodId(4);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(0, relations.size());
    }

    @Test
    public void test29() {
        //4 приемника для первичной один из них не создан, а 3 ежемесячных (их них 1 не создан). Результат: 4 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        FormData formData = new FormData();
        formData.setId(44L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("45, Тип: \"Консолидированная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("46, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("47, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));

        formDataDao.delete(1, 44L);
        formData.setId(null);
        formData.setFormTemplateId(11);
        formData.setDepartmentReportPeriodId(5);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("45, Тип: \"Консолидированная\", Вид: \"РНУ-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(0)));
        assertEquals("46, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(1)));
        assertEquals("47, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Февраль\"", getShortFormInfo(relations.get(2)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Статус: \"Не создана\", Месяц: \"Март\"", getShortFormInfo(relations.get(3)));
    }

    @Test
    public void test31() {
        //4 приемника для первичной один из них не создан, а 3 ежемесячных (их них 1 не создан). Результат: 4 записи. У ежемесячных форм должен быть указан месяц (даже если не создана)
        FormData formData = new FormData();
        formData.setId(15L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("13, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("48, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 15L);
        formData.setId(null);
        formData.setFormTemplateId(4);
        formData.setDepartmentReportPeriodId(7);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("13, Тип: \"Консолидированная\", Вид: \"РНУ-4\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\", Месяц: \"Январь\"", getShortFormInfo(relations.get(0)));
        assertEquals("48, Тип: \"Консолидированная\", Вид: \"РНУ-5\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2025\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test32() {
        //1 приемник для первичной с совпадающей датой корректировки. Результат: 1 запись + 1 несозданная с максимальной датой корректировки
        FormData formData = new FormData();
        formData.setId(49L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("50, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2005\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 49L);
        formData.setId(null);
        formData.setFormTemplateId(12);
        formData.setDepartmentReportPeriodId(10);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("50, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2005\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2005\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test33() {
        //1 приемник для первичной с не совпадающей датой корректировки. Результат: 1 запись (создана, дата корректировки 15.10.2006) + 1 несозданная с максимальной датой корректировки
        FormData formData = new FormData();
        formData.setId(51L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("52, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 51L);
        formData.setId(null);
        formData.setFormTemplateId(12);
        formData.setDepartmentReportPeriodId(12);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("52, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test34() {
        //1 приемник для первичной с не совпадающей датой корректировки. 2 несозданные с максимальной датой корректировки
        FormData formData = new FormData();
        formData.setId(53L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 34L);
        formData.setId(null);
        formData.setFormTemplateId(12);
        formData.setDepartmentReportPeriodId(15);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2007\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"10.01.2007\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test35() {
        //Назначено 5 приемников, но фактически ими являются только 3 созданных экземпляра (РНУ-13 за 05.01, РНУ-13 за 10.01 и РНУ-33), т.к остальные созданные экземпляры являются приемниками для второго экземпляра РНУ-12.
        //3 созданных экземпляра (РНУ-13 за 05.01, РНУ-13 за 10.01 и РНУ-33). Нф Волго-вятского банка исключается т.к для нее источником является другая нф
        FormData formData = new FormData();
        formData.setId(55L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("57, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("58, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2008\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("61, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(3)));

        formDataDao.delete(1, 55L);
        formData.setId(null);
        formData.setFormTemplateId(12);
        formData.setDepartmentReportPeriodId(17);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(4, relations.size());
        assertEquals("57, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("58, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2008\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\"", getShortFormInfo(relations.get(2)));
        assertEquals("61, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(3)));

        //2 созданных экземпляра (РНУ-13 за 15.01 для Байкальского и Воло-вятского банка)
        formData.setId(56L);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("59, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("60, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 56L);
        formData.setId(null);
        formData.setFormTemplateId(12);
        formData.setDepartmentReportPeriodId(27);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("59, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("60, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test36() {
        //1 приемник, но с периодом сравнения. Исходная нф создана в обычном периоде. Результат: 1 запись (возвращаются все источники из периода без учета периода сравнения)
        FormData formData = new FormData();
        formData.setId(62L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("63, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"первый квартал 2009\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 62L);
        formData.setId(null);
        formData.setFormTemplateId(14);
        formData.setDepartmentReportPeriodId(19);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("63, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"первый квартал 2009\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test37() {
        //1 приемник но с признаком нарастающего итога. Исходная нф создана в обычном периоде/ Результат: 1 запись (возвращаются все приемники из периода без признака)
        FormData formData = new FormData();
        formData.setId(64L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("65, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2010\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2010\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 64L);
        formData.setId(null);
        formData.setFormTemplateId(14);
        formData.setDepartmentReportPeriodId(20);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("65, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2010\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2010\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test38() {
        //2 приемника для консолидированной с разными периодами сравнения. Результат: 1 запись приемник не создан, период сравнения - 1 квартал 2010)
        FormData formData = new FormData();
        formData.setId(66L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2011\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал 2010\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 66L);
        formData.setId(null);
        formData.setFormTemplateId(16);
        formData.setDepartmentReportPeriodId(21);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(20);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2011\", Макет: \"true\", Статус: \"Не создана\", Период сравнения: \"третий квартал 2010\"", getShortFormInfo(relations.get(0)));

    }

    @Test
    public void test39() {
        //2 приемника для консолидированной с разными периодами сравнения. Результат: 1 запись (период сравнения - 3 квартал 2012)
        FormData formData = new FormData();
        formData.setId(69L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("70, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2012\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2012\"", getShortFormInfo(relations.get(0)));

        formDataDao.delete(1, 69L);
        formData.setId(null);
        formData.setFormTemplateId(16);
        formData.setDepartmentReportPeriodId(22);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(22);
        formData.setAccruing(true);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("70, Тип: \"Консолидированная\", Вид: \"РНУ-15\", Подразделение: \"Байкальский банк\", Период: \"третий квартал (9 месяцев) 2012\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал (9 месяцев) 2012\"", getShortFormInfo(relations.get(0)));
    }

    @Test
    public void test40() {
        //3 приемника но без периода сравнения. Исходная нф создана с периодом сравнения. Результат: 2 созданные записи (возвращаются все приемники из периода без учета других полей) + несозданная РНУ-33 + несозданная РНУ-13 в обычном периоде
        FormData formData = new FormData();
        formData.setId(72L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("73, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("74, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2026\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2026\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 72L);
        formData.setId(null);
        formData.setFormTemplateId(17);
        formData.setDepartmentReportPeriodId(122);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(122);
        formData.setAccruing(true);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("73, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"10.01.2026\"", getShortFormInfo(relations.get(0)));
        assertEquals("74, Тип: \"Консолидированная\", Вид: \"РНУ-13\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"15.01.2026\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-33\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2026\"", getShortFormInfo(relations.get(2)));
    }

    @Test
    public void test53() {
        //Результат: РНУ-20 (05.01) и обычная + несозданая РНУ-21 без корр периода
        FormData formData = new FormData();
        formData.setId(124L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("126, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("127, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-21\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(2)));

        formDataDao.delete(1, 124L);
        formData.setId(null);
        formData.setFormTemplateId(19);
        formData.setDepartmentReportPeriodId(17);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("126, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("127, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Создана\"", getShortFormInfo(relations.get(1)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-21\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortFormInfo(relations.get(2)));

        //Результат: несозданная РНУ-20 с макс. Корр. Периодом + несозданная РНУ-21 с макс. Корр. Периодом
        formData.setId(125L);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-21\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(1)));

        formDataDao.delete(1, 125L);
        formData.setId(null);
        formData.setFormTemplateId(19);
        formData.setDepartmentReportPeriodId(18);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-20\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(0)));
        assertEquals("null, Тип: \"Консолидированная\", Вид: \"РНУ-21\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortFormInfo(relations.get(1)));
    }

    @Test
    public void test54() {
        //2 источника из разных подразделений. Результат: оба источника
        FormData formData = new FormData();
        formData.setId(80L);
        List<Relation> relations = sourceDao.getDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("78, Тип: \"Консолидированная\", Вид: \"РНУ-100\", Подразделение: \"Байкальский банк\", Период: \"третий квартал 2026\", Макет: \"true\", Статус: \"Создана\", Период сравнения: \"третий квартал 2026\"", getShortFormInfo(relations.get(0)));
    }

    /*****************************  Получение деклараций-приемников НФ ***************************/

    @Test
    public void test41() {
        //2 приемника для консолидированной, один из них не создан. Результат: 2 записи
        FormData formData = new FormData();
        formData.setId(1L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(1)));

        //Исключаем несозданные
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, true, null);
        assertEquals(1, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        //Исключаем принятые
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, WorkflowState.CREATED);
        assertEquals(1, relations.size());
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(0)));
        //Нф без приемников-деклараций
        formData.setId(2L);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(0, relations.size());
        //Полное получение модели
        formData.setId(1L);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, false, false, null);
        assertEquals(2, relations.size());
        assertEquals("Д-1", relations.get(0).getDeclarationType().getName());
        assertEquals("Байкальский банк", relations.get(0).getDepartment().getName());
        assertEquals("первый квартал", relations.get(0).getDepartmentReportPeriod().getReportPeriod().getName());
        assertEquals(2000, relations.get(0).getDepartmentReportPeriod().getReportPeriod().getTaxPeriod().getYear());
    }

    @Test
    public void test42() {
        //2 приемника для консолидированной, один из них не создан. Результат: 2 записи  (у одной статус макета "не активен").
        FormData formData = new FormData();
        formData.setId(1L);
        declarationTemplateDao.updateVersionStatus(VersionedObjectStatus.DRAFT, 1);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(1)));

        formDataDao.delete(1, 1L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(1);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("1, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"false\", Налоговый орган: \"111\", КПП: \"222\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2000\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test43() {
        //Источник декларации из другого подразделения. Результат: 1 запись
        FormData formData = new FormData();
        formData.setId(37L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("2, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));

        formDataDao.delete(1, 37L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(2);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("2, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2002\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
    }

    @Test
    public void test46() {
        //Один из приемников нф находится в другом подразделении. Результат: 2 записи
        FormData formData = new FormData();
        formData.setId(76L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("9, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("10, Тип: \"Д-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(1)));

        formDataDao.delete(1, 76L);
        formData.setId(null);
        formData.setFormTemplateId(18);
        formData.setDepartmentReportPeriodId(19);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("9, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("10, Тип: \"Д-1\", Подразделение: \"Волго-Вятский банк\", Период: \"первый квартал 2009\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test47() {
        //Период действия назначения источника не пересекается с периодом декларации. Результат: пусто
        FormData formData = new FormData();
        formData.setId(38L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(0, relations.size());

        formDataDao.delete(1, 38L);
        formData.setId(null);
        formData.setFormTemplateId(3);
        formData.setDepartmentReportPeriodId(4);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(0, relations.size());
    }

    @Test
    public void test48() {
        //нф-источник ежемесячная. Результат: 1 запись
        FormData formData = new FormData();
        formData.setId(9L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("4, Тип: \"Д-3\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));

        formDataDao.delete(1, 9L);
        formData.setId(null);
        formData.setFormTemplateId(4);
        formData.setDepartmentReportPeriodId(5);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(1, relations.size());
        assertEquals("4, Тип: \"Д-3\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2004\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
    }

    @Test
    public void test49() {
        //Декларации-приемники с разными датами корректировки. Результат: 2 записи (Д-1 15.01, Д-2 15.01)
        FormData formData = new FormData();
        formData.setId(77L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("5, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"15.01.2005\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("13, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"15.01.2005\"", getShortDeclarationInfo(relations.get(1)));

        formDataDao.delete(1, 77L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(10);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("5, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"15.01.2005\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("13, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2005\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"15.01.2005\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test50() {
        //Декларации-приемники с разными датами корректировки. Результат: 2 несозданные записи (Д-1 15.01, Д-2 15.01)
        FormData formData = new FormData();
        formData.setId(18L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(1)));

        formDataDao.delete(1, 18L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(12);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2006\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2006\"", getShortDeclarationInfo(relations.get(1)));
    }

    @Test
    public void test51() {
        //Д-1 в обычном и корр.(05.01) периодах + несозданная Д-2 в макс. Корр.периоде
        FormData formData = new FormData();
        formData.setId(23L);
        List<Relation> relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("8, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("14, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"05.01.2008\"", getShortDeclarationInfo(relations.get(1)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortDeclarationInfo(relations.get(2)));

        formDataDao.delete(1, 23L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(17);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(3, relations.size());
        assertEquals("8, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("14, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Принята\", Дата сдачи корректировки: \"05.01.2008\"", getShortDeclarationInfo(relations.get(1)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"05.01.2008\"", getShortDeclarationInfo(relations.get(2)));

        //несозданные Д-1 и Д-2 в макс.корр. Периоде
        formData.setId(40L);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
        assertEquals(2, relations.size());
        assertEquals("null, Тип: \"Д-1\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortDeclarationInfo(relations.get(0)));
        assertEquals("null, Тип: \"Д-2\", Подразделение: \"Байкальский банк\", Период: \"первый квартал 2008\", Макет: \"true\", Налоговый орган: \"null\", КПП: \"null\", Статус: \"Не создана\", Дата сдачи корректировки: \"15.01.2008\"", getShortDeclarationInfo(relations.get(1)));

        formDataDao.delete(1, 40L);
        formData.setId(null);
        formData.setFormTemplateId(1);
        formData.setDepartmentReportPeriodId(18);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setComparativePeriodId(null);
        formData.setAccruing(false);
        relations = sourceDao.getDeclarationDestinationsInfo(formData, true, false, null);
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
