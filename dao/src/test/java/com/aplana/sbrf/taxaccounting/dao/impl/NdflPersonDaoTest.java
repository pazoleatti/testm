package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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

    @Autowired
    private PagingParams pagingParams;

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

   /* @Test
    public void testUpdatePersonRefBookReferences() {

        List<NaturalPerson> ndflPersonList = ndflPersonDao.fetchByDeclarationData(2);
        assertEquals(10, ndflPersonList.size());

        for (NaturalPerson person : ndflPersonList) {
            person.setPersonId(Long.valueOf(new Random().nextInt(1000)));
        }

        ndflPersonDao.updatePersonRefBookReferences(ndflPersonList);

        List<NdflPerson> updatedList = ndflPersonDao.fetchByDeclarationData(2);
        assertEquals(10, updatedList.size());

        for (NdflPerson person : updatedList) {
            assertNotNull(person.getPersonId());
        }
    }*/

    @Test
    public void testPersonCount() {
        int result = ndflPersonDao.getPersonCount(1);
        assertEquals(3, result);
    }

    @Test
    public void testIncomeCount() {
        int result = ndflPersonDao.getPersonIncomeCount(1);
        assertEquals(3, result);
    }

    @Test
    public void testDeductionCount() {
        int result = ndflPersonDao.getPersonDeductionsCount(1);
        assertEquals(2, result);
    }

    @Test
    public void testPrepaymentCount() {
        int result = ndflPersonDao.getPersonPrepaymentCount(1);
        assertEquals(2, result);
    }

    @Test
    public void testFindPersonIncomeByParametersInp() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getInp()).thenReturn("100500");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("inp");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersOperationId() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getOperationId()).thenReturn("1");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("operationId");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersKpp() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getKpp()).thenReturn("2222");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("kpp");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersOktmo() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getOktmo()).thenReturn("111222333");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("oktmo");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersIncomeCode() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getIncomeCode()).thenReturn("1010");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("incomeCode");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersIncomeType() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getIncomeAttr()).thenReturn("00");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("incomeType");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersTaxRate() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getTaxRate()).thenReturn("13");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("taxRate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersIncomePaymentNumber() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getNumberPaymentOrder()).thenReturn("0");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("paymentNumber");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersTransferDateFrom() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getTransferDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("taxTransferDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersTransferDateTo() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getTransferDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersCalculationDateFrom() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getCalculationDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("taxDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersCalculationDateTo() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getCalculationDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("taxDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersPaymentDateFrom() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getPaymentDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("paymentDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersPaymentDateTo() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(filter.getPaymentDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("paymentDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByAccruedDate() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("incomeAccruedDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByPayoutDate() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("incomePayoutDate");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByAccruedSumm() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("incomeAccruedSumm");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByPayoutSumm() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("incomePayoutSumm");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByTotalDeductionsSumm() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("totalDeductionsSumm");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByTaxBase() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("taxBase");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByCalculatedTax() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("calculatedTax");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByWithholdingTax() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("withholdingTax");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByNotHoldingTax() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("notHoldingTax");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByNotOverholdingTax() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("overholdingTax");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByRefoundTax() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("refoundTax");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonIncomeByParametersSortByTaxSumm() {
        NdflPersonIncomeFilter filter = Mockito.mock(NdflPersonIncomeFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("taxSumm");

        List<NdflPersonIncomeDTO> result = ndflPersonDao.fetchPersonIncomeByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersInp() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getInp()).thenReturn("100500");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("inp");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersOperationId() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getOperationId()).thenReturn("1");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("operationId");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersTypeCode() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getDeductionCode()).thenReturn("100");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("typeCode");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersIncomeCode() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getIncomeCode()).thenReturn("0000");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("incomeCode");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersCalculationDateFrom() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getIncomeAccruedDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("incomeAccrued");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersCalculationDateTo() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getIncomeAccruedDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("incomeAccrued");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersPeriodCurrDateFrom() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getDeductionDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("periodCurrDate");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersPeriodCurrDateTo() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(filter.getDeductionDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("periodCurrDate");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifType() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("notifType");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifDate() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("notifDate");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifNum() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("notifNum");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifSource() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("notifSource");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByNotifSumm() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("notifSumm");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByIncomeSumm() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("incomeSumm");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindPersonDeductionByParametersSortByPeriodPrevDate() {
        NdflPersonDeductionFilter filter = Mockito.mock(NdflPersonDeductionFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("periodPrevDate");

        List<NdflPersonDeductionDTO> result = ndflPersonDao.fetchPersonDeductionByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindpersonPrepaymentByParametersInp() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(filter.getInp()).thenReturn("100500");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("inp");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindpersonPrepaymentByParametersOperationId() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(filter.getOperationId()).thenReturn("1");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("operationId");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindpersonPrepaymentByParametersNotifNum() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(filter.getNotifNum()).thenReturn("1");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("notifNum");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindpersonPrepaymentByParametersNotifSource() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(filter.getNotifSource()).thenReturn("01");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("notifSource");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonPrepaymentByParametersNotifDateFrom() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(filter.getNotifDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("notifDate");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonPrepaymentByParametersNotifDateTo() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(filter.getNotifDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("notifDate");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindPersonDeductionByParametersSortBySumm() {
        NdflPersonPrepaymentFilter filter = Mockito.mock(NdflPersonPrepaymentFilter.class);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("desc");
        when(pagingParams.getProperty()).thenReturn("summ");

        List<NdflPersonPrepaymentDTO> result = ndflPersonDao.fetchPersonPrepaymentByParameters(1, filter, pagingParams);
        assertEquals(   "2", result.get(0).getOperationId());
    }

    @Test
    public void testFindNdflPersonByParametersInp(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getInp()).thenReturn("1234567890");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("inp");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(9, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersInnNp(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getInnNp()).thenReturn("123456780");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("innNp");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(9, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersInnForeign(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getInnForeign()).thenReturn("123456770");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("innForeign");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(8, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersSnils(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getSnils()).thenReturn("123456760");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("snils");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(7, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersIdDocNumber(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getIdDocNumber()).thenReturn("0000");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("idDocNumber");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersLastName(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getLastName()).thenReturn("Иванов");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("lastName");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(8, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersFirstName(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getFirstName()).thenReturn("Ivan");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("firstName");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersMiddleName(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getMiddleName()).thenReturn("Петрович");
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("middleName");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersDateFrom(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getDateFrom()).thenReturn(new Date(1L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("birthDay");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersDateTo(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(filter.getDateTo()).thenReturn(new Date(30000000000000L));
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("birthDay");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(10, result.size());
    }

    @Test
    public void testFindNdflPersonByParametersSortByCitizenship(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("citizenship");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals(   "001", result.get(0).getCitizenship());
    }

    @Test
    public void testFindNdflPersonByParametersSortByIdDocType(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("idDocType");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("11", result.get(0).getIdDocType());
    }

    @Test
    public void testFindNdflPersonByParametersSortByStatus(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("status");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("1", result.get(0).getStatus());
    }

    @Test
    public void testFindNdflPersonByParametersSortByRegionCode(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("regionCode");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("01", result.get(0).getRegionCode());
    }

    @Test
    public void testFindNdflPersonByParametersSortByPostIndex(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("postIndex");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("123456", result.get(0).getPostIndex());
    }

    @Test
    public void testFindNdflPersonByParametersSortByArea(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("area");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Область1", result.get(0).getArea());
    }

    @Test
    public void testFindNdflPersonByParametersSortByCity(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("city");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Город1", result.get(0).getCity());
    }

    @Test
    public void testFindNdflPersonByParametersSortByLocality(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("locality");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Район1", result.get(0).getLocality());
    }

    @Test
    public void testFindNdflPersonByParametersSortByStreet(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("street");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("Улица1", result.get(0).getStreet());
    }

    @Test
    public void testFindNdflPersonByParametersSortByHouse(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("house");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("1", result.get(0).getHouse());
    }

    @Test
    public void testFindNdflPersonByParametersSortByBuilding(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("building");

        List<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        assertEquals("2", result.get(0).getBuilding());
    }

    @Test
    public void testFindNdflPersonByParametersSortByFlat(){
        NdflPersonFilter filter = Mockito.mock(NdflPersonFilter.class);
        when(filter.getDeclarationDataId()).thenReturn(2L);
        when(pagingParams.getCount()).thenReturn(100);
        when(pagingParams.getPage()).thenReturn(1);
        when(pagingParams.getDirection()).thenReturn("asc");
        when(pagingParams.getProperty()).thenReturn("flat");

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
