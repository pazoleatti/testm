package com.aplana.sbrf.taxaccounting.form_template.deal.credit_contract.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Уступка прав требования по кредитным договорам
 *
 * @author Levykin
 */
public class CreditContractTest extends DealBaseTest {
    private static final int TYPE_ID = 385;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}