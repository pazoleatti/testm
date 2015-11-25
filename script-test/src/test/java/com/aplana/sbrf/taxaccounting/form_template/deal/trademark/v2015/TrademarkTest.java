package com.aplana.sbrf.taxaccounting.form_template.deal.trademark.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Предоставление права пользования товарным знаком
 *
 * @author Levykin
 */
public class TrademarkTest extends DealBaseTest {
    private static final int TYPE_ID = 379;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}