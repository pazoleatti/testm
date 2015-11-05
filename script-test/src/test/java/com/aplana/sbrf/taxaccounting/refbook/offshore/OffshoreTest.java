package com.aplana.sbrf.taxaccounting.refbook.offshore;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

/**
 * «Оффшорные зоны» (id = 519)
 *
 * @author Lhaziev
 */
public class OffshoreTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(OffshoreTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        value1.put("CODE_2", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value1.put("CODE_3", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value1.put("OFFSHORE_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 101L));
        value1.put("OFFSHORE_NAME", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
        value2.put("CODE_2", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value2.put("CODE_3", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value2.put("OFFSHORE_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value2.put("OFFSHORE_NAME", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value3.put("CODE_2", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value3.put("CODE_3", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value3.put("OFFSHORE_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 103L));
        value3.put("OFFSHORE_NAME", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value3);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        // value1
        Assert.assertEquals(new Long(1L), saveRecords.get(0).get("CODE_2").getReferenceValue());
        Assert.assertEquals(new Long(1L), saveRecords.get(0).get("CODE_3").getReferenceValue());
        Assert.assertEquals(new Long(101L), saveRecords.get(0).get("OFFSHORE_NAME").getReferenceValue());
        // value2
        Assert.assertEquals(new Long(2L), saveRecords.get(1).get("CODE_2").getReferenceValue());
        Assert.assertEquals(new Long(2L), saveRecords.get(1).get("CODE_3").getReferenceValue());
        Assert.assertNull(saveRecords.get(1).get("OFFSHORE_NAME").getReferenceValue());
        // value3
        Assert.assertNull(saveRecords.get(2).get("CODE_2").getReferenceValue());
        Assert.assertNull(saveRecords.get(2).get("CODE_3").getReferenceValue());
        Assert.assertEquals(new Long(103L), saveRecords.get(2).get("OFFSHORE_NAME").getReferenceValue());

        Assert.assertEquals(0, testHelper.getLogger().getEntries().size());
    }
}
