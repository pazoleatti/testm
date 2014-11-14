package com.aplana.sbrf.taxaccounting.form_template.deal.take_interbank_credit.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Привлечение средств на межбанковском рынке
 *
 * @author Levykin
 */
public class TakeInterbankCreditTest extends DealBaseTest {
    private static final int TYPE_ID = 402;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}