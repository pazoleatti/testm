package com.aplana.sbrf.taxaccounting.form_template.deal.bank_service.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import org.junit.Assert;

import java.util.List;

/**
 * Оказание банковских услуг
 *
 * @author Levykin
 */
public class BankServiceTest extends DealBaseTest {
    private static final int TYPE_ID = 382;

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(32000.0, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(32584.0, dataRows.get(1).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(100000.0, dataRows.get(2).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(32000.0, dataRows.get(0).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(32584.0, dataRows.get(1).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(100000.0, dataRows.get(2).getCell("cost").getNumericValue().doubleValue(), 0);
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}