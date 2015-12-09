package com.aplana.sbrf.taxaccounting.refbook.organization;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;

/**
 * «Организации - участники контролируемых сделок» (id = 9)
 *
 * @author Emamedova
 */
public class OrganizationTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(OrganizationTest.class);
    }

    @Before
    public void mockServices() {
        when(testHelper.getFormDataService().getRefBookValue(anyLong(), anyLong(), anyMap())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        result.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 1));
                        return result;
                    }
                }
        );
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("INN_KIO", new RefBookValue(RefBookAttributeType.STRING, "7736581290"));
        value1.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "773601001"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("INN_KIO", new RefBookValue(RefBookAttributeType.STRING, ""));
        value2.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "773601001"));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("INN_KIO", new RefBookValue(RefBookAttributeType.STRING, "0000000000"));
        value3.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "000000000"));
        saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        value4.put("INN_KIO", new RefBookValue(RefBookAttributeType.STRING, "1234567890"));
        value4.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "773601001"));
        saveRecords.add(value4);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Для организаций РФ атрибут «ИНН» является обязательным", entries.get(i++).getMessage());
        // value3
        Assert.assertEquals("Атрибут \"ИНН\" заполнен неверно (0000000000)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"КПП\" заполнен неверно (000000000)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9).", entries.get(i++).getMessage());
        // value4
        Assert.assertEquals("Вычисленное контрольное число по полю \"ИНН\" некорректно (1234567890).", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
