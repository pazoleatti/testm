package com.aplana.sbrf.taxaccounting.form_template.deal.precious_metals_trade.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Купля-продажа драгоценных металлов
 *
 * @author Levykin
 */
public class PreciousMetalsTradeTest extends DealBaseTest {
    private static final int TYPE_ID = 394;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
