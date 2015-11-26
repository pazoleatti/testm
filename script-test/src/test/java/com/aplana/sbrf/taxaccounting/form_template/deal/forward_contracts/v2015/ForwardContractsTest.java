package com.aplana.sbrf.taxaccounting.form_template.deal.forward_contracts.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Поставочные срочные сделки, базисным активом которых является иностранная валюта
 *
 * @author Levykin
 */
public class ForwardContractsTest extends DealBaseTest {
    private static final int TYPE_ID = 391;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}