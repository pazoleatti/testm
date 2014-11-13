package com.aplana.sbrf.taxaccounting.form_template.deal.nondeliverable.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Беспоставочные срочные сделки
 *
 * @author Levykin
 */
public class NondeliverableTest extends DealBaseTest {
    private static final int TYPE_ID = 392;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
