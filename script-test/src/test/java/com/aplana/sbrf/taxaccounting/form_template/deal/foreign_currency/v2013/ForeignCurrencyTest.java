package com.aplana.sbrf.taxaccounting.form_template.deal.foreign_currency.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Купля-продажа иностранной валюты
 *
 * @author Levykin
 */
public class ForeignCurrencyTest extends DealBaseTest {
    private static final int TYPE_ID = 390;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}