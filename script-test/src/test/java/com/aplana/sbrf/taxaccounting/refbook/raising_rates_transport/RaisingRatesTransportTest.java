package com.aplana.sbrf.taxaccounting.refbook.raising_rates_transport;

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
 * «Повышающие коэффициенты транспортного налога» (id = 209)
 *
 * @author Lhaziev
 */
public class RaisingRatesTransportTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(RaisingRatesTransportTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("YEAR_FROM", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        value1.put("YEAR_TO", new RefBookValue(RefBookAttributeType.NUMBER, 10));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("YEAR_FROM", new RefBookValue(RefBookAttributeType.NUMBER, 10));
        value2.put("YEAR_TO", new RefBookValue(RefBookAttributeType.NUMBER, 10));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("YEAR_FROM", new RefBookValue(RefBookAttributeType.NUMBER, 10));
        value3.put("YEAR_TO", new RefBookValue(RefBookAttributeType.NUMBER, 5));
        saveRecords.add(value3);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Поле «Количество лет, прошедших с года выпуска ТС, до» должно быть больше поля «Количество лет, прошедших с года выпуска ТС, от»!", entries.get(i++).getMessage());
        // value3
        Assert.assertEquals("Поле «Количество лет, прошедших с года выпуска ТС, до» должно быть больше поля «Количество лет, прошедших с года выпуска ТС, от»!", entries.get(i++).getMessage());

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
