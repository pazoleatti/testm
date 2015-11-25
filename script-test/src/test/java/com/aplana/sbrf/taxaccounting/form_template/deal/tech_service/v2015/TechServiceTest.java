package com.aplana.sbrf.taxaccounting.form_template.deal.tech_service.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Техническое обслуживание нежилых помещений
 *
 * @author Levykin
 */
public class TechServiceTest extends DealBaseTest {
    private static final int TYPE_ID = 377;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}