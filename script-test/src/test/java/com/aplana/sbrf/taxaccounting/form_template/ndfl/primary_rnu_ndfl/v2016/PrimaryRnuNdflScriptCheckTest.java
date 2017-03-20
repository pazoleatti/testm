package com.aplana.sbrf.taxaccounting.form_template.ndfl.primary_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Тестирование проверок
 */
public class PrimaryRnuNdflScriptCheckTest extends DeclarationScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(PrimaryRnuNdflScriptCheckTest.class);
    }

    @Override
    protected DeclarationData getDeclarationData() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setDepartmentId(1);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setReportPeriodId(1);
        declarationData.setState(State.ACCEPTED);
        declarationData.setAsnuId(1L);
        declarationData.setDepartmentReportPeriodId(1);
        declarationData.setKpp("123456789");
        declarationData.setTaxOrganCode("0123456789");
        return declarationData;
    }

    public boolean containLog(String text) {
        for (LogEntry logEntry : testHelper.getLogger().getEntries()) {
            if (logEntry.getMessage().contains(text) || logEntry.getMessage().matches(text)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void checkDataTest() {

        List<NdflPerson> ndflPersonList = mockFindNdflPerson();
        List<NdflPersonIncome> ndflPersonIncomeList = new ArrayList<NdflPersonIncome>();
        for (NdflPerson ndflPerson : ndflPersonList) {
            List<NdflPersonIncome> npiList = ndflPerson.getIncomes();
            for (NdflPersonIncome npi : npiList) {
                ndflPersonIncomeList.add(npi);
            }
        }

        when(testHelper.getNdflPersonService().findNdflPerson(any(Long.class))).thenReturn(ndflPersonList);
        when(testHelper.getNdflPersonService().findNdflPersonIncome(any(Long.class))).thenReturn(ndflPersonIncomeList);

        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();

        //1 Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '1'(.*)Текст ошибки: Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '2'(.*)Текст ошибки: Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24."));

        //2 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '3'(.*)Текст ошибки: Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '4'(.*)Текст ошибки: Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10."));

        //3 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '5'(.*)Текст ошибки: Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '6'(.*)Текст ошибки: Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11."));

        //4 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '7'(.*)Текст ошибки: Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '8'(.*)Текст ошибки: Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6."));

        //5 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '9'(.*)Текст ошибки: Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '10'(.*)Текст ошибки: Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7."));

        //6 Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22,23,24
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '11'(.*)Текст ошибки: Раздел 2. Графы 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22,23,24."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '12'(.*)Текст ошибки: Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22,23,24."));

        //7 Раздел 2. Графы 13,14,15 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '13'(.*)Текст ошибки: Раздел 2. Графы 13,14,15 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '14'(.*)Текст ошибки: Раздел 2. Графы 13,14,15 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24."));

        //8 Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 16
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '15'(.*)Текст ошибки: Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 16."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '16'(.*)Текст ошибки: Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 16."));

        //9 Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '17'(.*)Текст ошибки: Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '18'(.*)Текст ошибки: Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19."));

        //10 Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 17
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '19'(.*)Текст ошибки: Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 17."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '20'(.*)Текст ошибки: Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 17."));

        //11 Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 20
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '21'(.*)Текст ошибки: Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 20."));
        Assert.assertFalse(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '22'(.*)Текст ошибки: Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 20."));

        //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7,11 или 22,23,24
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '23'(.*)Текст ошибки: Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7,11 или 22,23,24."));
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '24'(.*)Текст ошибки: Раздел 2. Графа 21 должна быть не заполнена, если не заполнены Раздел 2. Графы 7,11 и 22,23,24."));

        //13 Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '25'(.*)Текст ошибки: Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них."));
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '26'(.*)Текст ошибки: Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них."));
        Assert.assertTrue(containLog("Ошибка в значении: Раздел 'Сведения о доходах и НДФЛ'. Строка '27'(.*)Текст ошибки: Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них."));
    }

    /**
     * Реквизиты
     * @return
     */
    private List<NdflPerson> mockFindNdflPerson() {
        List<NdflPerson> result = new ArrayList<NdflPerson>();

        NdflPerson ndflPerson1 = createNdflPerson(1L, "Иванов", "Иван", "Иванович", "12345");

        String column4Empty = null;
        String column4NotEmpty = "2000";
        String column5Empty = null;
        String column5NotEmpty = "01";
        Date column6Empty = null;
        Date column6NotEmpty = new Date();
        Date column7Empty = null;
        Date column7NotEmpty = new Date();
        BigDecimal column10Empty = null;
        BigDecimal column10NotEmpty = new BigDecimal(1);
        BigDecimal column11Empty = null;
        BigDecimal column11NotEmpty = new BigDecimal(1);
        BigDecimal column12Empty = null;
        BigDecimal column12NotEmpty = new BigDecimal(1);
        BigDecimal column13Empty = null;
        BigDecimal column13NotEmpty = new BigDecimal(1);
        Integer column14Empty = null;
        Integer column14NotEmpty = 1;
        Date column15Empty = null;
        Date column15NotEmpty = new Date();
        Long column16Empty = null;
        Long column16NotEmpty = 1L;
        Long column17Empty = null;
        Long column17NotEmpty = 1L;
        Long column18Empty = null;
        Long column18NotEmpty = 1L;
        Long column19Empty = null;
        Long column19NotEmpty = 1L;
        Long column20Empty = null;
        Long column20NotEmpty = 1L;
        Date column21Empty = null;
        Date column21NotEmpty = new Date();
        Date column22Empty = null;
        Date column22NotEmpty = new Date();
        String column23Empty = null;
        String column23NotEmpty = "1";
        Integer column24Empty = null;
        Integer column24NotEmpty = 1;

        //1 Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        final NdflPersonIncome ndflPersonIncome1 = createNdflPersonIncome(ndflPerson1.getId(), 1, column4Empty,
                column5Empty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22Empty, column23Empty, column24Empty);
        final NdflPersonIncome ndflPersonIncome2 = createNdflPersonIncome(ndflPerson1.getId(), 2, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22Empty, column23Empty, column24Empty);

        //2 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
        final NdflPersonIncome ndflPersonIncome3 = createNdflPersonIncome(ndflPerson1.getId(), 3, column4NotEmpty,
                column5NotEmpty, column6Empty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome4 = createNdflPersonIncome(ndflPerson1.getId(), 4, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //3 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
        final NdflPersonIncome ndflPersonIncome5 = createNdflPersonIncome(ndflPerson1.getId(), 5, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7Empty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome6 = createNdflPersonIncome(ndflPerson1.getId(), 6, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //4 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
        final NdflPersonIncome ndflPersonIncome7 = createNdflPersonIncome(ndflPerson1.getId(), 7, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10Empty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome8 = createNdflPersonIncome(ndflPerson1.getId(), 8, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //5 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
        final NdflPersonIncome ndflPersonIncome9 = createNdflPersonIncome(ndflPerson1.getId(), 9, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11Empty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome10 = createNdflPersonIncome(ndflPerson1.getId(), 10, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //6 Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22,23,24
        final NdflPersonIncome ndflPersonIncome11 = createNdflPersonIncome(ndflPerson1.getId(), 11, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13Empty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome12 = createNdflPersonIncome(ndflPerson1.getId(), 12, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12Empty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //7 Раздел 2. Графы 13,14,15 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        final NdflPersonIncome ndflPersonIncome13 = createNdflPersonIncome(ndflPerson1.getId(), 13, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13Empty, column14Empty, column15Empty, column16Empty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22Empty, column23Empty, column24Empty);
        final NdflPersonIncome ndflPersonIncome14 = createNdflPersonIncome(ndflPerson1.getId(), 14, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22Empty, column23Empty, column24Empty);

        //8 Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 16
        final NdflPersonIncome ndflPersonIncome15 = createNdflPersonIncome(ndflPerson1.getId(), 15, column4NotEmpty,
                column5NotEmpty, column6Empty, column7NotEmpty, column10Empty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18Empty,
                column19Empty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome16 = createNdflPersonIncome(ndflPerson1.getId(), 16, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //9 Раздел 2. Графы 6,10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
        final NdflPersonIncome ndflPersonIncome17 = createNdflPersonIncome(ndflPerson1.getId(), 17, column4NotEmpty,
                column5NotEmpty, column6Empty, column7NotEmpty, column10Empty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16Empty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome18 = createNdflPersonIncome(ndflPerson1.getId(), 18, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //10 Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 17
        final NdflPersonIncome ndflPersonIncome19 = createNdflPersonIncome(ndflPerson1.getId(), 19, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7Empty, column10NotEmpty, column11Empty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20Empty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome20 = createNdflPersonIncome(ndflPerson1.getId(), 20, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //11 Раздел 2. Графы 7,11 должны быть заполнены, если заполнена Раздел 2. Графа 20
        final NdflPersonIncome ndflPersonIncome21 = createNdflPersonIncome(ndflPerson1.getId(), 21, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7Empty, column10NotEmpty, column11Empty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17Empty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome22 = createNdflPersonIncome(ndflPerson1.getId(), 22, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24NotEmpty);

        //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7,11 или 22,23,24
        final NdflPersonIncome ndflPersonIncome23 = createNdflPersonIncome(ndflPerson1.getId(), 23, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7Empty, column10NotEmpty, column11Empty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21Empty, column22NotEmpty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome24 = createNdflPersonIncome(ndflPerson1.getId(), 24, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7Empty, column10NotEmpty, column11Empty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22Empty, column23Empty, column24Empty);

        //13 Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них
        final NdflPersonIncome ndflPersonIncome25 = createNdflPersonIncome(ndflPerson1.getId(), 25, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22Empty, column23NotEmpty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome26 = createNdflPersonIncome(ndflPerson1.getId(), 26, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23Empty, column24NotEmpty);
        final NdflPersonIncome ndflPersonIncome27 = createNdflPersonIncome(ndflPerson1.getId(), 27, column4NotEmpty,
                column5NotEmpty, column6NotEmpty, column7NotEmpty, column10NotEmpty, column11NotEmpty, column12NotEmpty,
                column13NotEmpty, column14NotEmpty, column15NotEmpty, column16NotEmpty, column17NotEmpty, column18NotEmpty,
                column19NotEmpty, column20NotEmpty, column21NotEmpty, column22NotEmpty, column23NotEmpty, column24Empty);

        ndflPerson1.setIncomes(new ArrayList<NdflPersonIncome>(){{
            add(ndflPersonIncome1);
            add(ndflPersonIncome2);
            add(ndflPersonIncome3);
            add(ndflPersonIncome4);
            add(ndflPersonIncome5);
            add(ndflPersonIncome6);
            add(ndflPersonIncome7);
            add(ndflPersonIncome8);
            add(ndflPersonIncome9);
            add(ndflPersonIncome10);
            add(ndflPersonIncome11);
            add(ndflPersonIncome12);
            add(ndflPersonIncome13);
            add(ndflPersonIncome14);
            add(ndflPersonIncome15);
            add(ndflPersonIncome16);
            add(ndflPersonIncome17);
            add(ndflPersonIncome18);
            add(ndflPersonIncome19);
            add(ndflPersonIncome20);
            add(ndflPersonIncome21);
            add(ndflPersonIncome22);
            add(ndflPersonIncome23);
            add(ndflPersonIncome24);
            add(ndflPersonIncome25);
            add(ndflPersonIncome26);
            add(ndflPersonIncome27);
        }});

        result.add(ndflPerson1);
        return result;
    }
    private NdflPerson createNdflPerson(Long id, String lastName, String firstName, String middleName, String inp) {
        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setId(id);
        ndflPerson.setLastName(lastName);
        ndflPerson.setFirstName(firstName);
        ndflPerson.setMiddleName(middleName);
        ndflPerson.setInp(inp);
        return ndflPerson;
    }

    /**
     * Сведения о доходах и НДФЛ
     * @return
     */
    private NdflPersonIncome createNdflPersonIncome(Long ndflPersonId, Integer rowNum,
                                                    String column4, String column5,
                                                    Date column6, Date column7, BigDecimal column10, BigDecimal column11,
                                                    BigDecimal column12, BigDecimal column13, Integer column14, Date column15,
                                                    Long column16, Long column17, Long column18, Long column19, Long column20,
                                                    Date column21, Date column22, String column23, Integer column24
    ) {
        NdflPersonIncome ndflPersonIncome = new NdflPersonIncome();

        ndflPersonIncome.setNdflPersonId(ndflPersonId);
        ndflPersonIncome.setRowNum(rowNum);

        ndflPersonIncome.setIncomeCode(column4);
        ndflPersonIncome.setIncomeType(column5);
        ndflPersonIncome.setIncomeAccruedDate(column6);
        ndflPersonIncome.setIncomePayoutDate(column7);
        ndflPersonIncome.setIncomeAccruedSumm(column10);
        ndflPersonIncome.setIncomePayoutSumm(column11);
        ndflPersonIncome.setTotalDeductionsSumm(column12);
        ndflPersonIncome.setTaxBase(column13);
        ndflPersonIncome.setTaxRate(column14);
        ndflPersonIncome.setTaxDate(column15);
        ndflPersonIncome.setCalculatedTax(column16);
        ndflPersonIncome.setWithholdingTax(column17);
        ndflPersonIncome.setNotHoldingTax(column18);
        ndflPersonIncome.setOverholdingTax(column19);
        ndflPersonIncome.setRefoundTax(column20);
        ndflPersonIncome.setTaxTransferDate(column21);
        ndflPersonIncome.setPaymentDate(column22);
        ndflPersonIncome.setPaymentNumber(column23);
        ndflPersonIncome.setTaxSumm(column24);

        return ndflPersonIncome;
    }
}
