package com.aplana.sbrf.taxaccounting.form_template.deal.rent_provision.v2013;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;

/**
 * Предоставление нежилых помещений в аренду
 *
 * @author Levykin
 */
public class RentProvisionTest extends DealBaseTest {
    private static final int TYPE_ID = 376;

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
