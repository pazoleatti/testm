package com.aplana.sbrf.taxaccounting.form_template.deal.corporate_credit.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Предоставление корпоративного кредита
 *
 * @author Levykin
 */
public class CorporateCreditTest extends DealBaseTest {
    private static final int TYPE_ID = 387;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
