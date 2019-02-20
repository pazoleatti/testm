package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.URM;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
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
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NdflPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NdflPersonDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private NdflPersonDao ndflPersonDao;

    @Before
    public void init() {
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(ndflPersonDao, "departmentDao", departmentDao);
        when(departmentDao.getParentTBId(anyInt())).thenReturn(7);
        when(departmentDao.getDepartmentIdsByType(anyInt())).thenReturn(asList(6, 7));
        Object repository = ReflectionTestUtils.getField(SqlUtils.class, "repository");
        Object namedParameterJdbcTemplate = ReflectionTestUtils.getField(ndflPersonDao, "namedParameterJdbcTemplate");
        ReflectionTestUtils.setField(repository, "namedParameterJdbcTemplate", namedParameterJdbcTemplate);
    }

    @Test
    public void buildQueryTest() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("lastName", "Иванов");
        parameters.put("firstName", "Федор");
        parameters.put("middleName", "Иванович");
        String sql = NdflPersonDaoImpl.buildQuery(parameters, null);
        assertTrue(sql.contains("lower(np.last_name) like lower(:lastName)"));
        assertFalse(sql.contains("np.inp = :inp"));
    }

    @Test
    public void findNdflPersonByParametersTest() {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(SubreportAliasConstants.LAST_NAME, "Иванов");
        parameters.put(SubreportAliasConstants.FIRST_NAME, "Федор");
        parameters.put(SubreportAliasConstants.TO_BIRTHDAY, new Date());

        PagingResult<NdflPerson> result = ndflPersonDao.fetchNdflPersonByParameters(1L, parameters, new PagingParams());
        assertEquals(1, result.size());
        assertEquals(1, result.getTotalCount());
    }

    @Test
    public void testGet() {
        NdflPerson person = ndflPersonDao.fetchOne(101);
        assertNotNull(person);
        assertEquals(2, person.getIncomes().size());
    }

    @Test
    public void testFindNdflPerson() {
        List<NdflPerson> result = ndflPersonDao.fetchByDeclarationData(1);

        for (NdflPerson person : result) {
            assertNotNull(person.getIncomes());
            assertNotNull(person.getDeductions());
            assertNotNull(person.getPrepayments());

            assertEquals(0, person.getIncomes().size());
            assertEquals(0, person.getDeductions().size());
            assertEquals(0, person.getPrepayments().size());
        }

        List<NdflPerson> result2 = ndflPersonDao.fetchByDeclarationData(2);
        assertNotNull(result2);
        assertEquals(10, result2.size());
        assertEquals(3, result.size());
    }

    private PagingParams pagingParams(int page, int count, String direction, String property) {
        PagingParams pagingParams = PagingParams.getInstance(page, count);
        pagingParams.setDirection(direction);
        pagingParams.setProperty(property);
        return pagingParams;
    }

    @Test
    public void combinedSearchByOperationId() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);
        // операция 1 есть во всех разделах 2-4
        filter.getIncome().setOperationId("1");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");

        AllSectionsResult result = fetchAll(filter, pagingParams);
        assertEquals(1, result.getOperationIds().size());
        assertEquals(asList(1, 3, 3, 3), asList(result.persons.size(), result.incomes.size(), result.deductions.size(), result.prepayments.size()));
    }

    @Test
    public void combinedSearchByOperationId2() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);
        // операция 2 есть только в разделах 2 и 3 (нет в р4)
        filter.getIncome().setOperationId("3");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");

        AllSectionsResult result = fetchAll(filter, pagingParams);
        assertEquals(1, result.getOperationIds().size());
        assertEquals(asList(1, 3, 3, 0), asList(result.persons.size(), result.incomes.size(), result.deductions.size(), result.prepayments.size()));
    }

    @Test
    public void combinedSearchIncomeTest() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);
        filter.getIncome().setKpp("555");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");

        AllSectionsResult result = fetchAll(filter, pagingParams);
        assertEquals(1, result.getOperationIds().size());
        assertEquals(asList(1, 1, 3, 3), asList(result.persons.size(), result.incomes.size(), result.deductions.size(), result.prepayments.size()));
    }

    @Test
    public void combinedSearchDeductionTest() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);
        filter.getDeduction().setNotifNum("555");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");

        AllSectionsResult result = fetchAll(filter, pagingParams);
        assertEquals(1, result.getOperationIds().size());
        assertEquals(asList(1, 3, 1, 3), asList(result.persons.size(), result.incomes.size(), result.deductions.size(), result.prepayments.size()));
    }

    @Test
    public void combinedSearchPrepaymentTest() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);
        filter.getPrepayment().setNotifSource("11");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");

        AllSectionsResult result = fetchAll(filter, pagingParams);
        assertEquals(2, result.getOperationIds().size());
        assertEquals(asList(2, 6, 6, 2), asList(result.persons.size(), result.incomes.size(), result.deductions.size(), result.prepayments.size()));
    }

    @Test
    public void combinedSearchAllTest() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);
        filter.getIncome().setOktmo("1");
        filter.getDeduction().setNotifType("11");
        filter.getPrepayment().setNotifNum("1");

        PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");

        AllSectionsResult result = fetchAll(filter, pagingParams);
        assertEquals(1, result.getOperationIds().size());
        assertEquals(asList(1, 3, 3, 1), asList(result.persons.size(), result.incomes.size(), result.deductions.size(), result.prepayments.size()));
    }

    private Map<List<URM>, Iterable<Long>> searchByURMCases = new HashMap<List<URM>, Iterable<Long>>() {{
        List<Long> ofCurrentTB = asList(50001L, 50002L, 50003L);
        List<Long> ofOthersTB = asList(50004L, 50005L, 50006L);
        List<Long> ofNoneTB = asList(50007L, 50008L, 50009L);

        put(Collections.EMPTY_LIST, concat(ofCurrentTB, ofOthersTB, ofNoneTB));
        put(asList(URM.CURRENT_TB), ofCurrentTB);
        put(asList(URM.OTHERS_TB), ofOthersTB);
        put(asList(URM.NONE_TB), ofNoneTB);
        put(asList(URM.CURRENT_TB, URM.OTHERS_TB), concat(ofCurrentTB, ofOthersTB));
        put(asList(URM.CURRENT_TB, URM.NONE_TB), concat(ofCurrentTB, ofNoneTB));
        put(asList(URM.OTHERS_TB, URM.NONE_TB), concat(ofOthersTB, ofNoneTB));
        put(asList(URM.CURRENT_TB, URM.OTHERS_TB, URM.NONE_TB), concat(ofCurrentTB, ofOthersTB, ofNoneTB));
    }};

    @Test
    public void searchByURM() {
        NdflFilter filter = new NdflFilter();
        filter.setDeclarationDataId(50000);

        for (Map.Entry<List<URM>, Iterable<Long>> searchByURMCase : searchByURMCases.entrySet()) {
            filter.getIncome().setUrmList(searchByURMCase.getKey());

            PagingParams pagingParams = pagingParams(1, 100, "asc", "inp");
            List<NdflPersonIncomeDTO> incomes = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);

            System.out.println(searchByURMCase.getKey().toString());
            System.out.println(searchByURMCase.getValue().toString());
            System.out.println(getIds(incomes).toString());
            assertEquals(newHashSet(searchByURMCase.getValue()), newHashSet(getIds(incomes)));
        }
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
        filter.getIncome().setOperationId("1");

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
        filter.getIncome().setOperationId("1");

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

        assertNotNull(id);

        NdflPerson ndflPerson = ndflPersonDao.fetchOne(id);

        assertTrue(EqualsBuilder.reflectionEquals(goodNdflPerson, ndflPerson, "incomes", "deductions", "prepayments"));

        boolean incomesEquals = CollectionUtils.isEqualCollection(goodNdflPerson.getIncomes(), ndflPerson.getIncomes(), new NdflPersonIncomeEquator());
        assertTrue(incomesEquals);

        boolean deductionsEquals = CollectionUtils.isEqualCollection(goodNdflPerson.getDeductions(), ndflPerson.getDeductions(), new NdflPersonDeductionEquator());
        assertTrue(deductionsEquals);

        boolean prepaymentEquals = CollectionUtils.isEqualCollection(goodNdflPerson.getPrepayments(), ndflPerson.getPrepayments(), new NdflPersonPrepaymentEquator());
        assertTrue(prepaymentEquals);
    }

    @Test(expected = DaoException.class)
    public void testBadSave() {
        NdflPerson person = createGoodNdflPerson();
        person.setDeclarationDataId(null);
        ndflPersonDao.save(person);
    }

    @Test
    public void testFetchOneNdflPersonIncome() {
        assertNotNull(ndflPersonDao.fetchOneNdflPersonIncome(1036));
    }

    @Test
    public void testFetchOneNdflPersonDeduction() {
        assertNotNull(ndflPersonDao.fetchOneNdflPersonDeduction(1));
    }

    @Test
    public void testFetchOneNdflPersonPrepayment() {
        assertNotNull(ndflPersonDao.fetchOneNdflPersonPrepayment(1));
    }

    @Test
    public void testFindIncomeOperationId() {
        List<String> result = ndflPersonDao.findIncomeOperationId(asList("a", "b", "3"));
        assertEquals(1, result.size());
        assertEquals("3", result.get(0));
    }

    @Test
    public void testFetchIncomeByNdflPersonId() {
        List<Long> result = ndflPersonDao.fetchIncomeIdByNdflPerson(101);
        assertEquals(2, result.size());
    }

    @Test
    public void testFetchDeductionByNdflPersonId() {
        List<Long> result = ndflPersonDao.fetchIncomeIdByNdflPerson(101);
        assertEquals(2, result.size());
    }

    @Test
    public void testFetchPrepaymentByNdflPersonId() {
        List<Long> result = ndflPersonDao.fetchIncomeIdByNdflPerson(101);
        assertEquals(2, result.size());
    }

    @Test
    public void testDeleteNdflPersonIncomeBatch() {
        ndflPersonDao.deleteNdflPersonIncomeBatch(asList(1036L, 1037L));
        assertNull(ndflPersonDao.fetchOneNdflPersonIncome(1036L));
        assertNull(ndflPersonDao.fetchOneNdflPersonIncome(1037L));
    }

    @Test
    public void testDeleteNdflPersonDeductionBatch() {
        ndflPersonDao.deleteNdflPersonDeductionBatch(asList(1L, 2L));
        assertNull(ndflPersonDao.fetchOneNdflPersonDeduction(1L));
        assertNull(ndflPersonDao.fetchOneNdflPersonDeduction(2L));
    }

    @Test
    public void testDeleteNdflPersonPrepaymentBatch() {
        ndflPersonDao.deleteNdflPersonPrepaymentBatch(asList(1L, 2L));
        assertNull(ndflPersonDao.fetchOneNdflPersonPrepayment(1L));
        assertNull(ndflPersonDao.fetchOneNdflPersonPrepayment(2L));
    }

    @Test
    public void testCheckIncomeExists() {
        assertTrue(ndflPersonDao.checkIncomeExists(1036, 1));
        assertFalse(ndflPersonDao.checkIncomeExists(1036, 2));
    }

    @Test
    public void ndflPersonExistsByDeclarationId() {
        assertTrue(ndflPersonDao.ndflPersonExistsByDeclarationId(1));
        assertFalse(ndflPersonDao.ndflPersonExistsByDeclarationId(111999555));
    }

    @Test
    public void testCheckDeductionsExists() {
        assertTrue(ndflPersonDao.checkDeductionExists(1, 1));
        assertFalse(ndflPersonDao.checkDeductionExists(1, 2));
    }

    @Test
    public void testCheckPrepaymentExists() {
        assertTrue(ndflPersonDao.checkPrepaymentExists(1, 1));
        assertFalse(ndflPersonDao.checkPrepaymentExists(1, 2));
    }

    @Test
    public void testUpdateNdflPersons() {
        List<NdflPerson> ndflPersonList = ndflPersonDao.fetchNdflPersonByIdList(asList(201L, 202L));
        ndflPersonList.get(0).setLastName("Петров");
        ndflPersonList.get(0).setModifiedBy("me");
        ndflPersonList.get(1).setLastName("Смит");
        ndflPersonList.get(1).setModifiedBy("me");
        ndflPersonDao.updateNdflPersons(ndflPersonList);
        List<NdflPerson> ndflPersonListResult = ndflPersonDao.fetchNdflPersonByIdList(asList(201L, 202L, 203L));
        assertEquals("me", ndflPersonListResult.get(0).getModifiedBy());
        assertEquals("Петров", ndflPersonListResult.get(0).getLastName());
        assertEquals("me", ndflPersonListResult.get(1).getModifiedBy());
        assertEquals("Смит", ndflPersonListResult.get(1).getLastName());
        assertNull(ndflPersonListResult.get(2).getModifiedBy());
        assertEquals("Иванов", ndflPersonListResult.get(2).getLastName());
    }

    @Test
    public void testUpdateIncomes() {
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonDao.fetchNdflPersonIncomeByNdflPerson(101L);
        ndflPersonIncomeList.get(0).setModifiedBy("me");
        ndflPersonIncomeList.get(1).setModifiedBy("me");
        ndflPersonDao.updateIncomes(ndflPersonIncomeList);
        List<NdflPersonIncome> ndflPersonIncomeListResult = ndflPersonDao.fetchNdflPersonIncomeByNdflPerson(101L);
        assertEquals("me", ndflPersonIncomeListResult.get(0).getModifiedBy());
    }

    @Test
    public void testUpdateDeductions() {
        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonDao.fetchNdflPersonDeductionByNdflPerson(101L);
        ndflPersonDeductionList.get(0).setModifiedBy("me");
        ndflPersonDeductionList.get(1).setModifiedBy("me");
        ndflPersonDao.updateDeductions(ndflPersonDeductionList);
        List<NdflPersonDeduction> ndflPersonDeductionListResult = ndflPersonDao.fetchNdflPersonDeductionByNdflPerson(101L);
        assertEquals("me", ndflPersonDeductionListResult.get(0).getModifiedBy());
    }

    @Test
    public void testUpdatePrepayments() {
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonDao.fetchNdflPersonPrepaymentByNdflPerson(101L);
        ndflPersonPrepaymentList.get(0).setModifiedBy("me");
        ndflPersonPrepaymentList.get(1).setModifiedBy("me");
        ndflPersonDao.updatePrepayments(ndflPersonPrepaymentList);
        List<NdflPersonPrepayment> ndflPersonPrepaymentListResult = ndflPersonDao.fetchNdflPersonPrepaymentByNdflPerson(101L);
        assertEquals("me", ndflPersonPrepaymentListResult.get(0).getModifiedBy());
    }

    @Test
    public void testUpdateNdflPersonsRowNum() {
        List<NdflPerson> ndflPersonList = ndflPersonDao.fetchNdflPersonByIdList(asList(201L, 202L));
        for (long i = 0; i < ndflPersonList.size(); i++) {
            ndflPersonList.get((int) i).setRowNum(i);
        }
        ndflPersonDao.updateNdflPersonsRowNum(ndflPersonList);
        List<NdflPerson> ndflPersonListResult = ndflPersonDao.fetchNdflPersonByIdList(asList(201L, 202L));
        assertEquals(new Long(0), ndflPersonListResult.get(0).getRowNum());
        assertEquals(new Long(1), ndflPersonListResult.get(1).getRowNum());
    }

    @Test
    public void testFetchPrepaymentByIncomesIdAndAccruedDate() {
        Calendar startDate = Calendar.getInstance();
        startDate.set(2005, Calendar.OCTOBER, 1, 0, 0, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2005, Calendar.OCTOBER, 10, 0, 0, 0);
        List<NdflPersonPrepayment> result = ndflPersonDao.fetchPrepaymentByIncomesIdAndAccruedDate(asList(1001L, 1002L, 1003L, 1004L), startDate.getTime(), endDate.getTime());
        assertEquals(3, result.size());
    }

    @Test
    public void testFetchRefBookPersons() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2018, Calendar.JANUARY, 1);
        List<NdflPerson> resultByDeclarationData = ndflPersonDao.fetchRefBookPersonsAsNdflPerson(1L, calendar.getTime());
        assertEquals(1, resultByDeclarationData.size());
        assertEquals("Федор", resultByDeclarationData.get(0).getFirstName());
    }

    @Test
    public void testFetchIncomeSourcesConsolidation() {
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(2018, Calendar.MAY, 25);
        Calendar startDate = Calendar.getInstance();
        startDate.set(2018, Calendar.JANUARY, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2018, Calendar.MARCH, 31);
        ConsolidationSourceDataSearchFilter filter = ConsolidationSourceDataSearchFilter.builder()
                .currentDate(currentDate.getTime())
                .periodStartDate(startDate.getTime())
                .periodEndDate(endDate.getTime())
                .consolidateDeclarationDataYear(2018)
                .dataSelectionDepth(2)
                .declarationType(100)
                .departmentId(7).build();
        List<ConsolidationIncome> incomes = ndflPersonDao.fetchIncomeSourcesConsolidation(filter);
        assertEquals(15, incomes.size());
        List<Long> incomesIds = new ArrayList<>();
        for (ConsolidationIncome income : incomes) {
            incomesIds.add(income.getId());
        }
        Collections.sort(incomes, new Comparator<ConsolidationIncome>() {
            @Override
            public int compare(ConsolidationIncome o1, ConsolidationIncome o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        assertTrue(incomesIds.contains(3070L));
        assertTrue(incomesIds.contains(3080L));
        assertTrue(incomesIds.contains(3090L));
        assertTrue(incomesIds.contains(3100L));
        assertTrue(incomesIds.contains(3110L));
        assertTrue(incomesIds.contains(3120L));
        assertTrue(incomesIds.contains(3130L));
        assertTrue(incomesIds.contains(3140L));
        assertTrue(incomesIds.contains(3150L));
        assertTrue(incomesIds.contains(3220L));
        assertTrue(incomesIds.contains(3230L));
        assertTrue(incomesIds.contains(3240L));
        assertTrue(incomesIds.contains(3250L));
        assertTrue(incomesIds.contains(3260L));
        assertTrue(incomesIds.contains(3270L));
    }

    @Test
    public void testFetchDeductionsForConsolidation() {
        List<NdflPersonDeduction> result = ndflPersonDao.fetchDeductionsForConsolidation(Collections.singletonList(1036L));
        assertEquals(1, result.size());
        assertEquals(Long.valueOf("1"), result.get(0).getId());
    }

    @Test
    public void testFetchPrepaymentsForConsolidation() {
        List<NdflPersonPrepayment> result = ndflPersonDao.fetchPrepaymentsForConsolidation((Collections.singletonList(1036L)));
        assertEquals(1, result.size());
        assertEquals(Long.valueOf("1"), result.get(0).getId());
    }

    @Test
    public void testFetchNdflPersonDeductionByNdflPersonAndOperation() {

        List<NdflPersonDeduction> result = ndflPersonDao.fetchNdflPersonDeductionByNdflPersonAndOperation(50001, "1");
        assertThat(result).hasSize(3);
    }

    @Test
    public void testFetchNdflPeronPrepaymentByNdflPersonAndOperation() {
        List<NdflPersonPrepayment> result = ndflPersonDao.fetchNdflPeronPrepaymentByNdflPersonAndOperation(1001, "1");
        assertThat(result).hasSize(1);
    }

    @Test
    public void testFindIncomesForPersonByKppOktmo() {
        List<NdflPersonIncome> result = ndflPersonDao.fetchNdflPersonIncomeByNdflPersonKppOktmo(Arrays.asList(1001L, 1002L), "99222", "111222333");
        assertThat(result).hasSize(3);
    }

    @Test
    public void test_findDeclarationDataIncomesWithSameOperationIdAndInp() {
        List<NdflPersonIncome> result = ndflPersonDao.findDeclarartionDataIncomesWithSameOperationIdAndInp(100L, "100500", "2");
        assertThat(result).hasSize(2);
    }

    @Test
    public void findOperationDate() {
        Date result = ndflPersonDao.findOperationDate(100L, "100500", "2");
        assertThat(result).isInSameDayAs("2005-10-02");
    }

    @Test
    public void testFindAllKppByDeclarationDataId() {
        List<KppSelect> kppList = ndflPersonDao.findAllKppByDeclarationDataId(100L, null, PagingParams.getInstance(0, 100));
        assertThat(kppList).hasSize(3);
        kppList = ndflPersonDao.findAllKppByDeclarationDataId(100L, null, PagingParams.getInstance(0, 3));
        assertThat(kppList).hasSize(3);
        kppList = ndflPersonDao.findAllKppByDeclarationDataId(100L, "99", PagingParams.getInstance(0, 100));
        assertThat(kppList).hasSize(2);
    }

    @Test
    public void findAllFor2Ndfl() {
        List<NdflPerson> persons = ndflPersonDao.findAllFor2Ndfl(100L, "99222", "111222333",
                new LocalDate(2005, 1, 1).toDate(), new LocalDate(2005, 10, 20).toDate());
        assertThat(persons).hasSize(1);
        assertThat(persons.get(0).getIncomes()).hasSize(3);
        assertThat(persons.get(0).getDeductions()).hasSize(2);
        assertThat(persons.get(0).getPrepayments()).hasSize(3);
    }


    private static Date toDate(String dateStr) {
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
        person.setAsnuId(1L);


        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<>();
        ndflPersonIncomes.add(createNdflPersonIncomes(1));
        ndflPersonIncomes.add(createNdflPersonIncomes(2));
        ndflPersonIncomes.add(createNdflPersonIncomes(3));
        person.setIncomes(ndflPersonIncomes);

        List<NdflPersonDeduction> ndflPersonDeductions = new ArrayList<>();
        ndflPersonDeductions.add(createNdflPersonDeduction(1));
        ndflPersonDeductions.add(createNdflPersonDeduction(2));
        person.setDeductions(ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = new ArrayList<>();
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
        personIncome.setAsnuId(1L);
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
        personDeduction.setAsnuId(1L);

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
        personPrepayment.setAsnuId(1L);

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

    class AllSectionsResult {
        List<NdflPerson> persons;
        List<NdflPersonIncomeDTO> incomes;
        List<NdflPersonDeductionDTO> deductions;
        List<NdflPersonPrepaymentDTO> prepayments;

        List<String> getOperationIds() {
            Set<String> operationIds = new HashSet<>();
            for (NdflPersonIncomeDTO income : incomes) {
                operationIds.add(income.getOperationId());
            }
            for (NdflPersonDeductionDTO deduction : deductions) {
                operationIds.add(deduction.getOperationId());
            }
            for (NdflPersonPrepaymentDTO prepayment : prepayments) {
                operationIds.add(prepayment.getOperationId());
            }
            return new ArrayList<>(operationIds);
        }
    }

    private AllSectionsResult fetchAll(NdflFilter filter, PagingParams pagingParams) {
        AllSectionsResult result = new AllSectionsResult();
        result.persons = ndflPersonDao.fetchNdflPersonByParameters(filter, pagingParams);
        result.incomes = ndflPersonDao.fetchPersonIncomeByParameters(filter, pagingParams);
        result.deductions = ndflPersonDao.fetchPersonDeductionByParameters(filter, pagingParams);
        result.prepayments = ndflPersonDao.fetchPersonPrepaymentByParameters(filter, pagingParams);
        return result;
    }

    private List<Long> getIds(List<NdflPersonIncomeDTO> incomes) {
        List<Long> ids = new ArrayList<>();
        for (NdflPersonIncomeDTO income : incomes) {
            ids.add(income.getId());
        }
        return ids;
    }
}
