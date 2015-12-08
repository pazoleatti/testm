package com.aplana.sbrf.taxaccounting.refbook.declaration_params_transport;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Параметры представления деклараций по транспортному налогу" (id = 210)
 *
 * @author Emamedova
 */
public class DeclarationParamsTransportTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(DeclarationParamsTransportTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "7750"));
        value1.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "775001001"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "aaaa"));
        value2.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "000000000"));
        saveRecords.add(value2);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Атрибут \"Код налогового органа\" заполнен неверно (000000000)! Ожидаемый паттерн: \"[0-9]{4}\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «[0-9]{4}»: Все 4 символа: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"КПП\" заполнен неверно (000000000)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"", entries.get(i++).getMessage());
        Assert.assertEquals("Расшифровка паттерна «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»: Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9).", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
