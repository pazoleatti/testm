package com.aplana.sbrf.taxaccounting.form_template.deal.interbank_credits.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Предоставление межбанковских кредитов
 *
 * @author Levykin
 */
public class InterbankCreditsTest extends DealBaseTest {
    private static final int TYPE_ID = 389;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
