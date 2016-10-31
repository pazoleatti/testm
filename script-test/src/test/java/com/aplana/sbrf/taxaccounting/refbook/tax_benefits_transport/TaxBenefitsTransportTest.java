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
        HashMap<String, RefBookValue> value = getRecord(20200L, "1234", "1234", "1234", 1L, 1L, null);
        saveRecords.add(value);
        testHelper.setSaveRecords(saveRecords);
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        // 1. Проверка корректности заполнения уменьшающего процента
        value.get("PERCENT").setValue(-1L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Уменьшающий процент, %» должно быть больше 0 и меньше 100", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals(null, value.get("BASE").getStringValue());
        testHelper.getLogger().clear();
        value.get("PERCENT").setValue(0L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Уменьшающий процент, %» должно быть больше 0 и меньше 100", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("PERCENT").setValue(100L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Уменьшающий процент, %» должно быть больше 0 и меньше 100", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("PERCENT").setValue(101L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Уменьшающий процент, %» должно быть больше 0 и меньше 100", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка корректности заполнения пониженной ставки
        value.get("PERCENT").setValue(99L);
        value.get("RATE").setValue(0L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Пониженная ставка» должно быть больше 0", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка корректности заполнения кода налоговой льготы
        value.get("PERCENT").setValue(99L);
        value.get("RATE").setValue(1L);
        value.get("SECTION").setValue(null);
        value.get("ITEM").setValue(null);
        value.get("SUBITEM").setValue(null);
        value.get("TAX_BENEFIT_ID").setValue(40200L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Код налоговой льготы» не должно содержать кода налогового вычета", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("TAX_BENEFIT_ID").setValue(40L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значение поля «Код налоговой льготы» не должно содержать кода налогового вычета", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("TAX_BENEFIT_ID").setValue(1L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals(0, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка корректности заполнения основания
        // 4а
        value.get("SECTION").setValue("1234");
        value.get("ITEM").setValue("1234");
        value.get("SUBITEM").setValue("1234");
        value.get("TAX_BENEFIT_ID").setValue(20201L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значения полей: «Основание - статья», «Основание - пункт», «Основание - подпункт» для выбранного кода льготы не заполняются", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("SECTION").setValue("1234");
        value.get("ITEM").setValue("1234");
        value.get("SUBITEM").setValue(null);
        value.get("TAX_BENEFIT_ID").setValue(20201L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значения полей: «Основание - статья», «Основание - пункт» для выбранного кода льготы не заполняются", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("SECTION").setValue(null);
        value.get("ITEM").setValue(null);
        value.get("SUBITEM").setValue("1234");
        value.get("TAX_BENEFIT_ID").setValue(20201L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значения полей: «Основание - подпункт» для выбранного кода льготы не заполняются", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        // 4б
        value.get("SECTION").setValue(null);
        value.get("ITEM").setValue("");
        value.get("SUBITEM").setValue(null);
        value.get("TAX_BENEFIT_ID").setValue(20230L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значения полей: «Основание - статья», «Основание - пункт», «Основание - подпункт» для выбранного кода льготы должны быть заполнены", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();
        value.get("SECTION").setValue("123-");
        value.get("ITEM").setValue("");
        value.get("SUBITEM").setValue("4");
        value.get("TAX_BENEFIT_ID").setValue(20230L);
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals("Значения полей: «Основание - пункт» для выбранного кода льготы должны быть заполнены", entries.get(0).getMessage());
        Assert.assertEquals(1, entries.size());
        testHelper.getLogger().clear();

        // Для прохождения всех проверок
        value.get("ITEM").setValue("3333");
        testHelper.execute(FormDataEvent.SAVE);
        Assert.assertEquals(0, entries.size());
        Assert.assertEquals("123-33330004", value.get("BASE").getStringValue());
        testHelper.getLogger().clear();
    }

    HashMap<String, RefBookValue> getRecord (Long taxBenefitId, String section, String item, String subitem, Long percent, Long rate, String base) {
        HashMap<String, RefBookValue> value = new HashMap<String, RefBookValue>();
        value.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, taxBenefitId));
        value.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, section));
        value.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, item));
        value.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, subitem));
        value.put("PERCENT", new RefBookValue(RefBookAttributeType.NUMBER, percent));
        value.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, rate));
        value.put("BASE", new RefBookValue(RefBookAttributeType.STRING, base));
        return value;
    }
}
