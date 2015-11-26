package com.aplana.sbrf.taxaccounting.form_template.deal.software_development.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий
 *
 * @author Levykin
 */
public class SoftwareDevelopmentTest extends DealBaseTest {
    private static final int TYPE_ID = 375;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}