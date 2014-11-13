package com.aplana.sbrf.taxaccounting.form_template.deal.forward_contracts.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Поставочные срочные сделки, базисным активом которых является иностранная валюта
 *
 * @author Levykin
 */
public class ForwardContractsTest extends DealBaseTest {
    private static final int TYPE_ID = 391;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}