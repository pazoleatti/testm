package com.aplana.sbrf.taxaccounting.form_template.deal.take_itf.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Привлечение ИТФ и аккредитивов
 *
 * @author Levykin
 */
public class TakeItfTest extends DealBaseTest {
    private static final int TYPE_ID = 403;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}