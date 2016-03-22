package com.aplana.sbrf.taxaccounting.form_template.deal.precious_metals_deliver.v2015;

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
 * Поставочные срочные сделки с драгоценными металлами
 *
 * @author Levykin
 */
public class PreciousMetalsDeliverTest extends DealBaseTest {
    private static final int TYPE_ID = 393;

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//precious_metals_deliver//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // для попадания в ЛП:
        // Корректность заполнения признака внешнеторговой сделки
        // Проверка заполнения сумм доходов и расходов
        row.getCell("name").setValue(1L, null);
        row.getCell("dependence").setValue(1L, null);
        row.getCell("dealType").setValue(1L, null);
        row.getCell("contractNum").setValue("docNum", null);
        row.getCell("contractDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("innerCode").setValue(2, null);
        row.getCell("unitCountryCode").setValue(55, null);
        row.getCell("countryCode2").setValue(55, null);
        row.getCell("signPhis").setValue(55, null);
        row.getCell("signTransaction").setValue(55, null);
        row.getCell("count").setValue(1, null);
        row.getCell("priceOne").setValue(555, null);
        row.getCell("totalNds").setValue(555, null);
        row.getCell("transactionDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("transactionDeliveryDate").setValue(sdf.parse("01.01.2015"), null);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Признак внешнеторговой сделки» не соответствует сведениям о стране отправке и о стране доставки драгоценных металлов!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: В одной из граф «Сумма доходов Банка по данным бухгалтерского учета, руб.», «Сумма расходов Банка по данным бухгалтерского учета, руб.» должно быть указано значение, отличное от «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("signTransaction").setValue(1, null);
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
        Assert.assertEquals(210000, dataRows.get(0).getCell("priceOne").getNumericValue().intValue());
        Assert.assertEquals(95000, dataRows.get(1).getCell("priceOne").getNumericValue().intValue());
        Assert.assertEquals(32000, dataRows.get(2).getCell("priceOne").getNumericValue().intValue());
        Assert.assertEquals(6, dataRows.get(3).getCell("priceOne").getNumericValue().intValue());

        Assert.assertEquals(210000, dataRows.get(0).getCell("totalNds").getNumericValue().intValue());
        Assert.assertEquals(95000, dataRows.get(1).getCell("totalNds").getNumericValue().intValue());
        Assert.assertEquals(32000, dataRows.get(2).getCell("totalNds").getNumericValue().intValue());
        Assert.assertEquals(6, dataRows.get(3).getCell("priceOne").getNumericValue().intValue());
    }

    @Override
    protected int getFormTypeId() {
        return TYPE_ID;
    }
}
