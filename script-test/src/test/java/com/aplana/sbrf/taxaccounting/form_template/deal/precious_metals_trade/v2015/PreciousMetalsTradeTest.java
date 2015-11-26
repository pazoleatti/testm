package com.aplana.sbrf.taxaccounting.form_template.deal.precious_metals_trade.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Купля-продажа драгоценных металлов
 *
 * @author Levykin
 */
public class PreciousMetalsTradeTest extends DealBaseTest {
    private static final int TYPE_ID = 394;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
