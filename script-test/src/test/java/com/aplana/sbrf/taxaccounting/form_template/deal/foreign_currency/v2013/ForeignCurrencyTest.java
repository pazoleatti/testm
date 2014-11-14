package com.aplana.sbrf.taxaccounting.form_template.deal.foreign_currency.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Купля-продажа иностранной валюты
 *
 * @author Levykin
 */
public class ForeignCurrencyTest extends DealBaseTest {
    private static final int TYPE_ID = 390;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}