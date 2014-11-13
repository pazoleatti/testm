package com.aplana.sbrf.taxaccounting.form_template.deal.take_interbank_credit.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Привлечение средств на межбанковском рынке
 *
 * @author Levykin
 */
public class TakeInterbankCreditTest extends DealBaseTest {
    private static final int TYPE_ID = 402;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}