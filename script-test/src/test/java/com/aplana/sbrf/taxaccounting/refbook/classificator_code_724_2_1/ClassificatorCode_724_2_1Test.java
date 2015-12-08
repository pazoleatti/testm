package com.aplana.sbrf.taxaccounting.refbook.classificator_code_724_2_1;

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
 * «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» (id = 102)
 *
 * @author Emamedova
 */
public class ClassificatorCode_724_2_1Test extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(ClassificatorCode_724_2_1Test.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("BOX_724_2_1", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("BOX_724_2_1", new RefBookValue(RefBookAttributeType.NUMBER, 2));
        saveRecords.add(value2);
        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        System.out.println(entries);
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Атрибут «Графа НФ 724.2.1 (0 – Графа 4; 1 – Графа 5)»: значение некорректно. Должно быть присвоено одно из допустимых значений: \"0\",\"1\".", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
