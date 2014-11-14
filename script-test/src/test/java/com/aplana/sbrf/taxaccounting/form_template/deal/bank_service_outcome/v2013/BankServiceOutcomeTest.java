package com.aplana.sbrf.taxaccounting.form_template.deal.bank_service_outcome.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Оказание услуг (расходы)
 *
 * @author Levykin
 */
public class BankServiceOutcomeTest extends DealBaseTest {
    private static final int TYPE_ID = 399;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}