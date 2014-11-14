package com.aplana.sbrf.taxaccounting.form_template.deal.rights_acquisition.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Приобретение прав требования
 *
 * @author Levykin
 */
public class RightsAcquisitionTest extends DealBaseTest {
    private static final int TYPE_ID = 404;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
