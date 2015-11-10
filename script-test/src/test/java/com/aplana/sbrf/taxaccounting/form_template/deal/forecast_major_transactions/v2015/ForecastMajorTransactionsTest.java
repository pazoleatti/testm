package com.aplana.sbrf.taxaccounting.form_template.deal.forecast_major_transactions.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Прогноз крупных сделок
 *
 * @author LKhaziev
 */
public class ForecastMajorTransactionsTest extends DealBaseTest {
    private static final int TYPE_ID = 810;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    public void importExcelTest() {
        // импорта пока нету
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}