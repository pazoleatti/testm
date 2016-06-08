package com.aplana.sbrf.taxaccounting.refbook.vehicles_average_cost_2015;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

/**
 * "Средняя стоимость транспортных средств (с 2015)" (id = 218)
 *
 * @author Bkinzyabulatov
 */
public class VehiclesAverageCost2015Test extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(VehiclesAverageCost2015Test.class);
    }

    @Before
    public void mockServices() {
        PagingResult<Map<String, RefBookValue>> pagingResult = new PagingResult<Map<String, RefBookValue>>();
        pagingResult.addAll(testHelper.getRefBookAllRecords(211L).values());
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), isNull(PagingParams.class), isNull(String.class), isNull(RefBookAttribute.class))).
                thenReturn(pagingResult);
        List<RefBookAttribute> attributeList = new ArrayList<RefBookAttribute>();
        RefBookAttribute attribute = new RefBookAttribute();
        attribute.setAlias("BREND");
        attribute.setAttributeType(RefBookAttributeType.STRING);
        attribute.setMaxLength(120);
        attributeList.add(attribute);
        attribute = new RefBookAttribute();
        attribute.setAlias("MODEL");
        attribute.setAttributeType(RefBookAttributeType.STRING);
        attribute.setMaxLength(120);
        attributeList.add(attribute);
        attribute = new RefBookAttribute();
        attribute.setAlias("ENGINE_VOLUME");
        attribute.setAttributeType(RefBookAttributeType.STRING);
        attribute.setMaxLength(120);
        attributeList.add(attribute);
        attribute = new RefBookAttribute();
        attribute.setAlias("ENGINE_TYPE");
        attribute.setAttributeType(RefBookAttributeType.STRING);
        attribute.setMaxLength(120);
        attributeList.add(attribute);
        attribute = new RefBookAttribute();
        attribute.setAlias("YOM_RANGE");
        attribute.setAttributeType(RefBookAttributeType.STRING);
        attribute.setMaxLength(120);
        attributeList.add(attribute);
        attribute = new RefBookAttribute();
        attribute.setAlias("AVG_COST");
        attribute.setAttributeType(RefBookAttributeType.REFERENCE);
        attributeList.add(attribute);
        RefBook refBook = new RefBook();
        refBook.setAttributes(attributeList);
        when(testHelper.getRefBookFactory().get(eq(218L))).thenReturn(refBook);
    }

    @Test
    public void importTest() {
        testHelper.setImportFileInputStream(getCustomInputStream("import.xml"));
        testHelper.execute(FormDataEvent.IMPORT);
        checkLogger();
    }

    @Test
    public void import2Test() {
        testHelper.setImportFileInputStream(getCustomInputStream("import_2.xml"));
        testHelper.execute(FormDataEvent.IMPORT);
        checkLogger();
    }
}
