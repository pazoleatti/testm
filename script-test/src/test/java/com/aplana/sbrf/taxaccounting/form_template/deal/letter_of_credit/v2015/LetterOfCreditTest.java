package com.aplana.sbrf.taxaccounting.form_template.deal.letter_of_credit.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Предоставление инструментов торгового финансирования и непокрытых аккредитивов
 *
 * @author Levykin
 */
public class LetterOfCreditTest extends DealBaseTest {
    private static final int TYPE_ID = 386;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // TODO Проверить правильность расчетов
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
