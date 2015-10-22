package com.aplana.sbrf.taxaccounting.refbook.jur_persons;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * «Юридические лица» (id = 520)
 *
 * @author Stanislav Yasinskiy
 */
public class JurPersonsTest extends RefBookScriptTestBase {

    private static final Long REF_BOOK_ID = 520L;

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(JurPersonsTest.class);
    }

    @Before
    public void mockServices() {
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(REF_BOOK_ID);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_ID)).thenReturn(provider);

        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        String filter = (String) invocation.getArguments()[2];
                        if (filter.contains("INN") && filter.contains("11111")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        }
                        return result;
                    }
                });
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678"));
        value1.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "770708389"));
        value1.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083893"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "12345678901"));
        // заполнен только КПП (без ИНН)
        // неверный патерн КПП
        value2.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "7707083891"));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        // неверный формат свифт
        value3.put("SWIFT", new RefBookValue(RefBookAttributeType.STRING, "1"));
        // заполнен только ИНН (без КПП)
        // неверная контрольная сумма ИНН
        value3.put("INN", new RefBookValue(RefBookAttributeType.STRING, "7707083894"));
        value3.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("01.01.2015")));
        value3.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("02.01.2015")));
        saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        // заполнен только ИНН (без КПП)
        // дубль в спр. по ИНН
        // неверный патерн ИНН
        value4.put("INN", new RefBookValue(RefBookAttributeType.STRING, "11111"));
        value4.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("01.01.2015")));
        value4.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("01.01.2015")));
        saveRecords.add(value4);

        HashMap<String, RefBookValue> value5 = new HashMap<String, RefBookValue>();
        // не заполнен идентификационный код организации
        // дата искл. меньше даты вкл.
        value5.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("02.01.2015")));
        value5.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("01.01.2015")));
        saveRecords.add(value5);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Обязательно должны быть указаны «ИНН» и «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"КПП\" заполнен неверно (7707083891)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «Код Swift» должно содержать 8 или 11 символов!", entries.get(i++).getMessage());
        Assert.assertEquals("Обязательно должны быть указаны «ИНН» и «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("Вычисленное контрольное число по полю \"ИНН\" некорректно (7707083894).", entries.get(i++).getMessage());
        Assert.assertEquals("Обязательно должны быть указаны «ИНН» и «КПП»!", entries.get(i++).getMessage());
        Assert.assertEquals("В справочнике уже существует организация с данным ИНН!", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"ИНН\" заполнен неверно (11111)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals("Обязательно должно быть заполнено одно из следующих полей: «ИНН», «КИО», «Код SWIFT», «Регистрационный номер в стране инкорпорации»!", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «Дата наступления основания для включения в список» должно быть больше или равно полю «Дата наступления основания для исключении из списка»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
