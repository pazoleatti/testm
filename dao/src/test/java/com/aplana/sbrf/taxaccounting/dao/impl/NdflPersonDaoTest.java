package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

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
        String sql = NdflPersonDaoImpl.buildQuery(parameters);
        Assert.assertTrue(sql.contains("np.last_name = :lastName"));
        Assert.assertFalse(sql.contains("np.inp = :inp"));
        //test...
    }

    @Test
    public void findNdflPersonByParametersTest() {

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("lastName", "Иванов");
        parameters.put("firstName", "Федор");
        parameters.put("middleName", "Иванович");
        //parameters.put("snils", "foo");
        //parameters.put("inn", "foo");
        //parameters.put("inp", "foo");
        //parameters.put("fromBirthDay", "foo");
        parameters.put("toBirthDay", new Date());
        //parameters.put("idDocNumber", "foo");

        PagingResult<NdflPerson> result = ndflPersonDao.findNdflPersonByParameters(1L, parameters, new PagingParams());
        assertEquals(1, result.size());
        assertEquals(1, result.getTotalCount());
        //добавить тестовых данных

    }

    @Test
    public void testGet() {
        NdflPerson person = ndflPersonDao.get(101);
        assertNotNull(person);
        Assert.assertEquals(2, person.getIncomes().size());
    }

    @Test
    public void testFindNdflPerson() {
        List<NdflPerson> result = ndflPersonDao.findPerson(1);

        for (NdflPerson person : result) {
            Assert.assertNotNull(person.getIncomes());
            Assert.assertNotNull(person.getDeductions());
            Assert.assertNotNull(person.getPrepayments());

            Assert.assertEquals(0, person.getIncomes().size());
            Assert.assertEquals(0, person.getDeductions().size());
            Assert.assertEquals(0, person.getPrepayments().size());
        }

        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testUpdatePersonRefBookReferences() {

        List<NdflPerson> ndflPersonList = ndflPersonDao.findPerson(2);
        assertEquals(10, ndflPersonList.size());

        for (NdflPerson person : ndflPersonList) {
            person.setPersonId(Long.valueOf(new Random().nextInt(1000)));
        }

        ndflPersonDao.updatePersonRefBookReferences(ndflPersonList);

        List<NdflPerson> updatedList = ndflPersonDao.findPerson(2);
        assertEquals(10, updatedList.size());

        for (NdflPerson person : updatedList) {
            assertNotNull(person.getPersonId());
        }
    }

    @Test
    public void testGoodSave() {

        NdflPerson goodNdflPerson = createGoodNdflPerson();
        Long id = ndflPersonDao.save(goodNdflPerson);

        Assert.assertNotNull(id);

        NdflPerson ndflPerson = ndflPersonDao.get(id);

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


    @Test(expected = DaoException.class)
    public void testDelete() {
        NdflPerson goodNdflPerson = createGoodNdflPerson();
        Long id = ndflPersonDao.save(goodNdflPerson);
        Assert.assertNotNull(id);

        NdflPerson ndflPerson = ndflPersonDao.get(id);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(goodNdflPerson, ndflPerson, "incomes", "deductions", "prepayments"));

        ndflPersonDao.delete(id);
        NdflPerson deleted = ndflPersonDao.get(id);
        assertNull(deleted);
        //test cascade delete
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
        personIncome.setRowNum(row);
        personIncome.setOperationId(11111L);
        personIncome.setOktmo("oktmo111");
        personIncome.setKpp("kpp111");
        personIncome.setSourceId(112233L);
        return personIncome;
    }

    private NdflPersonDeduction createNdflPersonDeduction(int row) {
        NdflPersonDeduction personDeduction = new NdflPersonDeduction();
        personDeduction.setRowNum(row);
        personDeduction.setOperationId(11111L);
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
        personPrepayment.setRowNum(row);
        personPrepayment.setOperationId(11111L);

        personPrepayment.setSumm(1999999L);
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
