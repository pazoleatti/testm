package com.aplana.sbrf.taxaccounting.form_template.deal.bank_service_income.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Оказание услуг (доходы)
 *
 * @author Levykin
 */
public class BankServiceIncomeTest extends DealBaseTest {
    private static final int TYPE_ID = 398;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}