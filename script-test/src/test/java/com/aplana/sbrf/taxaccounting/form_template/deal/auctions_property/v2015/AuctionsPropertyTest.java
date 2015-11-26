package com.aplana.sbrf.taxaccounting.form_template.deal.auctions_property.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import org.junit.Assert;

import java.util.List;

/**
 * Приобретение услуг по организации и проведению торгов по реализации имущества
 *
 * @author Levykin
 */
public class AuctionsPropertyTest extends DealBaseTest {
    private static final int TYPE_ID = 380;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(12.0, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(23.0, dataRows.get(1).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(33.0, dataRows.get(2).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12.0, dataRows.get(0).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(23.0, dataRows.get(1).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(33.0, dataRows.get(2).getCell("cost").getNumericValue().doubleValue(), 0);
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
