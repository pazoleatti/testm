package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Drunk
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NdflPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NdflPersonDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private NdflPersonDao ndflPersonDao;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void buildQueryTest() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastName", "Иванов");
        parameters.put("firstName", "Федор");
        parameters.put("middleName", "Иванович");
        String sql = NdflPersonDaoImpl.buildQuery(parameters, null);
        Assert.assertTrue(sql.contains("lower(np.last_name) like lower(:lastName)"));
        Assert.assertFalse(sql.contains("np.inp = :inp"));
        //test...
    }

    @Test
    public void findNdflPersonByParametersTest() {

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put(SubreportAliasConstants.LAST_NAME, "Иванов");
        parameters.put(SubreportAliasConstants.FIRST_NAME, "Федор");
        //parameters.put("middleName", "Иванович");
        //parameters.put("snils", "foo");
        //parameters.put("inn", "foo");
        //parameters.put("inp", "foo");
        //parameters.put("fromBirthDay", "foo");
        parameters.put(SubreportAliasConstants.TO_BIRTHDAY, new Date());
        //parameters.put("idDocNumber", "foo");

        PagingResult<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(1L, parameters, new PagingParams());
        assertEquals(1, result.size());
        assertEquals(1, result.getTotalCount());
        //добавить тестовых данных

    }

    @Test
    public void testGet() {
        NdflPerson person = ndflPersonDao.fetchOne(101);
        assertNotNull(person);
        Assert.assertEquals(2, person.getIncomes().size());
    }

    @Test
    public void testFindNdflPerson() {
        List<NdflPerson> result = ndflPersonDao.fetchByDeclarationData(1);

        for (NdflPerson person : result) {
            Assert.assertNotNull(person.getIncomes());
            Assert.assertNotNull(person.getDeductions());
            Assert.assertNotNull(person.getPrepayments());

            Assert.assertEquals(0, person.getIncomes().size());
            Assert.assertEquals(0, person.getDeductions().size());
            Assert.assertEquals(0, person.getPrepayments().size());
        }

        List<NdflPerson> result2 = ndflPersonDao.fetchByDeclarationData(2);
        Assert.assertNotNull(result2);
        Assert.assertEquals(10, result2.size());
        Assert.assertEquals(3, result.size());
    }

    private PagingParams pagingParams(int page, int count, String direction, String property) {
        PagingParams pagingParams = PagingParams.getInstance(page, count);
        pagingParams.setDirection(direction);
        pagingParams.setProperty(property);
        return pagingParams;
    }

    @Test
    public void testFindPersonIncomeByParametersInp() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPerson().setInp("100500");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersOperationId() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setOperationId("1");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "operationId");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersKpp() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setKpp("2222");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "kpp");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersOktmo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setOktmo("111222333");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "oktmo");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersIncomeCode() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setIncomeCode("1010");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "incomeCode");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersIncomeType() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setIncomeAttr("00");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "incomeType");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersTaxRate() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setTaxRate("13");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "taxRate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersIncomePaymentNumber() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setNumberPaymentOrder("0");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "paymentNumber");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersTransferDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setTransferDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "taxTransferDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersTransferDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setTransferDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "taxTransferDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersCalculationDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setCalculationDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "taxDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersCalculationDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setCalculationDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "taxDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersPaymentDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setPaymentDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "paymentDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersPaymentDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setPaymentDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "paymentDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByAccruedDate() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setTaxRate("13");

        PagingParams pagingParams = pagingParams(1, 100, "desc", "incomeAccruedDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByPayoutDate() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "incomePayoutDate");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByAccruedSumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "incomeAccruedSumm");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByPayoutSumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "incomePayoutSumm");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByTotalDeductionsSumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getIncome().setTaxRate("13");

        PagingParams pagingParams = pagingParams(1, 100, "desc", "totalDeductionsSumm");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByTaxBase() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "taxBase");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByCalculatedTax() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "calculatedTax");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByWithholdingTax() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "withholdingTax");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByNotHoldingTax() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "notHoldingTax");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByNotOverholdingTax() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "overholdingTax");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByRefoundTax() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "refoundTax");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByTaxSumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "taxSumm");
        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersInp() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPerson().setInp("100500");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersOperationId() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setOperationId("1");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "operationId");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersTypeCode() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setDeductionCode("100");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "typeCode");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersIncomeCode() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setDeductionIncomeCode("0000");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "incomeCode");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersCalculationDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setIncomeAccruedDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "incomeAccrued");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersCalculationDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setIncomeAccruedDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "incomeAccrued");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersPeriodCurrDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setDeductionDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "periodCurrDate");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersPeriodCurrDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getDeduction().setDeductionDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "periodCurrDate");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifType() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "notifType");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifDate() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "notifDate");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifNum() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "notifNum");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifSource() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "notifSource");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifSumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "notifSumm");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByIncomeSumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "incomeSumm");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByPeriodPrevDate() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "periodPrevDate");
        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindpersonPrepaymentByParametersInp() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPerson().setInp("100500");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindpersonPrepaymentByParametersOperationId() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPrepayment().setOperationId("1");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "operationId");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindpersonPrepaymentByParametersNotifNum() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPrepayment().setNotifNum("1");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "notifNum");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindpersonPrepaymentByParametersNotifSource() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPrepayment().setNotifSource("01");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "notifSource");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonPrepaymentByParametersNotifDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPrepayment().setNotifDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "notifDate");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonPrepaymentByParametersNotifDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);
        filter.getPrepayment().setNotifDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "notifDate");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersSortBySumm() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(1);

        PagingParams pagingParams = pagingParams(1, 100, "desc", "summ");
        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getOperationId());
    }

    @Test
    public void testFindNdflPersonByParametersInp() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setInp("1234567890");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(9, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersInnNp() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setInnNp("123456780");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "innNp");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(9, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersInnForeign() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setInnForeign("123456770");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "innForeign");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(8, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersSnils() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setSnils("123456760");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "snils");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(7, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersIdDocNumber() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setIdDocNumber("0000");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "idDocNumber");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersLastName() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setLastName("Иванов");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "lastName");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(8, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersFirstName() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setFirstName("Ivan");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "firstName");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersMiddleName() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setMiddleName("Петрович");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "middleName");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersDateFrom() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setDateFrom(new Date(1L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "birthDay");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersDateTo() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);
        filter.getPerson().setDateTo(new Date(30000000000000L));

        PagingParams pagingParams = pagingParams(1, 100, "asc", "birthDay");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersSortByCitizenship() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "citizenship");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("001", result.get(0).getCitizenship());
    }

    @Test
    public void testFindNdflPersonByParametersSortByIdDocType() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "idDocType");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("11", result.get(0).getIdDocType());
    }

    @Test
    public void testFindNdflPersonByParametersSortByStatus() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "status");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("1", result.get(0).getStatus());
    }

    @Test
    public void testFindNdflPersonByParametersSortByRegionCode() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "regionCode");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("01", result.get(0).getRegionCode());
    }

    @Test
    public void testFindNdflPersonByParametersSortByPostIndex() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "postIndex");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("123456", result.get(0).getPostIndex());
    }

    @Test
    public void testFindNdflPersonByParametersSortByArea() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "area");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Область1", result.get(0).getArea());
    }

    @Test
    public void testFindNdflPersonByParametersSortByCity() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "city");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Город1", result.get(0).getCity());
    }

    @Test
    public void testFindNdflPersonByParametersSortByLocality() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "locality");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Район1", result.get(0).getLocality());
    }

    @Test
    public void testFindNdflPersonByParametersSortByStreet() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "street");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Улица1", result.get(0).getStreet());
    }

    @Test
    public void testFindNdflPersonByParametersSortByHouse() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "house");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("1", result.get(0).getHouse());
    }

    @Test
    public void testFindNdflPersonByParametersSortByBuilding() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "building");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getBuilding());
    }

    @Test
    public void testFindNdflPersonByParametersSortByFlat() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(2);

        PagingParams pagingParams = pagingParams(1, 100, "asc", "flat");
        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("3", result.get(0).getFlat());
    }

    //@Test
    public void testGoodSave() {

        NdflPerson goodNdflPerson = createGoodNdflPerson();
        Long id = ndflPersonDao.save(goodNdflPerson);

        Assert.assertNotNull(id);

        NdflPerson ndflPerson = ndflPersonDao.fetchOne(id);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(goodNdflPerson, ndflPerson, "incomes", "deductions", "prepayments"));

        boolean incomesEquals = CollectionUtils.isEqualCollection(goodNdflPerson.getIncomes(), ndflPerson.getIncomes(), new NdflPersonIncomeEquator());
        Assert.assertTrue(incomesEquals);

        boolean deductionsEquals = CollectionUtils.isEqualCollection(goodNdflPerson.getDeductions(), ndflPerson.getDeductions(), new NdflPersonDeductionEquator());
        Assert.assertTrue(deductionsEquals);

        boolean prepaymentEquals = CollectionUtils.isEqualCollection(goodNdflPerson.getPrepayments(), ndflPerson.getPrepayments(), new NdflPersonPrepaymentEquator());
        Assert.assertTrue(prepaymentEquals);
    }

    @Test(expected = DaoException.class)
    public void testBadSave() {
        NdflPerson person = createGoodNdflPerson();
        person.setDeclarationDataId(null);
        Long id = ndflPersonDao.save(person);
    }

    @Test
    public void testFetchOneNdflPersonIncome() {
        Assert.assertNotNull(ndflPersonDao.fetchOneNdflPersonIncome(1036));
    }

    public void testFetchOneNdflPersonDeduction() {
        Assert.assertNotNull(ndflPersonDao.fetchOneNdflPersonDeduction(1));
    }

    @Test
    public void testFetchOneNdflPersonPrepayment() {
        Assert.assertNotNull(ndflPersonDao.fetchOneNdflPersonPrepayment(1));
    }

    @Test
    public void testFindIncomeOperationId() {
        List<String> result = ndflPersonDao.findIncomeOperationId(Arrays.asList("a", "b", "3"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("3", result.get(0));
    }

    @Test
    public void testFetchIncomeByNdflPersonId() {
        List<Long> result = ndflPersonDao.fetchIncomeIdByNdflPerson(101);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testFetchDeductionByNdflPersonId() {
        List<Long> result = ndflPersonDao.fetchIncomeIdByNdflPerson(101);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testFetchPrepaymentByNdflPersonId() {
        List<Long> result = ndflPersonDao.fetchIncomeIdByNdflPerson(101);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testDeleteNdflPersonIncomeBatch() {
        ndflPersonDao.deleteNdflPersonIncomeBatch(Arrays.asList(1036L, 1037L));
        Assert.assertNull(ndflPersonDao.fetchOneNdflPersonIncome(1036L));
        Assert.assertNull(ndflPersonDao.fetchOneNdflPersonIncome(1037L));
    }

    @Test
    public void testDeleteNdflPersonDeductionBatch() {
        ndflPersonDao.deleteNdflPersonDeductionBatch(Arrays.asList(1L, 2L));
        Assert.assertNull(ndflPersonDao.fetchOneNdflPersonDeduction(1L));
        Assert.assertNull(ndflPersonDao.fetchOneNdflPersonDeduction(2L));
    }

    @Test
    public void testDeleteNdflPersonPrepaymentBatch() {
        ndflPersonDao.deleteNdflPersonPrepaymentBatch(Arrays.asList(1L, 2L));
        Assert.assertNull(ndflPersonDao.fetchOneNdflPersonPrepayment(1L));
        Assert.assertNull(ndflPersonDao.fetchOneNdflPersonPrepayment(2L));
    }

    @Test
    public void testCheckIncomeExists() {
        Assert.assertTrue(ndflPersonDao.checkIncomeExists(1036, 1));
        Assert.assertFalse(ndflPersonDao.checkIncomeExists(1036, 2));
    }

    @Test
    public void testCheckDeductionsExists() {
        Assert.assertTrue(ndflPersonDao.checkDeductionExists(1, 1));
        Assert.assertFalse(ndflPersonDao.checkDeductionExists(1, 2));
    }

    @Test
    public void testCheckPrepaymentExists() {
        Assert.assertTrue(ndflPersonDao.checkPrepaymentExists(1, 1));
        Assert.assertFalse(ndflPersonDao.checkPrepaymentExists(1, 2));
    }

    @Test
    public void testUpdateIncomes() {
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.fetchNdflPersonIncomeByNdflPerson(101L);
        ndflPersonIncomeList.get(0).setModifiedBy("me");
        ndflPersonIncomeList.get(1).setModifiedBy("me");
        ndflPersonDao.updateIncomes(ndflPersonIncomeList);
        List<NdflPersonIncome> ndflPersonIncomeListResult = ndflPersonDao.fetchNdflPersonIncomeByNdflPerson(101L);
        Assert.assertEquals(ndflPersonIncomeListResult.get(0).getModifiedBy(), "me");
    }

    @Test
    public void testUpdateDeductions() {
        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonDao.fetchNdflPersonDeductionByNdflPerson(101L);
        ndflPersonDeductionList.get(0).setModifiedBy("me");
        ndflPersonDeductionList.get(1).setModifiedBy("me");
        ndflPersonDao.updateDeductions(ndflPersonDeductionList);
        List<NdflPersonDeduction> ndflPersonDeductionListResult = ndflPersonDao.fetchNdflPersonDeductionByNdflPerson(101L);
        Assert.assertEquals(ndflPersonDeductionListResult.get(0).getModifiedBy(), "me");
    }

    @Test
    public void testUpdatePrepayments() {
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonDao.fetchNdflPersonPrepaymentByNdflPerson(101L);
        ndflPersonPrepaymentList.get(0).setModifiedBy("me");
        ndflPersonPrepaymentList.get(1).setModifiedBy("me");
        ndflPersonDao.updatePrepayments(ndflPersonPrepaymentList);
        List<NdflPersonPrepayment> ndflPersonPrepaymentListResult = ndflPersonDao.fetchNdflPersonPrepaymentByNdflPerson(101L);
        Assert.assertEquals(ndflPersonPrepaymentListResult.get(0).getModifiedBy(), "me");
    }

    @Test
    public void testUpdateNdflPersonsRowNum() {
        List<NdflPerson> ndflPersonList = ndflPersonDao.fetchNdflPersonByIdList(Arrays.asList(201L, 202L));
        for (long i = 0; i < ndflPersonList.size(); i++) {
            ndflPersonList.get((int) i).setRowNum(i);
        }
        ndflPersonDao.updateNdflPersonsRowNum(ndflPersonList);
        List<NdflPerson> ndflPersonListResult = ndflPersonDao.fetchNdflPersonByIdList(Arrays.asList(201L, 202L));
        Assert.assertEquals(new Long(0), ndflPersonListResult.get(0).getRowNum());
        Assert.assertEquals(new Long(1), ndflPersonListResult.get(1).getRowNum());
    }

    @Test
    public void testFindInpCountForPersonsAndIncomeAccruedDatePeriod() {
        Calendar startDate = Calendar.getInstance();
        startDate.set(2005, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2005, 11, 31);
        int result = ndflPersonDao.findInpCountForPersonsAndIncomeAccruedDatePeriod(Arrays.asList(101L, 102L), startDate.getTime(), endDate.getTime());
        Assert.assertEquals(2, result);
    }

    @Test
    public void testFetchPrepaymentByIncomesIdAndAccruedDate() {
        Calendar startDate = Calendar.getInstance();
        startDate.set(2005, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2005, 11, 31);
        List<NdflPersonPrepayment> result = ndflPersonDao.fetchPrepaymentByIncomesIdAndAccruedDate(Arrays.asList(1036L, 1037L, 1038L), startDate.getTime(), endDate.getTime());
        Assert.assertEquals(2, result.size());
    }

    public static Date toDate(String dateStr) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private NdflPerson createGoodNdflPerson() {

        NdflPerson person = new NdflPerson();
        person.setId(null);
        person.setDeclarationDataId(1L);
        person.setInp("000-000-000-00");
        person.setSnils("123-321-111-11");
        person.setLastName("Иванов");
        person.setFirstName("Иван");
        person.setMiddleName("Иванович");
        person.setBirthDay(toDate("01.01.1980"));
        person.setCitizenship("643");

        person.setInnNp("123456789123");
        person.setInnForeign("");
        person.setIdDocType("11");
        person.setIdDocNumber("2002 123456");
        person.setStatus("1");
        person.setPostIndex("394000");
        person.setRegionCode("77");
        person.setArea("MSK");
        person.setCity("");

        person.setLocality("");
        person.setStreet(null);
        person.setHouse("");
        person.setBuilding("");
        person.setFlat("");
        person.setCountryCode("643");
        person.setAddress("");
        person.setAdditionalData("");


        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<NdflPersonIncome>();
        ndflPersonIncomes.add(createNdflPersonIncomes(1));
        ndflPersonIncomes.add(createNdflPersonIncomes(2));
        ndflPersonIncomes.add(createNdflPersonIncomes(3));
        person.setIncomes(ndflPersonIncomes);

        List<NdflPersonDeduction> ndflPersonDeductions = new ArrayList<NdflPersonDeduction>();
        ndflPersonDeductions.add(createNdflPersonDeduction(1));
        ndflPersonDeductions.add(createNdflPersonDeduction(2));
        person.setDeductions(ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = new ArrayList<NdflPersonPrepayment>();
        ndflPersonPrepayments.add(createNdflPersonPrepayment(1));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(2));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(3));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(5));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(5));

        person.setPrepayments(ndflPersonPrepayments);


        return person;
    }


    private NdflPersonIncome createNdflPersonIncomes(int row) {
        NdflPersonIncome personIncome = new NdflPersonIncome();
        personIncome.setRowNum(BigDecimal.valueOf(row));
        personIncome.setOperationId("11111");
        personIncome.setOktmo("oktmo111");
        personIncome.setKpp("kpp111");
        personIncome.setSourceId(112233L);
        return personIncome;
    }

    private NdflPersonDeduction createNdflPersonDeduction(int row) {
        NdflPersonDeduction personDeduction = new NdflPersonDeduction();
        personDeduction.setRowNum(BigDecimal.valueOf(row));
        personDeduction.setOperationId("11111");
        personDeduction.setTypeCode("001");

        personDeduction.setNotifType("11");
        personDeduction.setNotifDate(toDate("01.01.1980"));
        personDeduction.setNotifNum("notif_num");
        personDeduction.setNotifSource("notif_source");
        personDeduction.setNotifSumm(new BigDecimal("999999.99"));

        personDeduction.setIncomeAccrued(toDate("01.01.2016"));
        personDeduction.setIncomeCode("1234");
        personDeduction.setIncomeSumm(new BigDecimal("999999.99")); //123456789123456789.12

        personDeduction.setPeriodPrevDate(toDate("01.01.2016"));
        personDeduction.setPeriodPrevSumm(new BigDecimal("999999.99")); //123456789123456789.12
        personDeduction.setPeriodCurrDate(toDate("01.01.2016"));
        personDeduction.setPeriodCurrSumm(new BigDecimal("999999.99"));
        personDeduction.setSourceId(112233L);

        return personDeduction;
    }

    private NdflPersonPrepayment createNdflPersonPrepayment(int row) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
        personPrepayment.setRowNum(BigDecimal.valueOf(row));
        personPrepayment.setOperationId("11111");

        personPrepayment.setSumm(new BigDecimal("1999999")); //по xsd это поле xs:integer
        personPrepayment.setNotifNum("123-456-000");
        personPrepayment.setNotifDate(toDate("01.01.2016"));
        personPrepayment.setNotifSource("AAA");
        personPrepayment.setSourceId(112233L);

        return personPrepayment;
    }


    class NdflPersonIncomeEquator implements Equator<NdflPersonIncome> {
        @Override
        public boolean equate(NdflPersonIncome o1, NdflPersonIncome o2) {
            return EqualsBuilder.reflectionEquals(o1, o2);
        }

        @Override
        public int hash(NdflPersonIncome o) {
            return HashCodeBuilder.reflectionHashCode(o);
        }
    }

    class NdflPersonDeductionEquator implements Equator<NdflPersonDeduction> {
        @Override
        public boolean equate(NdflPersonDeduction o1, NdflPersonDeduction o2) {
            return EqualsBuilder.reflectionEquals(o1, o2);
        }

        @Override
        public int hash(NdflPersonDeduction o) {
            return HashCodeBuilder.reflectionHashCode(o);
        }
    }

    class NdflPersonPrepaymentEquator implements Equator<NdflPersonPrepayment> {
        @Override
        public boolean equate(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
            return EqualsBuilder.reflectionEquals(o1, o2);
        }

        @Override
        public int hash(NdflPersonPrepayment o) {
            return HashCodeBuilder.reflectionHashCode(o);
        }
    }


}
