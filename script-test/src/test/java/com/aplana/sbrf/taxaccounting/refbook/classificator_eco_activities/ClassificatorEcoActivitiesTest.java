package com.aplana.sbrf.taxaccounting.refbook.classificator_eco_activities;

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
 * «Общероссийский классификатор видов экономической деятельности» (id = 34)
 *
 * @author Emamedova
 */
public class ClassificatorEcoActivitiesTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(ClassificatorEcoActivitiesTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "11.11.11"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "11"));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "11.1"));
        saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        value4.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "11.11"));
        saveRecords.add(value4);

        HashMap<String, RefBookValue> value5 = new HashMap<String, RefBookValue>();
        value5.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "11.11.1"));
        saveRecords.add(value5);

        HashMap<String, RefBookValue> value6 = new HashMap<String, RefBookValue>();
        value6.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "1"));
        saveRecords.add(value6);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Атрибут \"Код\" заполнен неверно (1)! Ожидаемый паттерн: \"[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}\" / \"[0-9]{2}\" / \"[0-9]{2}\\.[0-9]{1}\" / \"[0-9]{2}\\.[0-9]{2}\" / \"[0-9]{2}\\.[0-9]{2}\\.[0-9]{1}\"", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
