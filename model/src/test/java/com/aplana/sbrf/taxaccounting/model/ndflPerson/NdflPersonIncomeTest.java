package com.aplana.sbrf.taxaccounting.model.ndflPerson;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class NdflPersonIncomeTest {

    @Test
    public void testNdflPersonIncomeComporatorByOperationDate(){
        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setOperationId("1");

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setOperationId("2");

        List<NdflPersonIncome> ndflPersonIncomeList1 = new ArrayList<>(Arrays.asList(income1, income2));

        NdflPerson person1 = new NdflPerson();
        person1.setInp("qqq");
        person1.setIncomes(ndflPersonIncomeList1);

        Map<Pair<String, String>, Date> operationDates = new HashMap<>();
        Calendar instance = Calendar.getInstance();

        instance.set(2010, Calendar.JANUARY, 3);
        operationDates.put(new Pair<>(income1.getOperationId(), person1.getInp()), instance.getTime());

        instance.set(2010, Calendar.JANUARY, 1);
        operationDates.put(new Pair<>(income2.getOperationId(), person1.getInp()), instance.getTime());

        Collections.sort(ndflPersonIncomeList1, NdflPersonIncome.getComparator(operationDates, person1));

        assertEquals("1", ndflPersonIncomeList1.get(1).getOperationId());
        assertEquals("2", ndflPersonIncomeList1.get(0).getOperationId());
    }

    @Test
    public void testNdflPersonIncomeComporatorByOperationId(){
        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setOperationId("1");

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setOperationId("3");

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setOperationId("2");

        List<NdflPersonIncome> ndflPersonIncomeList1 = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson person1 = new NdflPerson();
        person1.setIncomes(ndflPersonIncomeList1);

        Map<Pair<String, String>, Date> operationDates = new HashMap<>();

        Collections.sort(ndflPersonIncomeList1, NdflPersonIncome.getComparator(operationDates, person1));

        assertEquals("1", ndflPersonIncomeList1.get(0).getOperationId());
        assertEquals("2", ndflPersonIncomeList1.get(1).getOperationId());
        assertEquals("3", ndflPersonIncomeList1.get(2).getOperationId());
    }

    @Test
    public void testNdflPersonIncomeComporatorByActionDate(){
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

        List<NdflPersonIncome> ndflPersonIncomeList1 = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson person1 = new NdflPerson();
        person1.setIncomes(ndflPersonIncomeList1);

        Map<Pair<String, String>, Date> operationDates = new HashMap<>();

        Collections.sort(ndflPersonIncomeList1, NdflPersonIncome.getComparator(operationDates, person1));

        calendar.set(2010, Calendar.JANUARY, 1);
        assertEquals(calendar.getTime(), ndflPersonIncomeList1.get(0).getTaxDate());
        calendar.set(2010, Calendar.JANUARY, 2);
        assertEquals(calendar.getTime(), ndflPersonIncomeList1.get(1).getPaymentDate());
        calendar.set(2010, Calendar.JANUARY, 5);
        assertEquals(calendar.getTime(), ndflPersonIncomeList1.get(2).getTaxDate());
    }

    @Test
    public void testNdflPersonIncomeComporatorBySringType(){
        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setId(1L);
        income1.setIncomePayoutDate(new Date());

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setId(2L);

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setId(3L);
        income3.setIncomeAccruedDate(new Date());

        List<NdflPersonIncome> ndflPersonIncomeList1 = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson person1 = new NdflPerson();
        person1.setIncomes(ndflPersonIncomeList1);

        Map<Pair<String, String>, Date> operationDates = new HashMap<>();

        Collections.sort(ndflPersonIncomeList1, NdflPersonIncome.getComparator(operationDates, person1));

        assertEquals(new Long(3), ndflPersonIncomeList1.get(0).getId());
        assertEquals(new Long(1), ndflPersonIncomeList1.get(1).getId());
        assertEquals(new Long(2), ndflPersonIncomeList1.get(2).getId());
    }



}
