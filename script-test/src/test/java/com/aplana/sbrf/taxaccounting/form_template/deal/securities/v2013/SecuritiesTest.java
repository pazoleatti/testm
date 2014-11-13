package com.aplana.sbrf.taxaccounting.form_template.deal.securities.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Приобретение и реализация ценных бумаг (долей в уставном капитале)
 *
 * @author Levykin
 */
public class SecuritiesTest extends DealBaseTest {
    private static final int TYPE_ID = 381;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}