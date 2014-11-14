package com.aplana.sbrf.taxaccounting.form_template.deal.nondeliverable.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Беспоставочные срочные сделки
 *
 * @author Levykin
 */
public class NondeliverableTest extends DealBaseTest {
    private static final int TYPE_ID = 392;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
