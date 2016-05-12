package com.aplana.sbrf.taxaccounting.refbook.vehicles_average_cost_2015;

import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Before;

/**
 * "Средняя стоимость транспортных средств (с 2015)" (id = 218)
 *
 * @author Lhaziev
 */
public class VehiclesAverageCost2015Test extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(VehiclesAverageCost2015Test.class);
    }

    @Before
    public void mockServices() {
    }
}
