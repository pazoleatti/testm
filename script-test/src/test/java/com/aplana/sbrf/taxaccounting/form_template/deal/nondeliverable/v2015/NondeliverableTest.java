package com.aplana.sbrf.taxaccounting.form_template.deal.nondeliverable.v2015;

import com.aplana.sbrf.taxaccounting.form_template.deal.DealBaseTest;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Беспоставочные срочные сделки.
 */
public class NondeliverableTest extends DealBaseTest {
    private static final int TYPE_ID = 392;

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//nondeliverable//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // для попадания в ЛП:
        // Проверка заполнения сумм доходов и расходов
        row.getCell("name").setValue(1L, null);
        row.getCell("contractNum").setValue("docNum", null);
        row.getCell("contractDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("transactionType").setValue("transactionType", null);
        row.getCell("price").setValue(555, null);
        row.getCell("cost").setValue(555, null);
        row.getCell("transactionDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("transactionDeliveryDate").setValue(sdf.parse("01.01.2015"), null);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: В одной из граф «Сумма доходов Банка по данным бухгалтерского учета, руб.», «Сумма расходов Банка по данным бухгалтерского учета, руб.» должно быть указано значение, отличное от «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("incomeSum").setValue(1, null);
        row.getCell("consumptionSum").setValue(556, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    @Override
    protected void calcCheckAfterImport(List<DataRow<Cell>> dataRows) {
        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(25000, dataRows.get(2).getCell("price").getNumericValue().intValue());
        Assert.assertEquals(23000, dataRows.get(3).getCell("price").getNumericValue().intValue());
        Assert.assertEquals(956, dataRows.get(0).getCell("price").getNumericValue().intValue());
        Assert.assertEquals(2355, dataRows.get(1).getCell("price").getNumericValue().intValue());

        Assert.assertEquals(25000, dataRows.get(2).getCell("cost").getNumericValue().intValue());
        Assert.assertEquals(23000, dataRows.get(3).getCell("cost").getNumericValue().intValue());
        Assert.assertEquals(956, dataRows.get(0).getCell("cost").getNumericValue().intValue());
        Assert.assertEquals(2355, dataRows.get(1).getCell("cost").getNumericValue().intValue());
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
