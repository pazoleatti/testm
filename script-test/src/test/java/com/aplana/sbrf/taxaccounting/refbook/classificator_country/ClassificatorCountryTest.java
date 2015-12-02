package com.aplana.sbrf.taxaccounting.refbook.classificator_country;

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
 * «ОК 025-2001 (Общероссийский классификатор стран мира)» (id = 10)
 *
 * @author Emamedova
 */
public class ClassificatorCountryTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(ClassificatorCountryTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "123"));
        value1.put("CODE_2", new RefBookValue(RefBookAttributeType.STRING, "a1"));
        value1.put("CODE_3", new RefBookValue(RefBookAttributeType.STRING, "bb1"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "1234"));
        value2.put("CODE_2", new RefBookValue(RefBookAttributeType.STRING, "aa1"));
        value2.put("CODE_3", new RefBookValue(RefBookAttributeType.STRING, "bbb1"));
        saveRecords.add(value2);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        System.out.println(entries);
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Атрибут \"Код\" заполнен неверно (1234)! Ожидаемый паттерн: \"[0-9]{3}\"", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «Код (2-х букв.)» должно содержать 2 символа.", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «Код (3-х букв.)» должно содержать 3 символа.", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
