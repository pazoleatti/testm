package com.aplana.sbrf.taxaccounting.refbook.jur_persons;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * «Юридические лица» (id = 520)
 *
 * @author Stanislav Yasinskiy
 */
public class JurPersonsTest extends RefBookScriptTestBase {

    private static final Long REF_BOOK_ID = 520L;
    private static final Long REF_BOOK_ORG_CODE_ID = 513L;
    private static final Long REF_BOOK_TYPE_TCO_ID = 525L;

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(JurPersonsTest.class);
    }

    @Before
    public void mockServices() {
        RefBook refBook = mock(RefBook.class);
        when(refBook.getAttribute(anyString())).thenAnswer(
                new Answer<RefBookAttribute>() {
                    @Override
                    public RefBookAttribute answer(InvocationOnMock invocation) throws Throwable {
                        final String alias = (String) invocation.getArguments()[0];
                        return new RefBookAttribute(){{
                            setName(alias);
                        }};
                    }
                }
        );
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(REF_BOOK_ID);
        RefBookUniversal providerOrgCode = mock(RefBookUniversal.class);
        provider.setRefBookId(REF_BOOK_ORG_CODE_ID);
        RefBookUniversal providerTypeTco = mock(RefBookUniversal.class);
        provider.setRefBookId(REF_BOOK_TYPE_TCO_ID);
        when(testHelper.getRefBookFactory().get(REF_BOOK_ID)).thenReturn(refBook);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_ID)).thenReturn(provider);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_ORG_CODE_ID)).thenReturn(providerOrgCode);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_TYPE_TCO_ID)).thenReturn(providerTypeTco);

        when(provider.getRecordIdPairs(anyLong(), any(Date.class), anyBoolean(), anyString())).thenAnswer(
                new Answer<List<Pair<Long, Long>>>() {
                    @Override
                    public List<Pair<Long, Long>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String) invocation.getArguments()[3];
                        List<Pair<Long, Long>> pairs = new ArrayList<Pair<Long, Long>>();
                        if (filter.contains("INN") && filter.contains("11111")) {
                            pairs.add(new Pair<Long, Long>(1001L, 1001L));
                        } else if (filter.contains("182")) {
                            pairs.add(new Pair<Long, Long>(182L, 182L));
                        }
                        return pairs;
                    }
                });

        when(provider.getRecordVersionInfo(anyLong())).thenAnswer(
                new Answer<RefBookRecordVersion> () {
                    @Override
                    public RefBookRecordVersion answer(InvocationOnMock invocation) throws Throwable {
                        RefBookRecordVersion result = new RefBookRecordVersion();
                        result.setVersionStart((new GregorianCalendar(2012, Calendar.JANUARY, 1, 0, 0, 0)).getTime());
                        return result;
                    }
                });

        when(providerOrgCode.getRecordData(anyLong())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        Long record_id = (Long) invocation.getArguments()[0];
                        Long value = null;
                        if (record_id.equals(262625899L)) {
                            value = 1L;
                        } else if (record_id.equals(262625999L)) {
                            value = 2L;
                        }
                        result.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, value));
                        return result;
                    }
                }
        );

        when(providerTypeTco.getRecordData(anyLong())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        Long record_id = (Long) invocation.getArguments()[0];
                        String value = null;
                        if (record_id.equals(262680699L)) {
                            value = "ВЗЛ";
                        } else if (record_id.equals(262680899L)) {
                            value = "НЛ";
                        } else  if (record_id.equals(262625799L)) {
                            value = "РОЗ";
                        }
                        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, value));
                        return result;
                    }
                }
        );
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "НЛ"
        value1.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value1.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value1.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678"));
        value1.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "770708389"));
        value1.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083893"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "НЛ"
        value2.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value2.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value2.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678901"));
        // заполнен только КПП (без ИНН)
        // неверный патерн КПП
        value2.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "7707083891"));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "НЛ"
        value3.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value3.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        // неверный формат свифт
        value3.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "1"));
        // заполнен только ИНН (без КПП)
        // неверная контрольная сумма ИНН
        value3.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value3.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, null));
        value3.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, null));
        saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "НЛ"
        value4.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value4.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        // заполнен только ИНН (без КПП)
        // дубль в спр. по ИНН
        // неверный патерн ИНН
        value4.put("INN", new RefBookValue(RefBookAttributeType.STRING, "11111"));
        value4.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, null));
        value4.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, null));
        saveRecords.add(value4);

        HashMap<String, RefBookValue> value5 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "ВЗЛ"
        value5.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value5.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680699L));
        // не заполнен идентификационный код организации
        // дата искл. меньше даты вкл.
        value5.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("02.01.2015")));
        value5.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("01.01.2015")));
        value5.put("TAX_STATUS", new RefBookValue(RefBookAttributeType.REFERENCE, 0L));
        saveRecords.add(value5);

        HashMap<String, RefBookValue> value6 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "НЛ"
        value6.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value6.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value6.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678"));
        // заполенны REG_NUM и KIO для российской организации
        value6.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value6.put("KIO", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        saveRecords.add(value6);

        HashMap<String, RefBookValue> value7 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 2, TYPE = "РОЗ"
        value7.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625999L));
        value7.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625799L));
        // не заполнены поля: КИО, Код SWIFT, Регистрационный номер в стране инкорпорации
        // заполнено поле ИНН для иностранной организации
        // не заполнена "Оффшорная зона" для "РОЗ"
        value7.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        saveRecords.add(value7);

        HashMap<String, RefBookValue> value8 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 2, TYPE = "НЛ"
        value8.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625999L));
        value8.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value8.put("TAX_CODE_INCORPORATION", new RefBookValue(RefBookAttributeType.STRING, "7707083893"));
        // заполнено поле ИНН, КПП для иностранной организации
        value8.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value8.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        saveRecords.add(value8);

        HashMap<String, RefBookValue> value9 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 2, TYPE = "НЛ"
        value9.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625999L));
        value9.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value9.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678901"));
        value9.put("TAX_CODE_INCORPORATION", new RefBookValue(RefBookAttributeType.STRING, "1234567890123456"));
        saveRecords.add(value9);

        HashMap<String, RefBookValue> value10 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 1, TYPE = "ВЗЛ"
        value10.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625899L));
        value10.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680699L));
        value10.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value10.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value10.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("02.01.2015")));
        value10.put("VAT_STATUS", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        value10.put("DEP_CRITERION", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        value10.put("TAX_STATUS", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value10);

        HashMap<String, RefBookValue> value11 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 2, TYPE = "НЛ"
        value11.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625999L));
        value11.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value11.put("KIO", new RefBookValue(RefBookAttributeType.STRING, "7707083893"));
        value11.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678"));
        value11.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value11.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("02.01.2015")));
        value11.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("02.01.2016")));
        value11.put("VAT_STATUS", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        value11.put("DEP_CRITERION", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        value11.put("TAX_STATUS", new RefBookValue(RefBookAttributeType.REFERENCE, 0L));
        saveRecords.add(value11);

        HashMap<String, RefBookValue> value12 = new HashMap<String, RefBookValue>();
        // ORG_CODE = 2, TYPE = "НЛ"
        value12.put("ORG_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 262625999L));
        value12.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 262680899L));
        value12.put("KIO", new RefBookValue(RefBookAttributeType.STRING, "182"));
        value12.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "18218218"));
        value12.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING, "182"));
        value12.put("TAX_CODE_INCORPORATION", new RefBookValue(RefBookAttributeType.STRING, "182"));
        value12.put("STATREPORT_ID", new RefBookValue(RefBookAttributeType.STRING, "182"));
        saveRecords.add(value12);

        testHelper.setValidDateFrom((new GregorianCalendar(2012, Calendar.JANUARY, 1, 0, 0, 0)).getTime());
        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // 1
        Assert.assertEquals("7707083893 / 770708389", saveRecords.get(0).get("IKKSR").getStringValue());
        Assert.assertEquals("7707083893", saveRecords.get(0).get("IKSR").getStringValue());
        // 2
        Assert.assertEquals("Для российской организации обязательно должны быть заполнены поля: «ИНН», «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"КПП\" заполнен неверно (7707083891)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «Код Swift» должно содержать 8 или 11 символов!", entries.get(i++).getMessage());
        Assert.assertNull(saveRecords.get(1).get("IKKSR").getStringValue());
        Assert.assertNull(saveRecords.get(1).get("IKSR").getStringValue());
        // 3
        Assert.assertEquals("Для российской организации обязательно должны быть заполнены поля: «ИНН», «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("Вычисленное контрольное число по полю \"ИНН\" некорректно (7707083894).", entries.get(i++).getMessage());
        Assert.assertNull(saveRecords.get(2).get("IKKSR").getStringValue());
        Assert.assertEquals("7707083894", saveRecords.get(2).get("IKSR").getStringValue());
        // 4
        Assert.assertEquals("Для российской организации обязательно должны быть заполнены поля: «ИНН», «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("В справочнике уже существует организация, у которой значение ИНН, КИО, Код SWIFT, Регистрационный номер в стране инкорпорации или Код налогоплательщика в стране инкорпорации совпадает со значением в поле «INN»!", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"ИНН\" заполнен неверно (11111)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9).", entries.get(i++).getMessage());
        Assert.assertNull(saveRecords.get(3).get("IKKSR").getStringValue());
        Assert.assertEquals("11111", saveRecords.get(3).get("IKSR").getStringValue());
        // 5
        Assert.assertEquals("Для российской организации обязательно должны быть заполнены поля: «ИНН», «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «START_DATE» должно быть меньше или равно полю «END_DATE»!", entries.get(i++).getMessage());
        Assert.assertEquals("Для ВЗЛ обязательно должны быть заполнены поля «VAT_STATUS»,«DEP_CRITERION»!", entries.get(i++).getMessage());
        Assert.assertNull(saveRecords.get(4).get("IKKSR").getStringValue());
        Assert.assertNull(saveRecords.get(4).get("IKSR").getStringValue());
        // 6
        Assert.assertEquals("Для российской организации обязательно должны быть заполнены поля: «ИНН», «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("Для российской организации нельзя указать поле «REG_NUM»!", entries.get(i++).getMessage());
        Assert.assertEquals("Для российской организации нельзя указать поле «KIO»!", entries.get(i++).getMessage());
        Assert.assertEquals("Вычисленное контрольное число по полю \"КИО\" некорректно (7707083894).", entries.get(i++).getMessage());
        Assert.assertNull(saveRecords.get(5).get("IKKSR").getStringValue());
        Assert.assertNull(saveRecords.get(5).get("IKSR").getStringValue());
        //7
        Assert.assertEquals("Для иностранной организации обязательно должно быть заполнено одно из следующих полей: «Код налогоплательщика в стране инкорпорации», «Код SWIFT», «Регистрационный номер в стране инкорпорации»!", entries.get(i++).getMessage());
        Assert.assertEquals("Для иностранной организации нельзя указать «INN»!", entries.get(i++).getMessage());
        Assert.assertEquals("Для Резидента оффшорной зоны обязательно должно быть заполнено поле «OFFSHORE_CODE»!", entries.get(i++).getMessage());
        Assert.assertEquals("Вычисленное контрольное число по полю \"ИНН\" некорректно (7707083894).", entries.get(i++).getMessage());
        Assert.assertNull(saveRecords.get(6).get("IKKSR").getStringValue());
        Assert.assertNull(saveRecords.get(6).get("IKSR").getStringValue());
        //8
        Assert.assertEquals("Для иностранной организации нельзя указать «INN»!", entries.get(i++).getMessage());
        Assert.assertEquals("Для иностранной организации нельзя указать «KPP»!", entries.get(i++).getMessage());
        Assert.assertEquals("Вычисленное контрольное число по полю \"ИНН\" некорректно (7707083894).", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"КПП\" заполнен неверно (7707083894)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals("7707083893", saveRecords.get(7).get("IKKSR").getStringValue());
        Assert.assertEquals("7707083893", saveRecords.get(7).get("IKSR").getStringValue());
        //9
        Assert.assertEquals("1234567890123456", saveRecords.get(8).get("IKKSR").getStringValue());
        Assert.assertEquals("1234567890123456", saveRecords.get(8).get("IKSR").getStringValue());
        //10
        Assert.assertEquals("Для российских ВЗЛ обязательно должно быть заполнено поле «TAX_STATUS»!", entries.get(i++).getMessage());
        i++; // некорректное контрольное число ИНН
        i++; // неправильный паттерн КПП
        i++; // расшифровка паттерна КПП
        //11
        Assert.assertEquals("Для иностранной организации нельзя указать «TAX_STATUS»!", entries.get(i++).getMessage());
        String msg = "Для РОЗ и НЛ нельзя указать поле «%s»!";
        Assert.assertEquals(String.format(msg, "START_DATE"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "END_DATE"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "VAT_STATUS"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "DEP_CRITERION"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "TAX_STATUS"), entries.get(i++).getMessage());
        //12
        msg = "В справочнике уже существует организация, у которой значение ИНН, КИО, Код SWIFT, Регистрационный номер в стране инкорпорации или Код налогоплательщика в стране инкорпорации совпадает со значением в поле «%s»!";
        Assert.assertEquals(String.format(msg, "KIO"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "SWIFT"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "REG_NUM"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(msg, "TAX_CODE_INCORPORATION"), entries.get(i++).getMessage());
        Assert.assertEquals("В справочнике уже существует организация с данным значением поля «STATREPORT_ID»!", entries.get(i++).getMessage());
        i++; // некорректный КИО
        i++; // расшифровка паттерна КИО

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

        // Проверка автозаполнения поля 'RS'
        RefBookDataProvider prov = testHelper.getRefBookFactory().getDataProvider(REF_BOOK_ORG_CODE_ID);
        for (Map<String, RefBookValue> rec : saveRecords) {
            Long x = (Long) prov.getRecordData(rec.get("ORG_CODE").getReferenceValue()).get("CODE").getNumberValue();
            String rs = rec.get("RS").getStringValue();
            if (x != 2) {
                Assert.assertNull(rs);
                continue;
            }
            RefBookValue value = rec.get("REG_NUM") != null ? rec.get("REG_NUM") : rec.get("SWIFT");
            if (value != null) {
                Assert.assertEquals(value.getStringValue(), rs);
            } else {
                Assert.assertNull(rs);
            }
        }
    }
}
