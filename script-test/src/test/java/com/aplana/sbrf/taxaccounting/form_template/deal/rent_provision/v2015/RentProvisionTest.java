package com.aplana.sbrf.taxaccounting.form_template.deal.rent_provision.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Предоставление нежилых помещений в аренду
 *
 * @author Levykin
 */
public class RentProvisionTest extends DealBaseTest {
    private static final int TYPE_ID = 376;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
