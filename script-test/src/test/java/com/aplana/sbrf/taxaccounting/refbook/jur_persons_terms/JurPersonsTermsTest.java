package com.aplana.sbrf.taxaccounting.refbook.jur_persons_terms;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * «Правила назначения категории юридическому лицу» (id = 515)
 *
 * @author Stanislav Yasinskiy
 */
public class JurPersonsTermsTest extends RefBookScriptTestBase {

    private static final Long REF_BOOK_ID = 515L;

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(JurPersonsTermsTest.class);
    }

    @Before
    public void mockServices() {
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(REF_BOOK_ID);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_ID)).thenReturn(provider);

        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        return result;
                    }
                });
    }

    @Test
    public void save() throws ParseException {
        ArrayList<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();

        HashMap<String, RefBookValue> value1 = new HashMap<String, RefBookValue>();
        value1.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
        value1.put("MIN_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        value1.put("MAX_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        saveRecords.add(value1);

        HashMap<String, RefBookValue> value2 = new HashMap<String, RefBookValue>();
        value2.put("MIN_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value2);

        HashMap<String, RefBookValue> value3 = new HashMap<String, RefBookValue>();
        value3.put("MAX_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        saveRecords.add(value3);

        HashMap<String, RefBookValue> value4 = new HashMap<String, RefBookValue>();
        saveRecords.add(value4);

        HashMap<String, RefBookValue> value5 = new HashMap<String, RefBookValue>();
        value5.put("MIN_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        value5.put("MAX_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        saveRecords.add(value5);

        HashMap<String, RefBookValue> value6 = new HashMap<String, RefBookValue>();
        value6.put("MIN_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 2));
        value6.put("MAX_VALUE", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        saveRecords.add(value6);

        testHelper.setSaveRecords(saveRecords);

        testHelper.execute(FormDataEvent.SAVE);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Поле «Максимальный объем доходов и расходов» должно быть больше либо равно полю «Минимальный объем доходов и расходов»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }
}
