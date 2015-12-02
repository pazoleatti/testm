package com.aplana.sbrf.taxaccounting.refbook.region;

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
 * " Коды субъектов Российской Федерации" (id = 4)
 *
 * @author Emamedova
 */
public class RegionTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(RegionTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("OKATO_DEFINITION", new RefBookValue(RefBookAttributeType.STRING, "0"));
        value1.put("OKTMO_DEFINITION", new RefBookValue(RefBookAttributeType.STRING, "0"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value1.put("OKATO_DEFINITION", new RefBookValue(RefBookAttributeType.STRING, "qq"));
        value1.put("OKTMO_DEFINITION", new RefBookValue(RefBookAttributeType.STRING, "ww"));
        saveRecords.add(value2);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Атрибут \"Определяющая часть кода ОКАТО\" заполнен неверно (qq)! Значение должно содержать только цифры!", entries.get(i++).getMessage());
        Assert.assertEquals("Атрибут \"Определяющая часть кода ОКТМО\" заполнен неверно (ww)! Значение должно содержать только цифры!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
