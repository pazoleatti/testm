package com.aplana.sbrf.taxaccounting.refbook.offshore;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
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
import java.util.*;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(10L);
        when(testHelper.getRefBookFactory().getDataProvider(10L)).thenReturn(provider);

        when(provider.getRecordData(anyLong())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        Long record_id = (Long) invocation.getArguments()[0];
                        result.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NAME_" + record_id));
                        result.put("FULLNAME", new RefBookValue(RefBookAttributeType.STRING, "FULLNAME_" + record_id));
                        return result;
                    }
                }
        );
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        value1.put("CODE_2", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value1.put("CODE_3", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value1.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value1.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value1.put("OFFSHORE_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 101L));
        value1.put("OFFSHORE_NAME", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
        value2.put("CODE_2", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value2.put("CODE_3", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value2.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value2.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value2.put("OFFSHORE_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value2.put("OFFSHORE_NAME", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value3.put("CODE_2", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value3.put("CODE_3", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        value3.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value3.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value3.put("OFFSHORE_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 103L));
        value3.put("OFFSHORE_NAME", new RefBookValue(RefBookAttributeType.REFERENCE, null));
        saveRecords.add(value3);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        // value1
        Assert.assertEquals(new Long(1L), saveRecords.get(0).get("CODE_2").getReferenceValue());
        Assert.assertEquals(new Long(1L), saveRecords.get(0).get("CODE_3").getReferenceValue());
        Assert.assertEquals("NAME_1", saveRecords.get(0).get("SHORTNAME").getStringValue());
        Assert.assertEquals("FULLNAME_1", saveRecords.get(0).get("NAME").getStringValue());
        Assert.assertEquals(new Long(101L), saveRecords.get(0).get("OFFSHORE_NAME").getReferenceValue());
        // value2
        Assert.assertEquals(new Long(2L), saveRecords.get(1).get("CODE_2").getReferenceValue());
        Assert.assertEquals(new Long(2L), saveRecords.get(1).get("CODE_3").getReferenceValue());
        Assert.assertEquals("NAME_2", saveRecords.get(1).get("SHORTNAME").getStringValue());
        Assert.assertEquals("FULLNAME_2", saveRecords.get(1).get("NAME").getStringValue());
        Assert.assertNull(saveRecords.get(1).get("OFFSHORE_NAME").getReferenceValue());
        // value3
        Assert.assertNull(saveRecords.get(2).get("CODE_2").getReferenceValue());
        Assert.assertNull(saveRecords.get(2).get("CODE_3").getReferenceValue());
        Assert.assertEquals("NAME", saveRecords.get(2).get("SHORTNAME").getStringValue());
        Assert.assertEquals("NAME", saveRecords.get(2).get("NAME").getStringValue());
        Assert.assertEquals(new Long(103L), saveRecords.get(2).get("OFFSHORE_NAME").getReferenceValue());

        Assert.assertEquals(0, testHelper.getLogger().getEntries().size());
    }
}
