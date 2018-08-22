package com.aplana.sbrf.taxaccounting.model.ndflPerson;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class NdflPersonIncomeTest {

    @Test
    public void testNdflPersonIncomeComporatorByOperationDate() {
        Calendar instance = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 3);
        income1.setOperationId("1");
        income1.setPaymentDate(instance.getTime());

        NdflPersonIncome income2 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 1);
        income2.setOperationId("2");
        income2.setIncomeAccruedDate(instance.getTime());

        List<NdflPersonIncome> ndflPersonIncomeList1 = new ArrayList<>(Arrays.asList(income1, income2));

        NdflPerson person1 = new NdflPerson();
        person1.setInp("qqq");
        person1.setIncomes(ndflPersonIncomeList1);

        Collections.sort(ndflPersonIncomeList1, NdflPersonIncome.getComparator(person1));

        assertEquals("1", ndflPersonIncomeList1.get(1).getOperationId());
        assertEquals("2", ndflPersonIncomeList1.get(0).getOperationId());
    }

    @Test
    public void testNdflPersonIncomeComporatorByOperationId() {
        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setOperationId("1");

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setOperationId("3");

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setOperationId("2");

        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setIncomes(ndflPersonIncomes);

        Collections.sort(ndflPersonIncomes, NdflPersonIncome.getComparator(ndflPerson));

        assertEquals("1", ndflPersonIncomes.get(0).getOperationId());
        assertEquals("2", ndflPersonIncomes.get(1).getOperationId());
        assertEquals("3", ndflPersonIncomes.get(2).getOperationId());
    }

    @Test
    public void testNdflPersonIncomeComporatorByActionDate() {
        Calendar calendar = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setOperationId("1");
        calendar.set(2010, Calendar.JANUARY, 5);
        income1.setTaxDate(calendar.getTime());

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setOperationId("1");
        calendar.set(2010, Calendar.JANUARY, 1);
        income2.setTaxDate(calendar.getTime());

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setOperationId("1");
        calendar.set(2010, Calendar.JANUARY, 2);
        income3.setPaymentDate(calendar.getTime());

        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setIncomes(ndflPersonIncomes);

        Collections.sort(ndflPersonIncomes, NdflPersonIncome.getComparator(ndflPerson));

        calendar.set(2010, Calendar.JANUARY, 1);
        assertEquals(calendar.getTime(), ndflPersonIncomes.get(0).getTaxDate());
        calendar.set(2010, Calendar.JANUARY, 2);
        assertEquals(calendar.getTime(), ndflPersonIncomes.get(1).getPaymentDate());
        calendar.set(2010, Calendar.JANUARY, 5);
        assertEquals(calendar.getTime(), ndflPersonIncomes.get(2).getTaxDate());
    }

    @Test
    public void testNdflPersonIncomeComporatorBySringType() {
        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setId(1L);
        income1.setIncomePayoutDate(new Date());

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setId(2L);

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setId(3L);
        income3.setIncomeAccruedDate(new Date());

        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setIncomes(ndflPersonIncomes);

        Collections.sort(ndflPersonIncomes, NdflPersonIncome.getComparator(ndflPerson));

        assertEquals(new Long(3), ndflPersonIncomes.get(0).getId());
        assertEquals(new Long(1), ndflPersonIncomes.get(1).getId());
        assertEquals(new Long(2), ndflPersonIncomes.get(2).getId());
    }

    @Test
    public void testNdflSortAndUpdateRowNum1(){
        Calendar instance = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 1);
        income1.setOperationId("1");
        income1.setPaymentDate(instance.getTime());
        income1.setRowNum(new BigDecimal("1"));

        NdflPersonIncome income2 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 3);
        income2.setOperationId("2");
        income2.setIncomeAccruedDate(instance.getTime());
        income2.setRowNum(new BigDecimal("2"));

        NdflPersonIncome income3 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income3.setOperationId("3");
        income3.setPaymentDate(instance.getTime());
        income3.setRowNum(new BigDecimal("3"));

        NdflPersonIncome income4 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income4.setOperationId("3");
        income4.setIncomeAccruedDate(instance.getTime());
        income4.setRowNum(new BigDecimal("4"));

        NdflPerson person1 = new NdflPerson();
        person1.setInp("qqq");
        person1.setIncomes(new ArrayList<>(Arrays.asList(income1, income2, income3, income4)));

        person1.setIncomes(NdflPersonIncome.sortAndUpdateRowNum(person1));

        assertEquals("1", person1.getIncomes().get(0).getOperationId());
        assertEquals("3", person1.getIncomes().get(1).getOperationId());
        assertEquals("3", person1.getIncomes().get(2).getOperationId());
        assertEquals("2", person1.getIncomes().get(3).getOperationId());

        assertEquals(new BigDecimal("1"), person1.getIncomes().get(0).getRowNum());
        assertEquals(new BigDecimal("2"), person1.getIncomes().get(1).getRowNum());
        assertEquals(new BigDecimal("3"), person1.getIncomes().get(2).getRowNum());
        assertEquals(new BigDecimal("4"), person1.getIncomes().get(3).getRowNum());
    }

    @Test
    public void testNdflSortAndUpdateRowNum2(){
        Calendar instance = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 1);
        income1.setOperationId("1");
        income1.setPaymentDate(instance.getTime());
        income1.setRowNum(new BigDecimal("5"));

        NdflPersonIncome income2 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 3);
        income2.setOperationId("2");
        income2.setIncomeAccruedDate(instance.getTime());
        income2.setRowNum(new BigDecimal("6"));

        NdflPersonIncome income3 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income3.setOperationId("3");
        income3.setPaymentDate(instance.getTime());
        income3.setRowNum(new BigDecimal("7"));

        NdflPersonIncome income4 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income4.setOperationId("3");
        income4.setIncomeAccruedDate(instance.getTime());
        income4.setRowNum(new BigDecimal("8"));

        NdflPerson person1 = new NdflPerson();
        person1.setInp("qqq");
        person1.setIncomes(new ArrayList<>(Arrays.asList(income1, income3, income2, income4)));

        person1.setIncomes(NdflPersonIncome.sortAndUpdateRowNum(person1));

        assertEquals("1", person1.getIncomes().get(0).getOperationId());
        assertEquals("3", person1.getIncomes().get(1).getOperationId());
        assertEquals("3", person1.getIncomes().get(2).getOperationId());
        assertEquals("2", person1.getIncomes().get(3).getOperationId());

        assertEquals(new BigDecimal("5"), person1.getIncomes().get(0).getRowNum());
        assertEquals(new BigDecimal("6"), person1.getIncomes().get(1).getRowNum());
        assertEquals(new BigDecimal("7"), person1.getIncomes().get(2).getRowNum());
        assertEquals(new BigDecimal("8"), person1.getIncomes().get(3).getRowNum());
    }
}
