package com.aplana.sbrf.taxaccounting.form_template.deal.interbank_credits.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Предоставление межбанковских кредитов
 *
 * @author Levykin
 */
public class InterbankCreditsTest extends DealBaseTest {
    private static final int TYPE_ID = 389;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
