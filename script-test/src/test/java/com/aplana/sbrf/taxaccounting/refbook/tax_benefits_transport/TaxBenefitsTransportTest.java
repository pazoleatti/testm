package com.aplana.sbrf.taxaccounting.refbook.tax_benefits_transport;

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
 * «Параметры налоговых льгот транспортного налога» (id = 7)
 *
 * @author Lhaziev
 */
public class TaxBenefitsTransportTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(TaxBenefitsTransportTest.class);
    }

    @Before
    public void mockServices() {
        when(testHelper.getFormDataService().getRefBookValue(anyLong(), anyLong(), anyMap())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        Long record_id = (Long) invocation.getArguments()[1];
                        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, record_id.toString()));
                        return result;
                    }
                }
        );
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20200L));
        value1.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value1.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value1.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value1.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, null));
        value1.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20210L));
        value2.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value2.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, null));
        value2.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value2.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        value2.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, null));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20220L));
        value3.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value3.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value3.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value3.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        value3.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        value4.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20220L));
        value4.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value4.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value4.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value4.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, null));
        value4.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value4);

        HashMap<String, RefBookValue> value5 = new HashMap<String, RefBookValue>();
        value5.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20230L));
        value5.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value5.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value5.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value5.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        value5.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value5);

        HashMap<String, RefBookValue> value6 = new HashMap<String, RefBookValue>();
        value6.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20230L));
        value6.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value6.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value6.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        value6.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        value6.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, null));
        saveRecords.add(value6);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        // value2
        Assert.assertEquals("Для налоговой льготы «20210» поле «Основание - пункт» является обязательным!", entries.get(i++).getMessage());
        // value3
        // value4
        Assert.assertEquals("Для налоговой льготы «20220» поле «Уменьшающий процент, %» является обязательным!", entries.get(i++).getMessage());
        // value5
        // value6
        Assert.assertEquals("Для налоговой льготы «20230» поле «Пониженная ставка» является обязательным!", entries.get(i++).getMessage());

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
