package com.aplana.sbrf.taxaccounting.refbook.vehicles_tax_rate;

import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

/**
 * "Ставки транспортного налога" (id = 41)
 */
public class VehiclesTaxRateTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(VehiclesTaxRateTest.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void save() throws ParseException {
        // TODO
    }

    @Test
    public void addRow() {
        // TODO
    }
}
