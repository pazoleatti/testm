package com.aplana.sbrf.taxaccounting.refbook.tax_benefits_property;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * «Параметры налоговых льгот налога на имущество» (id = 203)
 *
 * @author Emamedova
 */
public class TaxBenefitsPropertyTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(TaxBenefitsPropertyTest.class);
    }

    @Before
    public void mockServices() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        testHelper.setValidDateFrom(new Date());
///Not found reference book with id = 202 при прохождении теста
        RefBook refBook = new RefBook();
        refBook.setId(202L);
        refBook.setName("Классификатор");
        when(testHelper.getRefBookFactory().get(202L)).thenReturn(refBook);

        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(202L);
        when(testHelper.getRefBookFactory().getDataProvider(202L)).thenReturn(provider);
///
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
        value1.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20220L));
        value1.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, null));
        value1.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, null));
        value1.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, null));
        value1.put("PARAM_DESTINATION", new RefBookValue(RefBookAttributeType.NUMBER, null));
        value1.put("ASSETS_CATEGORY", new RefBookValue(RefBookAttributeType.STRING, "ASSETS_CATEGORY"));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 20230L));
        value2.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, null));
        value2.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, null));
        value2.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, null));
        value2.put("PARAM_DESTINATION", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        value2.put("ASSETS_CATEGORY", new RefBookValue(RefBookAttributeType.STRING, null));
        //saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 2012400L));
        value3.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, null));
        //saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        value4.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 2012500L));
        value4.put("REDUCTION_SUM", new RefBookValue(RefBookAttributeType.NUMBER, null));
        value4.put("REDUCTION_PCT", new RefBookValue(RefBookAttributeType.NUMBER, null));
        //saveRecords.add(value4);

        /*
        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        // value1
        Assert.assertEquals("Для налоговой льготы «20220» поле «Основание - статья» является обязательным!", entries.get(i++).getMessage());
        Assert.assertEquals("Для налоговой льготы «20220» поле «Основание - пункт» является обязательным!", entries.get(i++).getMessage());
        Assert.assertEquals("Для налоговой льготы «20220» поле «Основание - подпункт» является обязательным!", entries.get(i++).getMessage());
        Assert.assertEquals("Поле «Категория имущества» должно быть заполнено только в том случае, если поле «Назначение параметра (0 – по средней, 1 – категория, 2 – по кадастровой)» равно значению «1»!", entries.get(i++).getMessage());
        // value2
        Assert.assertEquals("Для налоговой льготы «20230» поле «Основание - статья» является обязательным!", entries.get(i++).getMessage());
        Assert.assertEquals("Для налоговой льготы «20230» поле «Основание - пункт» является обязательным!", entries.get(i++).getMessage());
        Assert.assertEquals("Для налоговой льготы «20230» поле «Основание - подпункт» является обязательным!", entries.get(i++).getMessage());
        Assert.assertEquals("Для назначения параметра (0 – по средней, 1 – категория, 2 – по кадастровой) «1» поле «Категория имущества» является обязательным!", entries.get(i++).getMessage());
        // value3
        Assert.assertEquals("Для налоговой льготы «2012400» поле «Льготная ставка, %» является обязательным!", entries.get(i++).getMessage());
        // value4
        Assert.assertEquals("Для налоговой льготы «2012500» обязателен к заполнению один из атрибутов «Уменьшение суммы исчисленного налога, руб.» или «Уменьшение суммы исчисленного налога, %»!", entries.get(i++).getMessage());

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        */
    }
}
