package com.aplana.sbrf.taxaccounting.form_template.deal.repo.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Сделки РЕПО
 *
 * @author Levykin
 */
public class RepoTest extends DealBaseTest {
    private static final int TYPE_ID = 383;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
