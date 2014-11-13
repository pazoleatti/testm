package com.aplana.sbrf.taxaccounting.form_template.deal.bonds_trade.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Реализация и приобретение ценных бумаг
 *
 * @author Levykin
 */
public class BondsTradeTest extends DealBaseTest {
    private static final int TYPE_ID = 384;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}