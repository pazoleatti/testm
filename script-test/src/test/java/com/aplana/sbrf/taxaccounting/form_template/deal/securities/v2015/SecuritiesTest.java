package com.aplana.sbrf.taxaccounting.form_template.deal.securities.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Приобретение и реализация ценных бумаг (долей в уставном капитале)
 *
 * @author Levykin
 */
public class SecuritiesTest extends DealBaseTest {
    private static final int TYPE_ID = 381;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}