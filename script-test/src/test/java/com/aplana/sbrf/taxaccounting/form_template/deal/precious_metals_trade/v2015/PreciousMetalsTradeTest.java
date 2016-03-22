package com.aplana.sbrf.taxaccounting.form_template.deal.precious_metals_trade.v2015;

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
 * Купля-продажа драгоценных металлов
 *
 * @author Levykin
 */
public class PreciousMetalsTradeTest extends DealBaseTest {
    private static final int TYPE_ID = 394;

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//precious_metals_trade//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // для попадания в ЛП:
        // Проверка заполнения сумм доходов и расходов
        row.getCell("fullName").setValue(1L, null);
        row.getCell("interdependence").setValue(1L, null);
        row.getCell("docNumber").setValue("docNum", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("dealFocus").setValue(2, null);
        row.getCell("deliverySign").setValue(2, null);
        row.getCell("metalName").setValue(2, null);
        row.getCell("foreignDeal").setValue(2, null);
        row.getCell("count").setValue(1, null);
        row.getCell("incomeSum").setValue(0, null);
        row.getCell("outcomeSum").setValue(0, null);
        row.getCell("price").setValue(555, null);
        row.getCell("total").setValue(555, null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2015"), null);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: В одной из граф «Сумма доходов Банка по данным бухгалтерского учета, руб.», «Сумма расходов Банка по данным бухгалтерского учета, руб.» должно быть указано значение, отличное от «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("incomeSum").setValue(1, null);
        row.getCell("outcomeSum").setValue(0, null);
        i = 0;
        testHelper.execute(FormDataEvent.CALCULATE);
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
        Assert.assertEquals(100, dataRows.get(0).getCell("price").getNumericValue().intValue());
        Assert.assertEquals(560333, dataRows.get(1).getCell("price").getNumericValue().intValue());
        Assert.assertEquals(555, dataRows.get(2).getCell("price").getNumericValue().intValue());
        Assert.assertEquals(32999, dataRows.get(3).getCell("price").getNumericValue().intValue());

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(dataRows.get(i).getCell("price").getNumericValue(), dataRows.get(i).getCell("total").getNumericValue());
        }
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
