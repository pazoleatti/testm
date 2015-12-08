package com.aplana.sbrf.taxaccounting.refbook.bond;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * «Ценные бумаги» (id = 84)
 *
 * @author Emamedova
 */
public class BondTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(BondTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("12.12.2015")));
        value1.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("13.12.2015")));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("12.12.2015")));
        value2.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, sdf.parse("11.12.2015")));
        saveRecords.add(value2);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Поле «Дата окончания действия» должно быть больше или равно полю «Дата начала действия»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
