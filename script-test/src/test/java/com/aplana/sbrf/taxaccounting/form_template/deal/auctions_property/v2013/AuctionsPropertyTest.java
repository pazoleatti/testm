package com.aplana.sbrf.taxaccounting.form_template.deal.auctions_property.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Приобретение услуг по организации и проведению торгов по реализации имущества
 *
 * @author Levykin
 */
public class AuctionsPropertyTest extends DealBaseTest {
    private static final int TYPE_ID = 380;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
