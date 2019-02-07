package com.aplana.sbrf.taxaccounting.model.ndflPerson;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NdflPersonIncomeTest {

    @Test
    public void testNdflPersonIncomeComporatorByOperationDate() {
        Calendar instance = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 3);
        income1.setOperationId("1");
        income1.setPaymentDate(instance.getTime());
        income1.setOperationDate(instance.getTime());

        NdflPersonIncome income2 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 1);
        income2.setOperationId("2");
        income2.setIncomeAccruedDate(instance.getTime());
        income2.setOperationDate(instance.getTime());

        List<NdflPersonIncome> ndflPersonIncomeList1 = new ArrayList<>(Arrays.asList(income1, income2));

        NdflPerson person1 = new NdflPerson();
        person1.setInp("qqq");
        person1.setIncomes(ndflPersonIncomeList1);

        Collections.sort(ndflPersonIncomeList1, NdflPersonIncome.getComparator());

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

        Collections.sort(ndflPersonIncomes, NdflPersonIncome.getComparator());

        assertEquals("1", ndflPersonIncomes.get(0).getOperationId());
        assertEquals("2", ndflPersonIncomes.get(1).getOperationId());
        assertEquals("3", ndflPersonIncomes.get(2).getOperationId());
    }

    @Test
    public void testNdflPersonIncomeComporatorByActionDate() {
        Calendar calendar = Calendar.getInstance();
        Calendar operationDate = Calendar.getInstance();
        operationDate.set(2010, Calendar.JANUARY, 2);

        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setOperationId("1");
        calendar.set(2010, Calendar.JANUARY, 5);
        income1.setTaxDate(calendar.getTime());
        income1.setOperationDate(operationDate.getTime());
        income1.setActionDate(income1.getTaxDate());

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setOperationId("1");
        calendar.set(2010, Calendar.JANUARY, 1);
        income2.setTaxDate(calendar.getTime());
        income2.setOperationDate(operationDate.getTime());
        income2.setActionDate(income2.getTaxDate());

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setOperationId("1");
        calendar.set(2010, Calendar.JANUARY, 2);
        income3.setPaymentDate(calendar.getTime());
        income3.setOperationDate(operationDate.getTime());
        income3.setActionDate(income3.getPaymentDate());

        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setIncomes(ndflPersonIncomes);

        Collections.sort(ndflPersonIncomes, NdflPersonIncome.getComparator());

        calendar.set(2010, Calendar.JANUARY, 1);
        assertEquals(calendar.getTime(), ndflPersonIncomes.get(0).getTaxDate());
        calendar.set(2010, Calendar.JANUARY, 2);
        assertEquals(calendar.getTime(), ndflPersonIncomes.get(1).getPaymentDate());
        calendar.set(2010, Calendar.JANUARY, 5);
        assertEquals(calendar.getTime(), ndflPersonIncomes.get(2).getTaxDate());
    }

    @Test
    public void testNdflPersonIncomeComporatorByRowType() {
        NdflPersonIncome income1 = new NdflPersonIncome();
        income1.setId(1L);
        income1.setIncomePayoutDate(new Date());
        income1.setRowType(200);

        NdflPersonIncome income2 = new NdflPersonIncome();
        income2.setId(2L);
        income2.setRowType(300);

        NdflPersonIncome income3 = new NdflPersonIncome();
        income3.setId(3L);
        income3.setIncomeAccruedDate(new Date());
        income3.setRowType(100);

        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<>(Arrays.asList(income1, income2, income3));

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setIncomes(ndflPersonIncomes);

        Collections.sort(ndflPersonIncomes, NdflPersonIncome.getComparator());

        assertEquals(new Long(3), ndflPersonIncomes.get(0).getId());
        assertEquals(new Long(1), ndflPersonIncomes.get(1).getId());
        assertEquals(new Long(2), ndflPersonIncomes.get(2).getId());
    }
}
