package com.aplana.sbrf.taxaccounting.form_template.deal.take_itf.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Привлечение ИТФ и аккредитивов
 *
 * @author Levykin
 */
public class TakeItfTest extends DealBaseTest {
    private static final int TYPE_ID = 403;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}